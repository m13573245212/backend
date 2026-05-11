package com.example.filemanagement.controller;

import com.example.filemanagement.entity.ImageInfo;
import com.example.filemanagement.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageInfo> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mdFileId") Long mdFileId) {
        log.info("上传图片: fileName={}, mdFileId={}", file.getOriginalFilename(), mdFileId);
        ImageInfo imageInfo = imageService.uploadImage(file, mdFileId);
        return ResponseEntity.ok(imageInfo);
    }

    @GetMapping("/md/{mdFileId}")
    public ResponseEntity<List<ImageInfo>> getImagesByMdFileId(@PathVariable Long mdFileId) {
        log.info("获取MD文件关联的图片: mdFileId={}", mdFileId);
        return ResponseEntity.ok(imageService.getImagesByMdFileId(mdFileId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        log.info("删除图片: id={}", id);
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/md/{mdFileId}")
    public ResponseEntity<Void> deleteImagesByMdFileId(@PathVariable Long mdFileId) {
        log.info("删除MD文件关联的所有图片: mdFileId={}", mdFileId);
        imageService.deleteImagesByMdFileId(mdFileId);
        return ResponseEntity.noContent().build();
    }
}