package com.example.filemanagement.mapper;

import com.example.filemanagement.entity.ImageInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageInfoMapper {
    void insertImage(ImageInfo imageInfo);
    ImageInfo getImageById(Long id);
    List<ImageInfo> getImagesByMdFileId(Long mdFileId);
    void deleteImage(Long id);
    void deleteImagesByMdFileId(Long mdFileId);
}