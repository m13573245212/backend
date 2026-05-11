package com.example.filemanagement.service.impl;

import com.example.filemanagement.entity.FileInfo;
import com.example.filemanagement.mapper.FileInfoMapper;
import com.example.filemanagement.service.FileService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
    
    private final FileInfoMapper fileInfoMapper;

    @Override
    public PageInfo<FileInfo> getFilesByPage(int pageNum, int pageSize, String keyword, String sortField, String sortOrder) {
        log.debug("分页查询文件: pageNum={}, pageSize={}, keyword={}, sortField={}, sortOrder={}", 
                pageNum, pageSize, keyword, sortField, sortOrder);
        
        // 设置排序
        String orderBy = sortField + " " + sortOrder;
        PageHelper.startPage(pageNum, pageSize, orderBy);
        
        // 根据是否有关键词选择查询方式
        List<FileInfo> files = fileInfoMapper.getFiles(keyword);

        PageInfo<FileInfo> pageInfo = new PageInfo<>(files);
        log.debug("分页查询完成: 总记录数={}", pageInfo.getTotal());
        return pageInfo;
    }

    @Override
    public FileInfo getFileById(Long id) {
        log.debug("根据ID查询文件: id={}", id);
        FileInfo file = fileInfoMapper.getFileById(id);
        if (file != null) {
            log.debug("找到文件: {}", file.getFileName());
        } else {
            log.debug("未找到文件: id={}", id);
        }
        return file;
    }

    @Override
    public FileInfo createFile(FileInfo fileInfo) {
        log.debug("插入新文件: {}", fileInfo.getFileName());
        fileInfoMapper.insertFile(fileInfo);
        log.debug("文件插入成功: id={}", fileInfo.getId());
        return fileInfo;
    }

    @Override
    public FileInfo updateFile(Long id, FileInfo fileInfo) {
        log.debug("更新文件: id={}", id);
        FileInfo existingFile = fileInfoMapper.getFileById(id);
        if (existingFile == null) {
            log.warn("更新失败，文件不存在: id={}", id);
            throw new RuntimeException("文件不存在");
        }
        fileInfo.setId(id);
        fileInfoMapper.updateFile(fileInfo);
        log.debug("文件更新成功: id={}", id);
        return fileInfo;
    }

    @Override
    public void deleteFile(Long id) {
        log.debug("删除文件: id={}", id);
        FileInfo existingFile = fileInfoMapper.getFileById(id);
        if (existingFile == null) {
            log.warn("删除失败，文件不存在: id={}", id);
            throw new RuntimeException("文件不存在");
        }
        fileInfoMapper.deleteFile(id);
        log.debug("文件删除成功: id={}", id);
    }


    @Override
    public PreviewResult previewFile(Long id) {
        log.debug("预览文件: id={}", id);
        FileInfo fileInfo = fileInfoMapper.getFileById(id);
        if (fileInfo == null) {
            log.warn("预览失败，文件不存在: id={}", id);
            throw new RuntimeException("文件不存在");
        }
        
        String filePath = fileInfo.getFilePath();
        log.debug("文件路径: {}", filePath);
        
        // 处理 Blob URL 的情况
        if (filePath.startsWith("blob:")) {
            log.warn("Blob URL 无法直接读取，请使用本地文件路径");
            return new PreviewResult("⚠️ Blob URL 无法直接读取，请使用本地文件路径", false);
        }
        
        // 处理普通文件路径
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("文件不存在: {}", filePath);
            return new PreviewResult("⚠️ 文件不存在: " + filePath, false);
        }
        
        // 使用 UTF-8 编码读取文件内容
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            log.debug("文件读取成功，内容长度: {} 字符", content.length());
        } catch (Exception e) {
            log.error("文件读取失败: {}", e.getMessage());
            return new PreviewResult("⚠️ 文件读取失败: " + e.getMessage(), false);
        }
        
        // 判断是否为 Markdown 文件
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".md") || fileName.endsWith(".markdown")) {
            log.debug("Markdown 文件，进行 HTML 转换");
            return new PreviewResult(convertMarkdownToHtml(content.toString()), true);
        }
        
        return new PreviewResult(content.toString(), false);
    }
    
    /**
     * 将 Markdown 内容转换为 HTML
     */
    private String convertMarkdownToHtml(String markdown) {
        try {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(markdown);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String html = renderer.render(document);
            // 添加基础样式
            return "<style>" +
                    "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; padding: 20px; }" +
                    "h1 { border-bottom: 1px solid #eee; padding-bottom: 0.3em; }" +
                    "h2 { border-bottom: 1px solid #eee; padding-bottom: 0.3em; }" +
                    "code { background-color: #f6f8fa; padding: 0.2em 0.4em; border-radius: 6px; font-size: 85%; }" +
                    "pre { background-color: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }" +
                    "pre code { padding: 0; }" +
                    "a { color: #0366d6; text-decoration: none; }" +
                    "a:hover { text-decoration: underline; }" +
                    "ul, ol { padding-left: 2em; }" +
                    "li { margin-top: 0.25em; }" +
                    "blockquote { margin: 0; padding: 0 1em; color: #6a737d; border-left: 0.25em solid #dfe2e5; }" +
                    "table { border-spacing: 0; border-collapse: collapse; width: 100%; }" +
                    "th, td { padding: 6px 13px; border: 1px solid #dfe2e5; }" +
                    "tr:nth-child(2n) { background-color: #f6f8fa; }" +
                    "img { max-width: 100%; }" +
                    "</style>" + html;
        } catch (Exception e) {
            log.error("Markdown 转换失败: {}", e.getMessage());
            return "<p>⚠️ Markdown 转换失败: " + e.getMessage() + "</p><pre>" + markdown + "</pre>";
        }
    }
}
