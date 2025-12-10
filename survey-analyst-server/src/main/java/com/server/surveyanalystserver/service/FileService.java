package com.server.surveyanalystserver.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件Service接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取文件信息
     * @param fileUrl 文件URL
     * @return 文件信息
     */
    String getFileInfo(String fileUrl);
    
    /**
     * 保存 base64 图片数据为文件
     * @param base64Data base64 数据（包含 data:image/png;base64, 前缀）
     * @param fileExtension 文件扩展名（如 .png, .jpg）
     * @return 文件访问URL
     */
    String saveBase64Image(String base64Data, String fileExtension);
}

