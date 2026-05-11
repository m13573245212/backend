package com.example.filemanagement.service;

import com.example.filemanagement.entity.ImageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    ImageInfo uploadImage(MultipartFile file, Long mdFileId);
    ImageInfo getImageById(Long id);
    List<ImageInfo> getImagesByMdFileId(Long mdFileId);
    void deleteImage(Long id);
    void deleteImagesByMdFileId(Long mdFileId);
}