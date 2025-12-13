package com.server.surveyanalystserver.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件Service接口
 */
public interface FileService {

    /**
     * 上传文件
     * 上传文件到服务器，支持多种文件格式（图片、文档等）
     * @param file 上传的文件对象
     * @return 文件访问URL，用于后续访问该文件
     */
    String uploadFile(MultipartFile file);

    /**
     * 删除文件
     * 根据文件URL删除服务器上的文件
     * @param fileUrl 文件访问URL
     * @return true表示删除成功，false表示删除失败
     */
    boolean deleteFile(String fileUrl);

    /**
     * 获取文件信息
     * 根据文件URL获取文件的详细信息（如文件大小、类型等）
     * @param fileUrl 文件访问URL
     * @return 文件信息字符串
     */
    String getFileInfo(String fileUrl);
    
    /**
     * 保存base64图片数据为文件
     * 将base64编码的图片数据保存为文件，并返回文件访问URL
     * @param base64Data base64数据（包含data:image/png;base64,前缀）
     * @param fileExtension 文件扩展名（如.png, .jpg）
     * @return 文件访问URL
     */
    String saveBase64Image(String base64Data, String fileExtension);
}

