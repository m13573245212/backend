package com.example.filemanagement.service.impl;

import com.example.filemanagement.entity.FileInfo;
import com.example.filemanagement.mapper.FileInfoMapper;
import com.example.filemanagement.service.FileService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    
    private final FileInfoMapper fileInfoMapper;

    @Override
    public List<FileInfo> getAllFiles() {
        logger.debug("查询所有文件");
        List<FileInfo> files = fileInfoMapper.getAllFiles();
        logger.debug("查询完成，共 {} 条记录", files.size());
        return files;
    }

    @Override
    public PageInfo<FileInfo> getFilesByPage(int pageNum, int pageSize) {
        logger.debug("分页查询文件: pageNum={}, pageSize={}", pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<FileInfo> files = fileInfoMapper.getAllFiles();
        PageInfo<FileInfo> pageInfo = new PageInfo<>(files);
        logger.debug("分页查询完成: 总记录数={}", pageInfo.getTotal());
        return pageInfo;
    }

    @Override
    public FileInfo getFileById(Long id) {
        logger.debug("根据ID查询文件: id={}", id);
        FileInfo file = fileInfoMapper.getFileById(id);
        if (file != null) {
            logger.debug("找到文件: {}", file.getFileName());
        } else {
            logger.debug("未找到文件: id={}", id);
        }
        return file;
    }

    @Override
    public FileInfo createFile(FileInfo fileInfo) {
        logger.debug("插入新文件: {}", fileInfo.getFileName());
        fileInfoMapper.insertFile(fileInfo);
        logger.debug("文件插入成功: id={}", fileInfo.getId());
        return fileInfo;
    }

    @Override
    public FileInfo updateFile(Long id, FileInfo fileInfo) {
        logger.debug("更新文件: id={}", id);
        FileInfo existingFile = fileInfoMapper.getFileById(id);
        if (existingFile == null) {
            logger.warn("更新失败，文件不存在: id={}", id);
            throw new RuntimeException("文件不存在");
        }
        fileInfo.setId(id);
        fileInfoMapper.updateFile(fileInfo);
        logger.debug("文件更新成功: id={}", id);
        return fileInfo;
    }

    @Override
    public void deleteFile(Long id) {
        logger.debug("删除文件: id={}", id);
        FileInfo existingFile = fileInfoMapper.getFileById(id);
        if (existingFile == null) {
            logger.warn("删除失败，文件不存在: id={}", id);
            throw new RuntimeException("文件不存在");
        }
        fileInfoMapper.deleteFile(id);
        logger.debug("文件删除成功: id={}", id);
    }

    @Override
    public List<FileInfo> searchFiles(String keyword) {
        logger.debug("搜索文件: keyword={}", keyword);
        List<FileInfo> files = fileInfoMapper.searchFiles(keyword);
        logger.debug("搜索完成，找到 {} 条记录", files.size());
        return files;
    }

    @Override
    public PageInfo<FileInfo> searchFilesByPage(String keyword, int pageNum, int pageSize) {
        logger.debug("分页搜索文件: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<FileInfo> files = fileInfoMapper.searchFiles(keyword);
        PageInfo<FileInfo> pageInfo = new PageInfo<>(files);
        logger.debug("分页搜索完成: 总记录数={}", pageInfo.getTotal());
        return pageInfo;
    }
}
