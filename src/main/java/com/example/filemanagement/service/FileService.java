package com.example.filemanagement.service;

import com.example.filemanagement.entity.FileInfo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface FileService {
    
    List<FileInfo> getAllFiles();
    
    PageInfo<FileInfo> getFilesByPage(int pageNum, int pageSize);
    
    FileInfo getFileById(Long id);
    
    FileInfo createFile(FileInfo fileInfo);
    
    FileInfo updateFile(Long id, FileInfo fileInfo);
    
    void deleteFile(Long id);
    
    List<FileInfo> searchFiles(String keyword);
    
    PageInfo<FileInfo> searchFilesByPage(String keyword, int pageNum, int pageSize);
}
