package com.server.surveyanalystserver.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
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
    private QuestionMapper questionMapper;

    @Autowired
    private OptionMapper optionMapper;

    @Autowired
    private ResponseMapper responseMapper;

    @Autowired
    private AnswerMapper answerMapper;

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

            // 设置响应头
            String fileName = URLEncoder.encode(survey.getTitle() + "_数据导出", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

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

            // 构建导出数据
            List<Map<String, Object>> exportData = new ArrayList<>();
            int index = 1;
            for (FormData formData : formDataList) {
                Map<String, Object> row = new HashMap<>();
                
                // 序号列
                row.put("序号", index++);
                
                // ID列（FormData的ID）
                row.put("ID", formData.getId());
                
                // 提交时间列
                row.put("提交时间", formData.getCreateTime() != null ? 
                        formData.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                
                // 所用时间列（completeTime是毫秒，转换为秒）
                if (formData.getCompleteTime() != null) {
                    row.put("所用时间(秒)", formData.getCompleteTime() / 1000.0);
                } else {
                    row.put("所用时间(秒)", "");
                }
                
                // 来自IP列
                row.put("来自IP", formData.getSubmitRequestIp() != null ? formData.getSubmitRequestIp() : "");
                
                // 用户ID列（FormData没有userId，尝试从Response中查找）
                Long userId = null;
                if (formData.getCreateTime() != null) {
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
                row.put("用户ID", userId != null ? userId : "");

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
                        row.put(label, displayValue);
                    }
                } else {
                    // 如果没有数据，所有题目列都为空
                    for (FormItem item : inputFormItems) {
                        row.put(item.getLabel(), "");
                    }
                }

                exportData.add(row);
            }

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), Map.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("问卷数据")
                    .doWrite(exportData);

        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
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
                // 文件上传，返回文件URL列表
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
                                        return name + "(" + url + ")";
                                    }
                                    return name;
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
                    String name = (String) fileMap.getOrDefault("name", "文件");
                    String url = (String) fileMap.getOrDefault("url", fileMap.getOrDefault("rawUrl", ""));
                    return url != null && !url.isEmpty() ? name + "(" + url + ")" : name;
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
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取问卷统计
            Map<String, Object> surveyStats = statisticsService.getSurveyStatistics(surveyId);

            // 获取所有题目
            List<Question> questions = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, surveyId)
                                                      .orderByAsc(Question::getOrderNum)
            );

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

            // 题目统计
            for (Question question : questions) {
                Map<String, Object> questionStat = statisticsService.getQuestionStatistics(question.getId());
                
                Map<String, Object> row = new HashMap<>();
                row.put("统计项", "题目统计");
                row.put("题目", question.getTitle());
                row.put("题型", question.getType());

                if (questionStat.containsKey("optionStats")) {
                    List<Map<String, Object>> optionStats = (List<Map<String, Object>>) questionStat.get("optionStats");
                    for (Map<String, Object> optionStat : optionStats) {
                        Map<String, Object> optionRow = new HashMap<>(row);
                        optionRow.put("选项", optionStat.get("optionContent"));
                        optionRow.put("选择人数", optionStat.get("count"));
                        optionRow.put("比例(%)", optionStat.get("percentage"));
                        exportData.add(optionRow);
                    }
                } else {
                    row.put("有效答案数", questionStat.get("validAnswers"));
                    row.put("总答案数", questionStat.get("totalAnswers"));
                    exportData.add(row);
                }
            }

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), Map.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("统计数据")
                    .doWrite(exportData);

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
            response.setContentType("application/pdf");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".pdf");

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

            // 题目统计
            Paragraph questionTitle = new Paragraph("二、题目统计")
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(10);
            document.add(questionTitle);

            // 获取所有题目
            List<Question> questions = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, surveyId)
                                                      .orderByAsc(Question::getOrderNum)
            );

            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                Map<String, Object> questionStat = statisticsService.getQuestionStatistics(question.getId());

                Paragraph qTitle = new Paragraph((i + 1) + ". " + question.getTitle())
                        .setFontSize(14)
                        .setBold()
                        .setMarginTop(15)
                        .setMarginBottom(5);
                document.add(qTitle);

                if (questionStat.containsKey("optionStats")) {
                    List<Map<String, Object>> optionStats = (List<Map<String, Object>>) questionStat.get("optionStats");
                    float[] optionColumnWidths = {40, 30, 30};
                    Table optionTable = new Table(optionColumnWidths);
                    optionTable.addCell(createCell("选项", true));
                    optionTable.addCell(createCell("选择人数", true));
                    optionTable.addCell(createCell("比例(%)", true));
                    
                    for (Map<String, Object> optionStat : optionStats) {
                        optionTable.addCell(createCell(String.valueOf(optionStat.get("optionContent")), false));
                        optionTable.addCell(createCell(String.valueOf(optionStat.get("count")), false));
                        optionTable.addCell(createCell(String.valueOf(optionStat.get("percentage")), false));
                    }
                    document.add(optionTable);
                } else {
                    Paragraph statText = new Paragraph("有效答案数：" + questionStat.get("validAnswers") + 
                                                       "，总答案数：" + questionStat.get("totalAnswers"))
                            .setFontSize(12)
                            .setMarginBottom(10);
                    document.add(statText);
                }
            }

            // 关闭文档
            document.close();

        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    /**
     * 创建PDF表格单元格
     */
    private com.itextpdf.layout.element.Cell createCell(String text, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text))
                .setPadding(5);
        
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBold();
        }
        
        return cell;
    }
}
