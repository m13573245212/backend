package com.example.fileManagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;

/**
 * Web 配置类
 * 配置静态资源映射和文件上传解析器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.image-dir:./uploads/images}")
    private String imageDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /images/** 请求映射到图片上传目录
        // 这样 <img src="/images/md_19/photo.png"> 就能直接访问上传的图片
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imageDir + "/");
    }

    /**
     * 配置 CommonsMultipartResolver 替代默认的 StandardServletMultipartResolver
     * 解决临时文件无法清理的问题
     */
    @Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() throws IOException {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        // 设置最大上传文件大小
        resolver.setMaxUploadSize(10 * 1024 * 1024); // 10MB
        // 设置最大内存中的文件大小，超过此大小的文件将被写入临时文件
        resolver.setMaxInMemorySize(4 * 1024 * 1024); // 4MB
        // 设置临时文件目录
        String tempDir = System.getProperty("java.io.tmpdir") + "/spring-multipart";
        File tempFile = new File(tempDir);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        resolver.setUploadTempDir(new org.springframework.core.io.FileSystemResource(tempDir));
        // 设置默认编码
        resolver.setDefaultEncoding("UTF-8");
        return resolver;
    }
}