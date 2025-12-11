package com.server.surveyanalystserver.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 数据加密工具类
 * 使用AES对称加密算法对敏感数据进行加密存储
 */
@Component
public class DataEncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    /**
     * 加密密钥（从配置文件读取，如果没有配置则使用默认密钥）
     * 注意：生产环境必须使用环境变量配置密钥
     */
    @Value("${data.encryption.secret-key:4pR7tY9wE2qA5sD8fG1hJ3kL6mN0pQ2rT5vX8zB}")
    private String secretKey;

    /**
     * 加密数据
     * @param data 原始数据
     * @return 加密后的Base64字符串
     */
    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            // 确保密钥长度为16、24或32字节（AES-128、AES-192、AES-256）
            byte[] keyBytes = ensureKeyLength(secretKey.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("数据加密失败", e);
        }
    }

    /**
     * 解密数据
     * @param encryptedData 加密后的Base64字符串
     * @return 原始数据
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        try {
            // 确保密钥长度为16、24或32字节
            byte[] keyBytes = ensureKeyLength(secretKey.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("数据解密失败", e);
        }
    }

    /**
     * 确保密钥长度为16、24或32字节
     * @param keyBytes 原始密钥字节数组
     * @return 调整后的密钥字节数组
     */
    private byte[] ensureKeyLength(byte[] keyBytes) {
        int length = keyBytes.length;
        if (length == 16 || length == 24 || length == 32) {
            return keyBytes;
        }

        // 如果长度不符合要求，截取或填充到16字节（AES-128）
        byte[] result = new byte[16];
        if (length > 16) {
            System.arraycopy(keyBytes, 0, result, 0, 16);
        } else {
            System.arraycopy(keyBytes, 0, result, 0, length);
            // 填充剩余字节
            for (int i = length; i < 16; i++) {
                result[i] = (byte) (i - length);
            }
        }
        return result;
    }
}

