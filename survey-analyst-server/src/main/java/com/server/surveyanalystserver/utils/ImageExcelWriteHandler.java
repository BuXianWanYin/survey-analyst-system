package com.server.surveyanalystserver.utils;

import com.alibaba.excel.write.handler.WorkbookWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
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
            
            // 设置列宽（确保列足够宽以显示图片）
            int colWidth = sheet.getColumnWidth(colIndex);
            if (colWidth < 4000) {
                colWidth = 4000; // 设置列宽（约40个字符）
                sheet.setColumnWidth(colIndex, colWidth);
            }
            
            // 设置行高（确保行足够高以显示图片）
            double rowHeightPoints = row.getHeightInPoints();
            if (rowHeightPoints < 60) {
                rowHeightPoints = 60; // 最小行高60点
            }
            // 如果有多张图片，增加行高
            if (totalImages > 1) {
                rowHeightPoints = Math.max(rowHeightPoints, totalImages * 60.0); // 每张图片60点高度
            } else {
                rowHeightPoints = Math.max(rowHeightPoints, 60.0); // 单张图片60点高度
            }
            row.setHeightInPoints((float) rowHeightPoints);
            
            // 创建锚点，参考标准做法：col2=col1+1, row2=row1+1 让图片填充单元格
            // dx1, dy1, dx2, dy2 都设置为0，表示从单元格左上角到右下角
            ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            
            if (totalImages > 1) {
                // 多张图片：在单元格内垂直排列
                // 计算每张图片应该占用的高度（单位：EMU，1点 = 12700 EMU）
                double totalHeightEmu = rowHeightPoints * 12700;
                double imageHeightEmu = totalHeightEmu / totalImages;
                double topOffsetEmu = imageIndex * imageHeightEmu;
                
                // 设置锚点：使用 dx/dy 来指定图片在单元格内的垂直位置
                // col1, row1: 左上角单元格
                // col2, row2: 右下角单元格（设置为同一单元格，用 dx/dy 控制位置）
                anchor.setCol1(colIndex);
                anchor.setRow1(rowIndex);
                anchor.setCol2(colIndex);  // 同一列
                anchor.setRow2(rowIndex);  // 同一行
                
                // dx1, dy1: 左上角相对于单元格左上角的偏移
                // dx2, dy2: 右下角相对于单元格左上角的偏移
                anchor.setDx1(0);  // 左边对齐
                anchor.setDy1((int) topOffsetEmu);  // 垂直偏移
                anchor.setDx2(0);  // 右边对齐（需要根据列宽计算）
                anchor.setDy2((int) (topOffsetEmu + imageHeightEmu));  // 底部位置
                
                // 需要计算列宽的 EMU 值来设置 dx2
                // 列宽单位：1/256 字符宽度，1个字符宽度 ≈ 9525 EMU
                int cellWidthEmu = (int) (colWidth * 9525 / 256.0);
                anchor.setDx2(cellWidthEmu);
            } else {
                // 单张图片：填充整个单元格
                // 关键：col2 = col1 + 1, row2 = row1 + 1，dx/dy = 0
                // 这样图片会填充整个单元格，resize() 会自动调整
                anchor.setCol1(colIndex);
                anchor.setRow1(rowIndex);
                anchor.setCol2(colIndex + 1);  // 下一列（图片填充到单元格边界）
                anchor.setRow2(rowIndex + 1);  // 下一行（图片填充到单元格边界）
                
                // dx/dy 都设置为 0，让图片从单元格左上角开始，填充到右下角
                anchor.setDx1(0);
                anchor.setDy1(0);
                anchor.setDx2(0);
                anchor.setDy2(0);
            }

            // 插入图片
            Picture picture = drawing.createPicture(anchor, pictureIdx);
            
            // 控制图片大小：使用 resize(double scale) 方法控制缩放比例
            // 参数说明：
            // - resize() 或 resize(1.0): 完全填充锚点区域（可能变形）
            // - resize(0.9): 填充90%的区域（保持宽高比）
            // 我们需要让图片适应单元格，保持宽高比，不超出单元格
            // 先尝试获取图片原始尺寸来计算合适的缩放比例
            
            try {
                // 获取图片原始尺寸
                BufferedImage img = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
                if (img == null) {
                    throw new Exception("无法读取图片");
                }
                int imgWidth = img.getWidth();
                int imgHeight = img.getHeight();
                
                // 计算单元格尺寸（像素）
                // 列宽：1字符 ≈ 7像素，列宽单位是 1/256 字符
                double cellWidthPx = (colWidth / 256.0) * 7.0;
                // 行高：1点 ≈ 1.33像素
                double cellHeightPx = rowHeightPoints * 1.33;
                
                // 如果是多张图片，每张图片的高度需要除以总数
                if (totalImages > 1) {
                    cellHeightPx = cellHeightPx / totalImages;
                }
                
                // 计算缩放比例（保持宽高比，取较小的比例以确保图片完全在单元格内）
                double scaleX = cellWidthPx / imgWidth;
                double scaleY = cellHeightPx / imgHeight;
                double scale = Math.min(scaleX, scaleY);  // 取较小值，确保图片不会超出单元格
                
                // 应用缩放（乘以0.95留一点边距）
                picture.resize(scale * 0.95);
                
                System.out.println("图片缩放: 原始尺寸=" + imgWidth + "x" + imgHeight + 
                    ", 单元格尺寸=" + cellWidthPx + "x" + cellHeightPx + 
                    ", 缩放比例=" + (scale * 0.95));
            } catch (Exception e) {
                // 如果无法读取图片尺寸，使用默认的 resize() 方法
                System.err.println("无法读取图片尺寸，使用默认缩放: " + e.getMessage());
                // resize() 会填充锚点区域，但可能保持宽高比
                picture.resize(1.0);
            }
            
            System.out.println("成功插入图片到 row=" + rowIndex + ", col=" + colIndex + 
                ", imageIndex=" + imageIndex + "/" + totalImages +
                ", colWidth=" + colWidth + ", rowHeight=" + rowHeightPoints);
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
