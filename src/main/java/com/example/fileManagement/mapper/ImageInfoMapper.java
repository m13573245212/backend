package com.example.fileManagement.mapper;

import com.example.fileManagement.entity.ImageInfo;
import com.example.fileManagement.entity.req.ImageRequest;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageInfoMapper {
    PageInfo<ImageInfo> getImages(ImageRequest request);
    void insertImage(ImageInfo imageInfo);
    ImageInfo getImageById(Long id);
    List<ImageInfo> getImagesByMdFileId(Long mdFileId);
    void deleteImage(Long id);
    void deleteImagesByMdFileId(Long mdFileId);
}