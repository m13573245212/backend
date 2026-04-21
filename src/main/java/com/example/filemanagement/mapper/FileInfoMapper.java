package com.example.filemanagement.mapper;

import com.example.filemanagement.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileInfoMapper {
    
    List<FileInfo> getAllFiles();
    
    FileInfo getFileById(Long id);
    
    int insertFile(FileInfo fileInfo);
    
    int updateFile(FileInfo fileInfo);
    
    int deleteFile(Long id);
    
    List<FileInfo> searchFiles(String keyword);
}
