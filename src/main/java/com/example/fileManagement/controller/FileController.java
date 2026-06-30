package com.example.fileManagement.controller;

import com.example.fileManagement.entity.FileInfo;
import com.example.fileManagement.service.FileService;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/page")
    public ResponseEntity<PageInfo<FileInfo>> getFilesByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        PageInfo<FileInfo> pageInfo = fileService.getFilesByPage(pageNum, pageSize, keyword, sortField, sortOrder);
        return ResponseEntity.ok(pageInfo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileInfo> getFileById(@PathVariable Long id) {
        FileInfo file = fileService.getFileById(id);
        if (file == null) {
            log.warn("文件不存在: id={}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }

    @PostMapping
    public ResponseEntity<FileInfo> createFile(@RequestBody FileInfo fileInfo) {
        FileInfo createdFile = fileService.createFile(fileInfo);
        return ResponseEntity.ok(createdFile);
    }

    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("上传文件: fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        FileInfo uploadedFile = fileService.uploadFile(file);
        return ResponseEntity.ok(uploadedFile);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<FileInfo> updateFile(@PathVariable Long id, @RequestBody FileInfo fileInfo) {
        FileInfo updatedFile = fileService.updateFile(id, fileInfo);
        return ResponseEntity.ok(updatedFile);
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<FileService.PreviewResult> previewFile(@PathVariable Long id, HttpServletRequest request) {
        FileService.PreviewResult result = fileService.previewFile(id,request);
        return ResponseEntity.ok(result);
    }

}
