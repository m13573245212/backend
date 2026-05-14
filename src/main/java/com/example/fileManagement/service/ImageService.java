package com.example.fileManagement.service;

import com.example.fileManagement.entity.ImageInfo;
import com.example.fileManagement.entity.req.ImageRequest;
import com.github.pagehelper.PageInfo;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ResponseEntity<PageInfo<ImageInfo>> getImages(ImageRequest request);
    ImageInfo uploadImage(MultipartFile file, Long mdFileId);
    ImageInfo getImageById(Long id);
    List<ImageInfo> getImagesByMdFileId(Long mdFileId);
    void deleteImage(Long id);
    void deleteImagesByMdFileId(Long mdFileId);
}