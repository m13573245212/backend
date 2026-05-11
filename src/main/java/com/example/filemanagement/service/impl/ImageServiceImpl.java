package com.example.filemanagement.service.impl;

import com.example.filemanagement.entity.ImageInfo;
import com.example.filemanagement.mapper.ImageInfoMapper;
import com.example.filemanagement.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageInfoMapper imageInfoMapper;

    @Value("${file.upload.image-dir:./uploads/images}")
    private String imageDir;

    @Override
    public ImageInfo uploadImage(MultipartFile file, Long mdFileId) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }

        // 创建 MD 文件专属子目录：images/md_{mdFileId}/
        String mdSubDir = "md_" + mdFileId;
        Path uploadPath = Paths.get(imageDir, mdSubDir);
        
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("创建MD文件专属目录: {}", uploadPath);
            } catch (IOException e) {
                log.error("创建目录失败", e);
                throw new RuntimeException("创建目录失败");
            }
        }

        // 直接使用原始文件名，子目录已隔离重名
        Path filePath = uploadPath.resolve(originalName);
        File destFile = filePath.toFile();

        try {
            file.transferTo(destFile);
            log.info("图片上传成功: {}", filePath);

            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setFileName(originalName);
            imageInfo.setFilePath(filePath.toString());
            imageInfo.setMdFileId(mdFileId);

            imageInfoMapper.insertImage(imageInfo);
            log.info("图片记录保存成功: id={}", imageInfo.getId());

            return imageInfo;

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败");
        }
    }

    @Override
    public ImageInfo getImageById(Long id) {
        return imageInfoMapper.getImageById(id);
    }

    @Override
    public List<ImageInfo> getImagesByMdFileId(Long mdFileId) {
        return imageInfoMapper.getImagesByMdFileId(mdFileId);
    }

    @Override
    public void deleteImage(Long id) {
        ImageInfo imageInfo = imageInfoMapper.getImageById(id);
        if (imageInfo == null) {
            throw new RuntimeException("图片不存在");
        }

        File file = new File(imageInfo.getFilePath());
        if (file.exists()) {
            boolean deleted = file.delete();
            log.info("物理文件删除: {} - {}", imageInfo.getFilePath(), deleted ? "成功" : "失败");
        }

        imageInfoMapper.deleteImage(id);
        log.info("图片记录删除成功: id={}", id);
    }

    @Override
    public void deleteImagesByMdFileId(Long mdFileId) {
        List<ImageInfo> images = imageInfoMapper.getImagesByMdFileId(mdFileId);
        
        for (ImageInfo image : images) {
            File file = new File(image.getFilePath());
            if (file.exists()) {
                file.delete();
            }
            imageInfoMapper.deleteImage(image.getId());
        }

        // 删除 MD 文件专属目录
        Path mdDir = Paths.get(imageDir, "md_" + mdFileId);
        try {
            Files.deleteIfExists(mdDir);
            log.info("删除MD文件专属目录: {}", mdDir);
        } catch (IOException e) {
            log.warn("删除目录失败: {}", mdDir);
        }

        log.info("删除MD文件关联的所有图片: mdFileId={}, count={}", mdFileId, images.size());
    }
}