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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Excel图片写入处理器
 * 用于在Excel中嵌入图片
 */
@Component
public class ImageExcelWriteHandler implements WorkbookWriteHandler {

    // 上传文件目录（相对于项目根目录）
    // 注意：这个路径应该与 FileConfig 中的 path 配置一致
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
        
        System.out.println("=== 开始处理图片嵌入 ===");
        System.out.println("图片列数: " + imageColumns.size() + ", 行数据数: " + rowDataMap.size());
        System.out.println("图片列: " + imageColumns);
        
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
                    System.err.println("图片列未找到: " + columnName + ", 可用列: " + columnIndexMap.keySet());
                    continue;
                }

                Object imageValue = rowData.get(columnName);
                if (imageValue == null || imageValue.toString().isEmpty()) {
                    System.err.println("图片列数据为空: " + columnName + ", rowData keys: " + rowData.keySet());
                    continue;
                }

                try {
                    String imageValueStr = imageValue.toString();
                    System.out.println("处理图片列: " + columnName + ", 值: " + 
                        (imageValueStr.length() > 100 ? imageValueStr.substring(0, 100) + "..." : imageValueStr));
                    
                    // 解析图片URL（可能是多个图片用分号分隔）
                    // 图片数据已保存为文件路径，格式为 /upload/yyyy/MM/dd/filename.ext
                    List<String> imageUrls = new ArrayList<>();
                    String[] simpleUrls = imageValueStr.split(";");
                    for (String url : simpleUrls) {
                        url = url.trim();
                        if (!url.isEmpty()) {
                            imageUrls.add(url);
                        }
                    }
                    
                    // 先统计实际要处理的图片数量（可能包含空格分隔的多个URL）
                    int totalImages = 0;
                    for (String imageUrl : imageUrls) {
                        if (!imageUrl.isEmpty()) {
                            if (imageUrl.contains(" ")) {
                                String[] urls = imageUrl.split("\\s+");
                                for (String url : urls) {
                                    if (!url.trim().isEmpty()) {
                                        totalImages++;
                                    }
                                }
                            } else {
                                totalImages++;
                            }
                        }
                    }
                    
                    // 处理每个图片URL
                    int imageIndex = 0;
                    for (String imageUrl : imageUrls) {
                        if (imageUrl.isEmpty()) {
                            continue;
                        }
                        
                        // 如果URL包含空格分隔的多个URL，进一步分割
                        if (imageUrl.contains(" ")) {
                            String[] urls = imageUrl.split("\\s+");
                            for (String url : urls) {
                                url = url.trim();
                                if (url.isEmpty()) continue;
                                processImage(workbook, drawing, row, sheet, colIndex, rowIndex, url, imageIndex++, totalImages);
                            }
                            continue;
                        }
                        
                        processImage(workbook, drawing, row, sheet, colIndex, rowIndex, imageUrl, imageIndex++, totalImages);
                    }
                } catch (Exception e) {
                    // 图片处理失败，继续处理下一列
                    System.err.println("处理图片失败: " + columnName + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        // 清理数据
        rowDataMap.clear();
        }
    }
    
    /**
     * 处理单张图片
     */
    private void processImage(Workbook workbook, Drawing<?> drawing, Row row, Sheet sheet,
                             int colIndex, int rowIndex, String imageUrl, int imageIndex, int totalImages) {
        try {
            System.out.println("开始处理图片: row=" + rowIndex + ", col=" + colIndex + ", index=" + imageIndex + 
                "/" + totalImages + ", url=" + (imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl));
            
            // 读取图片
            byte[] imageBytes = readImageBytes(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                System.err.println("读取图片失败或图片为空: " + imageUrl);
                return;
            }
            
            System.out.println("成功读取图片，大小: " + imageBytes.length + " bytes");

            // 检测图片类型（根据文件扩展名）
            int pictureType = Workbook.PICTURE_TYPE_PNG; // 默认PNG
            String urlLower = imageUrl.toLowerCase();
            if (urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg")) {
                pictureType = Workbook.PICTURE_TYPE_JPEG;
            } else {
                // PNG、GIF等其他格式都使用PNG类型
                pictureType = Workbook.PICTURE_TYPE_PNG;
            }
            
            // 创建图片
            int pictureIdx = workbook.addPicture(imageBytes, pictureType);
            
            // 获取列宽和行高（以像素为单位）
            int colWidth = sheet.getColumnWidth(colIndex);
            if (colWidth < 3000) {
                colWidth = 3000; // 设置最小列宽（约30个字符）
                sheet.setColumnWidth(colIndex, colWidth);
            }
            
            // 列宽转换：Excel列宽单位是1/256字符宽度，转换为像素大约乘以0.75
            double cellWidthPx = colWidth * 0.75;
            
            // 行高（以点为单位，1点=1/72英寸）
            double rowHeightPoints = row.getHeightInPoints();
            if (rowHeightPoints < 30) {
                rowHeightPoints = 30; // 最小行高
            }
            // 如果有多张图片，增加行高
            if (totalImages > 1) {
                rowHeightPoints = Math.max(rowHeightPoints, totalImages * 80.0); // 每张图片约80点高度
            } else {
                rowHeightPoints = Math.max(rowHeightPoints, 80.0); // 单张图片80点高度
            }
            row.setHeightInPoints((float) rowHeightPoints);
            
            // 行高转换为像素：1点 = 1.33像素
            double cellHeightPx = rowHeightPoints * 1.33;
            
            // 如果有多张图片，计算每张图片的高度和垂直偏移
            double imageHeight = cellHeightPx / totalImages; // 每张图片分配的高度
            double verticalOffset = imageIndex * imageHeight; // 当前图片的垂直偏移
            
            // 创建锚点，确保图片在正确的单元格内
            ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            
            // 始终保持在指定列
            anchor.setCol1(colIndex);
            anchor.setCol2(colIndex + 1);
            anchor.setRow1(rowIndex);
            anchor.setRow2(rowIndex + 1);
            
            // 使用 dx 和 dy 来精确控制图片位置（单位：EMU，1像素 ≈ 9525 EMU）
            // dx1, dy1: 左上角偏移；dx2, dy2: 右下角偏移
            // 注意：Excel 的单元格坐标和像素转换需要精确计算
            int dx1 = 2 * 9525; // 左边距（约2像素）
            int dy1 = (int) (verticalOffset * 9525) + 2 * 9525; // 垂直偏移（多张图片时纵向排列）+ 上边距
            int dx2 = (int) (cellWidthPx * 9525) - 2 * 9525; // 右边界 - 右边距
            int dy2 = (int) ((verticalOffset + imageHeight) * 9525) - 2 * 9525; // 下边界 - 下边距
            
            anchor.setDx1(dx1);
            anchor.setDy1(dy1);
            anchor.setDx2(dx2);
            anchor.setDy2(dy2);

            // 插入图片
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            
            // 调用 resize() 让图片自动适应锚点定义的区域（保持宽高比）
            picture.resize();
            
            System.out.println("成功插入图片到 row=" + rowIndex + ", col=" + colIndex + 
                ", imageIndex=" + imageIndex + "/" + totalImages +
                ", 位置: dx1=" + dx1 + ", dy1=" + dy1 + ", dx2=" + dx2 + ", dy2=" + dy2 +
                ", 单元格大小: " + cellWidthPx + "x" + imageHeight + " 像素");
        } catch (Exception e) {
            System.err.println("插入图片异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 读取图片字节
     * 图片数据已保存为文件，路径格式为 /upload/yyyy/MM/dd/filename.ext
     */
    private byte[] readImageBytes(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 如果是相对路径（以/upload/开头），转换为本地文件路径
            // 路径格式：/upload/yyyy/MM/dd/filename.ext
            if (imageUrl.startsWith("/upload/")) {
                // 去掉开头的/upload/，直接拼接 upload/ 目录
                String relativePath = imageUrl.substring(8); // 去掉 "/upload/"
                File imageFile = new File(UPLOAD_DIR + relativePath);
                System.out.println("尝试读取文件: " + imageFile.getAbsolutePath() + ", 存在: " + imageFile.exists());
                if (imageFile.exists() && imageFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        byte[] imageBytes = IOUtils.toByteArray(fis);
                        System.out.println("成功读取文件: " + imageFile.getAbsolutePath() + ", 大小: " + imageBytes.length + " bytes");
                        return imageBytes;
                    }
                } else {
                    System.err.println("文件不存在: " + imageFile.getAbsolutePath());
                    // 尝试其他可能的路径（备用方案）
                    File altFile = new File(relativePath);
                    if (altFile.exists() && altFile.isFile()) {
                        System.out.println("使用替代路径读取文件: " + altFile.getAbsolutePath());
                        try (FileInputStream fis = new FileInputStream(altFile)) {
                            return IOUtils.toByteArray(fis);
                        }
                    }
                }
            } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // 如果是HTTP URL，从网络读取
                try (InputStream is = new URL(imageUrl).openStream()) {
                    byte[] imageBytes = IOUtils.toByteArray(is);
                    System.out.println("成功从网络读取图片: " + imageUrl + ", 大小: " + imageBytes.length + " bytes");
                    return imageBytes;
                } catch (Exception e) {
                    System.err.println("网络读取图片失败: " + imageUrl + ", 错误: " + e.getMessage());
                }
            } else {
                // 尝试作为相对路径处理（直接是文件名，拼接 upload/ 目录）
                File imageFile = new File(UPLOAD_DIR + imageUrl);
                if (imageFile.exists() && imageFile.isFile()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        return IOUtils.toByteArray(fis);
                    }
                } else {
                    // 尝试作为绝对路径处理
                    File absFile = new File(imageUrl);
                    if (absFile.exists() && absFile.isFile()) {
                        try (FileInputStream fis = new FileInputStream(absFile)) {
                            return IOUtils.toByteArray(fis);
                        }
                    } else {
                        System.err.println("无法找到图片文件: " + imageUrl);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("读取图片失败: " + imageUrl + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
