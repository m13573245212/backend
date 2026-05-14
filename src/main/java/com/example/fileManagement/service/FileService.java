package com.example.fileManagement.service;

import com.example.fileManagement.entity.FileInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


public interface FileService {
    
    PageInfo<FileInfo> getFilesByPage(int pageNum, int pageSize, String keyword, String sortField, String sortOrder);
    
    FileInfo getFileById(Long id);
    
    FileInfo createFile(FileInfo fileInfo);
    
    FileInfo uploadFile(MultipartFile file);
    
    FileInfo updateFile(Long id, FileInfo fileInfo);
    
    void deleteFile(Long id);
    
    PreviewResult previewFile(Long id, HttpServletRequest request);
    
    /**
     * 文件预览结果
     */
    class PreviewResult {
        private final String content;
        private final boolean isMarkdown;
        public PreviewResult(String content, boolean isMarkdown) {
            this.content = content;
            this.isMarkdown = isMarkdown;
        }
        public String getContent() {
            return content;
        }
        @JsonProperty("isMarkdown")
        public boolean isMarkdown() {
            return isMarkdown;
        }
    }
}
