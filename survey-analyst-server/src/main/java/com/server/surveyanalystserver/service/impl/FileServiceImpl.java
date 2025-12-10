package com.server.surveyanalystserver.service.impl;

import com.server.surveyanalystserver.config.FileConfig;
import com.server.surveyanalystserver.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * 文件Service实现类
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileConfig fileConfig;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 验证文件大小
        long maxSize = fileConfig.getMaxSize();
        if (file.getSize() > maxSize) {
            long fileSizeMB = file.getSize() / (1024 * 1024);
            long maxSizeMB = maxSize / (1024 * 1024);
            throw new RuntimeException(String.format("文件大小超过限制：当前文件 %dMB，最大允许 %dMB", fileSizeMB, maxSizeMB));
        }

        try {
            // 获取文件原始名称
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("文件名不能为空");
            }

            // 获取文件扩展名
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }

            // 生成新的文件名
            String newFileName = UUID.randomUUID().toString() + extension;

            // 按日期创建目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadPath = fileConfig.getPath() + "/" + dateDir;
            
            // 创建目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 保存文件
            Path filePath = Paths.get(uploadPath, newFileName);
            Files.write(filePath, file.getBytes());

            // 生成文件访问URL
            String fileUrl = "/upload/" + dateDir + "/" + newFileName;
            return fileUrl;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            // 移除URL前缀
            String relativePath = fileUrl;
            if (fileUrl.startsWith("/upload/")) {
                relativePath = fileUrl.substring(7);
            }

            // 构建完整路径
            String fullPath = fileConfig.getPath() + "/" + relativePath;
            File file = new File(fullPath);

            if (file.exists() && file.isFile()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFileInfo(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return null;
            }

            // 移除URL前缀
            String relativePath = fileUrl;
            if (fileUrl.startsWith("/upload/")) {
                relativePath = fileUrl.substring(7);
            }

            // 构建完整路径
            String fullPath = fileConfig.getPath() + "/" + relativePath;
            File file = new File(fullPath);

            if (file.exists() && file.isFile()) {
                return "文件大小: " + file.length() + " 字节";
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String saveBase64Image(String base64Data, String fileExtension) {
        if (base64Data == null || base64Data.isEmpty()) {
            throw new RuntimeException("base64数据不能为空");
        }
        
        try {
            // 移除 base64 数据前缀（如 data:image/png;base64,）
            String base64Content = base64Data;
            if (base64Data.contains(",")) {
                base64Content = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            
            // 解码 base64 数据
            byte[] imageBytes = Base64.getDecoder().decode(base64Content);
            
            // 验证文件大小
            long maxSize = fileConfig.getMaxSize();
            if (imageBytes.length > maxSize) {
                long fileSizeMB = imageBytes.length / (1024 * 1024);
                long maxSizeMB = maxSize / (1024 * 1024);
                throw new RuntimeException(String.format("文件大小超过限制：当前文件 %dMB，最大允许 %dMB", fileSizeMB, maxSizeMB));
            }
            
            // 生成新的文件名
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 按日期创建目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadPath = fileConfig.getPath() + "/" + dateDir;
            
            // 创建目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
            Path filePath = Paths.get(uploadPath, newFileName);
            Files.write(filePath, imageBytes);
            
            // 生成文件访问URL
            String fileUrl = "/upload/" + dateDir + "/" + newFileName;
            return fileUrl;
        } catch (Exception e) {
            throw new RuntimeException("保存base64图片失败", e);
        }
    }
}

