package com.example.filemanagement.controller;

import com.example.filemanagement.entity.FileInfo;
import com.example.filemanagement.service.FileService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileInfo>> getAllFiles() {
        logger.info("获取所有文件列表");
        List<FileInfo> files = fileService.getAllFiles();
        logger.info("成功获取 {} 个文件", files.size());
        return ResponseEntity.ok(files);
    }

    @GetMapping("/page")
    public ResponseEntity<PageInfo<FileInfo>> getFilesByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        logger.info("分页获取文件列表: pageNum={}, pageSize={}", pageNum, pageSize);
        PageInfo<FileInfo> pageInfo = fileService.getFilesByPage(pageNum, pageSize);
        logger.info("成功获取分页数据: 总记录数={}, 当前页={}", pageInfo.getTotal(), pageInfo.getPageNum());
        return ResponseEntity.ok(pageInfo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileInfo> getFileById(@PathVariable Long id) {
        logger.info("获取文件详情: id={}", id);
        FileInfo file = fileService.getFileById(id);
        if (file == null) {
            logger.warn("文件不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("成功获取文件: {}", file.getFileName());
        return ResponseEntity.ok(file);
    }

    @PostMapping
    public ResponseEntity<FileInfo> createFile(@RequestBody FileInfo fileInfo) {
        logger.info("创建新文件: {}", fileInfo.getFileName());
        FileInfo createdFile = fileService.createFile(fileInfo);
        logger.info("文件创建成功: id={}, name={}", createdFile.getId(), createdFile.getFileName());
        return ResponseEntity.ok(createdFile);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileInfo> updateFile(@PathVariable Long id, @RequestBody FileInfo fileInfo) {
        logger.info("更新文件: id={}", id);
        try {
            FileInfo updatedFile = fileService.updateFile(id, fileInfo);
            logger.info("文件更新成功: id={}", id);
            return ResponseEntity.ok(updatedFile);
        } catch (RuntimeException e) {
            logger.warn("文件更新失败: id={}, 原因={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        logger.info("删除文件: id={}", id);
        try {
            fileService.deleteFile(id);
            logger.info("文件删除成功: id={}", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.warn("文件删除失败: id={}, 原因={}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileInfo>> searchFiles(@RequestParam String keyword) {
        logger.info("搜索文件: keyword={}", keyword);
        List<FileInfo> files = fileService.searchFiles(keyword);
        logger.info("搜索完成: 找到 {} 个文件", files.size());
        return ResponseEntity.ok(files);
    }

    @GetMapping("/search/page")
    public ResponseEntity<PageInfo<FileInfo>> searchFilesByPage(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        logger.info("分页搜索文件: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        PageInfo<FileInfo> pageInfo = fileService.searchFilesByPage(keyword, pageNum, pageSize);
        logger.info("分页搜索完成: 总记录数={}", pageInfo.getTotal());
        return ResponseEntity.ok(pageInfo);
    }
}
