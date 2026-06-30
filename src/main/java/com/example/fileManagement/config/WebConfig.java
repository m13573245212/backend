package com.example.fileManagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置静态资源映射和文件上传解析器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.image-dir:./uploads/images}")
    private String imageDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 将 /images/** 请求映射到图片上传目录
        // 这样 <img src="/images/md_19/photo.png"> 就能直接访问上传的图片
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imageDir + "/");
    }

}