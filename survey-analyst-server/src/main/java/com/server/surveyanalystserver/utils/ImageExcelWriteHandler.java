package com.server.surveyanalystserver.utils;

import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Excel图片写入处理器
 * 用于在Excel中嵌入图片
 */
@Component
public class ImageExcelWriteHandler implements WorkbookWriteHandler {

    private static final String UPLOAD_DIR = "upload/";
    private final Set<String> imageColumns = new HashSet<>();
    private final Map<Integer, Map<String, Object>> rowDataMap = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 设置图片列
     */
    public void setImageColumns(Set<String> columns) {
        imageColumns.clear();
        if (columns != null) {
            imageColumns.addAll(columns);
        }
    }

    /**
     * 设置行数据（用于获取图片URL）
     */
    public void setRowData(int rowIndex, Map<String, Object> rowData) {
        rowDataMap.put(rowIndex, rowData);
    }

    @Override
    public void afterWorkbookDispose(WriteWorkbookHolder writeWorkbookHolder) {
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        
        // 处理每个sheet
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            Drawing<?> drawing = sheet.createDrawingPatriarch();

        // 获取表头，确定图片列的位置
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return;
        }

        // 构建列名到列索引的映射
        java.util.Map<String, Integer> columnIndexMap = new java.util.HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String columnName = cell.getStringCellValue();
                columnIndexMap.put(columnName, i);
            }
        }

        // 遍历数据行，处理图片
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            Map<String, Object> rowData = rowDataMap.get(rowIndex - 1); // rowIndex - 1因为header是第0行
            if (rowData == null) {
                continue;
            }

            // 处理每个图片列
            for (String columnName : imageColumns) {
                Integer colIndex = columnIndexMap.get(columnName);
                if (colIndex == null) {
                    continue;
                }

                Object imageValue = rowData.get(columnName);
                if (imageValue == null || imageValue.toString().isEmpty()) {
                    continue;
                }

                try {
                    // 解析图片URL（可能是多个图片用分号分隔）
                    String[] imageUrls = imageValue.toString().split(";");
                    int imageIndex = 0;
                    for (String imageUrl : imageUrls) {
                        imageUrl = imageUrl.trim();
                        if (imageUrl.isEmpty()) {
                            continue;
                        }

                        // 读取图片
                        byte[] imageBytes = readImageBytes(imageUrl);
                        if (imageBytes == null || imageBytes.length == 0) {
                            continue;
                        }

                        // 创建图片
                        int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
                        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
                        anchor.setCol1(colIndex);
                        anchor.setRow1(rowIndex);
                        anchor.setCol2(colIndex + 1);
                        anchor.setRow2(rowIndex + 1);
                        
                        // 调整图片位置（避免重叠）
                        if (imageIndex > 0) {
                            anchor.setCol1(colIndex + imageIndex);
                            anchor.setCol2(colIndex + imageIndex + 1);
                        }

                        drawing.createPicture(anchor, pictureIdx);
                        
                        // 设置行高以适应图片
                        row.setHeightInPoints(60);
                        
                        imageIndex++;
                    }
                } catch (Exception e) {
                    // 图片处理失败，继续处理下一列
                    e.printStackTrace();
                }
            }
        }

            // 清理数据
            rowDataMap.clear();
        }
    }

    /**
     * 读取图片字节
     */
    private byte[] readImageBytes(String imageUrl) {
        try {
            // 如果是Base64格式（data:image/...;base64,...），直接解码
            if (imageUrl != null && imageUrl.startsWith("data:image/")) {
                int base64Index = imageUrl.indexOf("base64,");
                if (base64Index > 0) {
                    String base64Data = imageUrl.substring(base64Index + 7); // 跳过 "base64,"
                    return java.util.Base64.getDecoder().decode(base64Data);
                }
            }
            
            // 如果是相对路径（以/upload/开头），转换为本地文件路径
            if (imageUrl.startsWith("/upload/")) {
                String relativePath = imageUrl.substring(1); // 去掉开头的/
                File imageFile = new File(UPLOAD_DIR + relativePath);
                if (imageFile.exists() && imageFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        return IOUtils.toByteArray(fis);
                    }
                }
            } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // 如果是HTTP URL，从网络读取
                try (InputStream is = new URL(imageUrl).openStream()) {
                    return IOUtils.toByteArray(is);
                }
            } else {
                // 尝试作为相对路径处理
                File imageFile = new File(UPLOAD_DIR + imageUrl);
                if (imageFile.exists() && imageFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        return IOUtils.toByteArray(fis);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
