package com.server.surveyanalystserver.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.surveyanalystserver.utils.ImageExcelWriteHandler;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.server.surveyanalystserver.entity.*;
import com.server.surveyanalystserver.mapper.*;
import com.server.surveyanalystserver.service.ExportService;
import com.server.surveyanalystserver.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据导出Service实现类
 */
@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private SurveyMapper surveyMapper;

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private FormDataMapper formDataMapper;

    @Autowired
    private FormItemMapper formItemMapper;

    @Autowired
    private FormConfigMapper formConfigMapper;

    @Override
    public void exportSurveyData(Long surveyId, HttpServletResponse response) {
        try {
            Survey survey = surveyMapper.selectById(surveyId);
            if (survey == null) {
                throw new RuntimeException("问卷不存在");
            }

            // 获取表单配置
            FormConfig formConfig = formConfigMapper.selectOne(
                    new LambdaQueryWrapper<FormConfig>().eq(FormConfig::getSurveyId, surveyId)
            );
            if (formConfig == null) {
                throw new RuntimeException("表单配置不存在");
            }

            String formKey = formConfig.getFormKey();

            // 设置响应头（Excel是二进制文件）
            String fileName = URLEncoder.encode(survey.getTitle() + "_数据导出", "UTF-8")
                    .replaceAll("\\+", "%20");
            
            // 重置响应，清除可能已设置的header和缓冲区
            response.reset();
            response.resetBuffer(); // 重置缓冲区，确保没有数据被写入
            
            // 设置Content-Type（二进制文件，明确不设置字符编码）
            // 使用setHeader而不是setContentType，避免触发字符编码过滤器
            response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            // 获取输出流（必须在设置header之后获取）
            OutputStream outputStream = response.getOutputStream();

            // 获取所有表单数据
            LambdaQueryWrapper<FormData> formDataWrapper = new LambdaQueryWrapper<>();
            formDataWrapper.eq(FormData::getFormKey, formKey)
                          .orderByDesc(FormData::getCreateTime);
            List<FormData> formDataList = formDataMapper.selectList(formDataWrapper);

            // 获取所有表单项（排除展示类组件）
            List<FormItem> formItems = formItemMapper.selectByFormKey(formKey);
            // 过滤掉展示类组件
            List<FormItem> inputFormItems = formItems.stream()
                    .filter(item -> {
                        String type = item.getType();
                        return !"DIVIDER".equals(type) && 
                               !"IMAGE".equals(type) && 
                               !"IMAGE_CAROUSEL".equals(type) && 
                               !"DESC_TEXT".equals(type);
                    })
                    .sorted(Comparator.comparing(FormItem::getSort, Comparator.nullsLast(Long::compareTo)))
                    .collect(Collectors.toList());

            // 识别图片列（IMAGE_UPLOAD、SIGN_PAD类型的字段，以及UPLOAD类型中的图片文件）
            Set<String> imageColumns = new HashSet<>();
            for (FormItem item : inputFormItems) {
                if ("IMAGE_UPLOAD".equals(item.getType()) || "SIGN_PAD".equals(item.getType())) {
                    imageColumns.add(item.getLabel());
                } else if ("UPLOAD".equals(item.getType())) {
                    // UPLOAD类型也需要检查，如果包含图片文件则加入图片列
                    imageColumns.add(item.getLabel());
                }
            }

            // 构建固定的列名列表（确保所有行都有相同的列）
            List<String> columnNames = new ArrayList<>();
            columnNames.add("序号");
            columnNames.add("ID");
            columnNames.add("提交时间");
            columnNames.add("所用时间(秒)");
            columnNames.add("来自IP");
            columnNames.add("用户ID");
            // 添加所有表单项的标签作为列名
            for (FormItem item : inputFormItems) {
                columnNames.add(item.getLabel());
            }

            // 构建导出数据
            List<Map<String, Object>> exportData = new ArrayList<>();
            int index = 1;
            for (FormData formData : formDataList) {
                Map<String, Object> row = new LinkedHashMap<>(); // 使用LinkedHashMap保持顺序
                
                // 初始化所有列为空值，确保所有行都有相同的列
                for (String columnName : columnNames) {
                    row.put(columnName, "");
                }
                
                // 序号列
                row.put("序号", index++);
                
                // ID列（FormData的ID）
                row.put("ID", formData.getId());
                
                // 提交时间列
                row.put("提交时间", formData.getCreateTime() != null ? 
                        formData.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                
                // 所用时间列（优先使用completeTime，如果没有则通过startTime和createTime计算）
                if (formData.getCompleteTime() != null) {
                    row.put("所用时间(秒)", formData.getCompleteTime());
                } else if (formData.getStartTime() != null && formData.getCreateTime() != null) {
                    long durationSeconds = java.time.Duration.between(formData.getStartTime(), formData.getCreateTime()).getSeconds();
                    row.put("所用时间(秒)", durationSeconds);
                } else {
                    row.put("所用时间(秒)", "");
                }
                
                // 来自IP列
                row.put("来自IP", formData.getSubmitRequestIp() != null ? formData.getSubmitRequestIp() : "");
                
                // 用户ID列（FormData没有userId，尝试从Response中查找）
                Long userId = null;
                if (formData.getCreateTime() != null && formData.getSubmitRequestIp() != null) {
                    // 尝试通过提交时间和IP匹配Response来获取userId
                    LambdaQueryWrapper<Response> respWrapper = new LambdaQueryWrapper<>();
                    respWrapper.eq(Response::getSurveyId, surveyId)
                              .eq(Response::getIpAddress, formData.getSubmitRequestIp())
                              .between(Response::getSubmitTime, 
                                      formData.getCreateTime().minusSeconds(5), 
                                      formData.getCreateTime().plusSeconds(5))
                              .last("LIMIT 1");
                    Response matchedResponse = responseMapper.selectOne(respWrapper);
                    if (matchedResponse != null) {
                        userId = matchedResponse.getUserId();
                    }
                }
                row.put("用户ID", userId != null ? userId.toString() : "");

                // 动态渲染问卷的选项名称列
                Map<String, Object> originalData = formData.getOriginalData();
                if (originalData != null) {
                    for (FormItem item : inputFormItems) {
                        Object value = originalData.get(item.getFormItemId());
                        String label = item.getLabel();
                        
                        if (value == null) {
                            row.put(label, "");
                            continue;
                        }
                        
                        // 对于图片列，直接保存原始值（用于图片处理器）
                        if ("IMAGE_UPLOAD".equals(item.getType()) || "SIGN_PAD".equals(item.getType())) {
                            // 图片列保存原始值，让ImageExcelWriteHandler处理
                            // 如果是List，转换为分号分隔的URL字符串
                            if (value instanceof List) {
                                List<?> files = (List<?>) value;
                                String imageUrls = files.stream()
                                        .map(f -> {
                                            if (f instanceof Map) {
                                                @SuppressWarnings("unchecked")
                                                Map<String, Object> fileMap = (Map<String, Object>) f;
                                                String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                                                return url != null && !url.isEmpty() ? url : "";
                                            } else if (f instanceof String) {
                                                return (String) f;
                                            }
                                            return String.valueOf(f);
                                        })
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.joining(";"));
                                row.put(label, imageUrls);
                            } else if (value instanceof String) {
                                // 直接是字符串（可能是Base64或URL）
                                row.put(label, (String) value);
                            } else {
                                row.put(label, String.valueOf(value));
                            }
                            continue;
                        }
                        
                        // 对于UPLOAD类型，需要判断是否是图片文件
                        if ("UPLOAD".equals(item.getType())) {
                            // 判断是否是图片文件，如果是图片则保存URL用于嵌入，否则显示完整URL
                            if (value instanceof List) {
                                List<?> files = (List<?>) value;
                                StringBuilder imageUrls = new StringBuilder();
                                StringBuilder fileUrls = new StringBuilder();
                                
                                for (Object f : files) {
                                    String url = "";
                                    String name = "";
                                    boolean isImage = false;
                                    
                                    if (f instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> fileMap = (Map<String, Object>) f;
                                        url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                                        name = (String) fileMap.getOrDefault("name", "");
                                        
                                        // 判断是否是图片文件
                                        if (url != null && !url.isEmpty()) {
                                            String urlLower = url.toLowerCase();
                                            isImage = urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg") || 
                                                     urlLower.endsWith(".png") || urlLower.endsWith(".gif") ||
                                                     urlLower.endsWith(".bmp") || urlLower.endsWith(".webp");
                                        }
                                    } else if (f instanceof String) {
                                        url = (String) f;
                                        String urlLower = url.toLowerCase();
                                        isImage = urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg") || 
                                                 urlLower.endsWith(".png") || urlLower.endsWith(".gif") ||
                                                 urlLower.endsWith(".bmp") || urlLower.endsWith(".webp");
                                    }
                                    
                                    if (url != null && !url.isEmpty()) {
                                        if (isImage) {
                                            // 图片文件，保存URL用于嵌入
                                            if (imageUrls.length() > 0) imageUrls.append(";");
                                            imageUrls.append(url);
                                        } else {
                                            // 非图片文件，构建完整URL显示
                                            if (fileUrls.length() > 0) fileUrls.append("; ");
                                            String fullUrl = buildFullUrl(url);
                                            if (name != null && !name.isEmpty()) {
                                                fileUrls.append(name).append("(").append(fullUrl).append(")");
                                            } else {
                                                fileUrls.append(fullUrl);
                                            }
                                        }
                                    }
                                }
                                
                                // 如果包含图片，保存到图片列；如果包含非图片文件，格式化显示
                                if (imageUrls.length() > 0) {
                                    row.put(label, imageUrls.toString());
                                } else if (fileUrls.length() > 0) {
                                    row.put(label, fileUrls.toString());
                                } else {
                                    row.put(label, "");
                                }
                            } else if (value instanceof String) {
                                String url = (String) value;
                                String urlLower = url.toLowerCase();
                                boolean isImage = urlLower.endsWith(".jpg") || urlLower.endsWith(".jpeg") || 
                                                urlLower.endsWith(".png") || urlLower.endsWith(".gif") ||
                                                urlLower.endsWith(".bmp") || urlLower.endsWith(".webp");
                                if (isImage) {
                                    row.put(label, url); // 图片，用于嵌入
                                } else {
                                    row.put(label, buildFullUrl(url)); // 非图片，显示完整URL
                                }
                            } else {
                                row.put(label, String.valueOf(value));
                            }
                            continue;
                        }
                        
                        // 解析scheme获取配置
                        Map<String, Object> scheme = null;
                        try {
                            String schemeStr = item.getScheme();
                            if (schemeStr != null && !schemeStr.isEmpty()) {
                                scheme = new com.fasterxml.jackson.databind.ObjectMapper()
                                        .readValue(schemeStr, 
                                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                            }
                        } catch (Exception e) {
                            // 解析失败，使用默认值
                        }
                        
                        Map<String, Object> config = new HashMap<>();
                        if (scheme != null && scheme.containsKey("config")) {
                            Object configObj = scheme.get("config");
                            if (configObj instanceof Map) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> configMap = (Map<String, Object>) configObj;
                                config = configMap;
                            }
                        }
                        
                        // 根据组件类型格式化值
                        String displayValue = formatFormItemValue(item.getType(), value, config);
                        row.put(label, displayValue != null ? displayValue : "");
                    }
                }

                exportData.add(row);
            }

            // 如果没有数据，至少创建一个空行包含表头
            if (exportData.isEmpty()) {
                Map<String, Object> emptyRow = new LinkedHashMap<>();
                for (String columnName : columnNames) {
                    emptyRow.put(columnName, "");
                }
                exportData.add(emptyRow);
            }

            // 使用EasyExcel导出，构建表头
            // 将列名列表转换为 List<List<String>> 格式（EasyExcel需要的表头格式）
            List<List<String>> head = new ArrayList<>();
            for (String columnName : columnNames) {
                List<String> headColumn = new ArrayList<>();
                headColumn.add(columnName);
                head.add(headColumn);
            }
            
            // 创建图片写入处理器
            ImageExcelWriteHandler imageWriteHandler = new ImageExcelWriteHandler();
            imageWriteHandler.setImageColumns(imageColumns);
            
            // 将每行的Map数据传递给图片处理器
            for (int i = 0; i < exportData.size(); i++) {
                imageWriteHandler.setRowData(i, exportData.get(i));
            }

            // 将数据转换为 List<List<Object>> 格式
            // 对于图片列，写入单元格的文本为空或占位符（图片处理器会嵌入图片）
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : exportData) {
                List<Object> rowData = new ArrayList<>();
                for (String columnName : columnNames) {
                    Object value = row.get(columnName);
                    // 对于图片列，单元格文本为空（图片由处理器嵌入，不需要占位符）
                    if (imageColumns.contains(columnName)) {
                        // 图片列单元格文本设置为空，实际图片由ImageExcelWriteHandler嵌入
                        rowData.add("");
                    } else {
                        rowData.add(value != null ? value : "");
                    }
                }
                dataList.add(rowData);
            }

            // 使用EasyExcel导出（明确指定表头和数据）
            EasyExcel.write(outputStream)
                    .head(head)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(imageWriteHandler) // 启用图片写入
                    .sheet("问卷数据")
                    .doWrite(dataList);
            
            // 确保数据刷新到输出流
            outputStream.flush();

        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建完整URL（用于非图片文件的显示）
     */
    private String buildFullUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        // 如果已经是完整URL，直接返回
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        // 如果是相对路径，拼接服务器地址
        // 默认使用 localhost:8080，实际部署时可以从配置文件读取
        String baseUrl = "http://localhost:8080";
        if (url.startsWith("/")) {
            return baseUrl + url;
        } else {
            return baseUrl + "/" + url;
        }
    }
    
    /**
     * 格式化表单项值
     */
    private String formatFormItemValue(String type, Object value, Map<String, Object> config) {
        if (value == null) {
            return "";
        }
        
        switch (type) {
            case "RADIO":
            case "SELECT":
                // 单选或下拉，显示选项标签
                return getOptionLabel(value, config);
                
            case "CHECKBOX":
            case "IMAGE_SELECT":
                // 多选，显示多个选项标签
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    return values.stream()
                            .map(v -> getOptionLabel(v, config))
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining("、"));
                }
                return getOptionLabel(value, config);
                
            case "CASCADER":
                // 级联选择
                if (value instanceof List) {
                    List<?> values = (List<?>) value;
                    List<Map<String, Object>> options = (List<Map<String, Object>>) config.getOrDefault("options", new ArrayList<>());
                    return formatCascaderValue(values, options);
                }
                return String.valueOf(value);
                
            case "RATE":
                // 评分
                return value + "分";
                
            case "SIGN_PAD":
            case "IMAGE_UPLOAD":
                // 图片上传，返回图片URL列表
                if (value instanceof List) {
                    List<?> files = (List<?>) value;
                    return files.stream()
                            .map(f -> {
                                if (f instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> fileMap = (Map<String, Object>) f;
                                    String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                                    if (url == null || url.isEmpty()) {
                                        return String.valueOf(fileMap.getOrDefault("name", ""));
                                    }
                                    return url;
                                } else if (f instanceof String) {
                                    return (String) f;
                                }
                                return String.valueOf(f);
                            })
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining("; "));
                } else if (value instanceof String) {
                    return (String) value;
                } else if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fileMap = (Map<String, Object>) value;
                    String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                    return url != null && !url.isEmpty() ? url : "[图片]";
                }
                return String.valueOf(value);
                
            case "UPLOAD":
                // 文件上传，如果是图片则嵌入，否则显示完整URL
                // 这个分支只会在非图片列时执行（图片列已经在上面处理了）
                if (value instanceof List) {
                    List<?> files = (List<?>) value;
                    return files.stream()
                            .map(f -> {
                                if (f instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> fileMap = (Map<String, Object>) f;
                                    String name = (String) fileMap.getOrDefault("name", "");
                                    String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                                    if (url != null && !url.isEmpty()) {
                                        String fullUrl = buildFullUrl(url);
                                        if (name != null && !name.isEmpty()) {
                                            return name + "(" + fullUrl + ")";
                                        }
                                        return fullUrl;
                                    }
                                    return name;
                                } else if (f instanceof String) {
                                    return buildFullUrl((String) f);
                                }
                                return String.valueOf(f);
                            })
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining("; "));
                } else if (value instanceof String) {
                    return buildFullUrl((String) value);
                } else if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fileMap = (Map<String, Object>) value;
                    String name = (String) fileMap.getOrDefault("name", "文件");
                    String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                    if (url != null && !url.isEmpty()) {
                        String fullUrl = buildFullUrl(url);
                        return name + "(" + fullUrl + ")";
                    }
                    return name;
                }
                return String.valueOf(value);
                
            default:
                // 其他类型直接转为字符串
                if (value instanceof List) {
                    return ((List<?>) value).stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining("、"));
                }
                return String.valueOf(value);
        }
    }

    /**
     * 获取选项标签
     */
    @SuppressWarnings("unchecked")
    private String getOptionLabel(Object value, Map<String, Object> config) {
        Object optionsObj = config.getOrDefault("options", new ArrayList<>());
        if (!(optionsObj instanceof List)) {
            return String.valueOf(value);
        }
        List<Map<String, Object>> options = (List<Map<String, Object>>) optionsObj;
        for (Map<String, Object> option : options) {
            if (Objects.equals(option.get("value"), value)) {
                return String.valueOf(option.getOrDefault("label", value));
            }
        }
        return String.valueOf(value);
    }

    /**
     * 格式化级联选择值
     */
    @SuppressWarnings("unchecked")
    private String formatCascaderValue(List<?> values, List<Map<String, Object>> options) {
        List<String> labels = new ArrayList<>();
        List<Map<String, Object>> currentOptions = options;
        
        for (Object val : values) {
            boolean found = false;
            for (Map<String, Object> option : currentOptions) {
                if (Objects.equals(option.get("value"), val)) {
                    labels.add(String.valueOf(option.getOrDefault("label", val)));
                    if (option.containsKey("children")) {
                        Object childrenObj = option.get("children");
                        if (childrenObj instanceof List) {
                            currentOptions = (List<Map<String, Object>>) childrenObj;
                        } else {
                            currentOptions = new ArrayList<>();
                        }
                    } else {
                        currentOptions = new ArrayList<>();
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                labels.add(String.valueOf(val));
                break;
            }
        }
        
        return String.join(" / ", labels);
    }

    @Override
    public void exportStatistics(Long surveyId, HttpServletResponse response) {
        try {
            Survey survey = surveyMapper.selectById(surveyId);
            if (survey == null) {
                throw new RuntimeException("问卷不存在");
            }

            // 设置响应头
            String fileName = URLEncoder.encode(survey.getTitle() + "_统计数据", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.reset();
            response.resetBuffer(); // 重置缓冲区，确保没有数据被写入
            response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            OutputStream outputStream = response.getOutputStream();

            // 获取问卷统计
            Map<String, Object> surveyStats = statisticsService.getSurveyStatistics(surveyId);

            // 构建统计数据
            List<Map<String, Object>> exportData = new ArrayList<>();

            // 问卷概览
            Map<String, Object> overview = new HashMap<>();
            overview.put("统计项", "问卷概览");
            overview.put("总填写数", surveyStats.get("totalResponses"));
            overview.put("已完成", surveyStats.get("completedResponses"));
            overview.put("草稿数", surveyStats.get("draftResponses"));
            overview.put("有效率(%)", surveyStats.get("validRate"));
            exportData.add(overview);

            // 注意：题目统计功能已废弃，系统已迁移到表单系统
            // 如需导出题目统计，请使用基于form_item的导出方法

            // 使用EasyExcel导出
            EasyExcel.write(outputStream, Map.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("统计数据")
                    .doWrite(exportData);
            
            // 确保数据刷新到输出流
            outputStream.flush();

        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public void exportAnalysisReport(Long surveyId, HttpServletResponse response) {
        try {
            Survey survey = surveyMapper.selectById(surveyId);
            if (survey == null) {
                throw new RuntimeException("问卷不存在");
            }

            // 设置响应头
            String fileName = URLEncoder.encode(survey.getTitle() + "_分析报告", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.reset();
            response.resetBuffer(); // 重置缓冲区，确保没有数据被写入
            response.setHeader("Content-Type", "application/pdf");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".pdf");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            // 获取统计数据
            Map<String, Object> surveyStats = statisticsService.getSurveyStatistics(surveyId);

            // 使用iText生成PDF
            OutputStream outputStream = response.getOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 标题
            Paragraph title = new Paragraph(survey.getTitle() + " - 分析报告")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // 生成时间
            Paragraph date = new Paragraph("生成时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(date);

            // 问卷概览
            Paragraph overviewTitle = new Paragraph("一、问卷概览")
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(overviewTitle);

            // 创建统计表格
            float[] columnWidths = {50, 50};
            Table statsTable = new Table(columnWidths);
            statsTable.addCell(createCell("总填写数", true));
            statsTable.addCell(createCell(String.valueOf(surveyStats.get("totalResponses")), false));
            statsTable.addCell(createCell("已完成", true));
            statsTable.addCell(createCell(String.valueOf(surveyStats.get("completedResponses")), false));
            statsTable.addCell(createCell("草稿数", true));
            statsTable.addCell(createCell(String.valueOf(surveyStats.get("draftResponses")), false));
            statsTable.addCell(createCell("有效率(%)", true));
            statsTable.addCell(createCell(String.valueOf(surveyStats.get("validRate")), false));
            document.add(statsTable);

            // 注意：题目统计功能已废弃，系统已迁移到表单系统
            // 如需导出题目统计，请使用基于form_item的导出方法
            Paragraph note = new Paragraph("注意：题目统计功能已废弃，系统已迁移到表单系统")
                    .setFontSize(12)
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(note);

            // 关闭文档
            document.close();

        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    /**
     * 创建PDF表格单元格
     */
    private Cell createCell(String text, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new Cell()
                .add(new Paragraph(text))
                .setPadding(5);
        
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold();
        }
        
        return cell;
    }
}
