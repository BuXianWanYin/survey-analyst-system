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

    @Override
    public void exportSurveyData(Long surveyId, HttpServletResponse response) {
        try {
            Survey survey = surveyMapper.selectById(surveyId);
            if (survey == null) {
                throw new RuntimeException("问卷不存在");
            }

            // 设置响应头
            String fileName = URLEncoder.encode(survey.getTitle() + "_数据导出", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 获取所有填写记录
            LambdaQueryWrapper<Response> responseWrapper = new LambdaQueryWrapper<>();
            responseWrapper.eq(Response::getSurveyId, surveyId)
                          .eq(Response::getStatus, "COMPLETED")
                          .orderByDesc(Response::getSubmitTime);
            List<Response> responses = responseMapper.selectList(responseWrapper);

            // 获取所有题目
            List<Question> questions = questionMapper.selectList(
                    new LambdaQueryWrapper<Question>().eq(Question::getSurveyId, surveyId)
                                                      .orderByAsc(Question::getOrderNum)
            );

            // 构建导出数据
            List<Map<String, Object>> exportData = new ArrayList<>();
            for (Response resp : responses) {
                Map<String, Object> row = new HashMap<>();
                row.put("填写ID", resp.getId());
                row.put("提交时间", resp.getSubmitTime() != null ? 
                        resp.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
                row.put("设备类型", resp.getDeviceType() != null ? resp.getDeviceType() : "");
                row.put("填写时长(秒)", resp.getDuration() != null ? resp.getDuration() : "");

                // 获取该填写记录的所有答案
                List<Answer> answers = answerMapper.selectList(
                        new LambdaQueryWrapper<Answer>().eq(Answer::getResponseId, resp.getId())
                );

                // 按题目组织答案
                Map<Long, List<Answer>> answerMap = answers.stream()
                        .collect(Collectors.groupingBy(Answer::getQuestionId));

                // 为每个题目添加答案列
                for (Question question : questions) {
                    List<Answer> questionAnswers = answerMap.getOrDefault(question.getId(), new ArrayList<>());
                    String answerText = questionAnswers.stream()
                            .map(answer -> {
                                if (answer.getOptionId() != null) {
                                    Option option = optionMapper.selectById(answer.getOptionId());
                                    return option != null ? option.getContent() : "";
                                } else if (answer.getContent() != null) {
                                    return answer.getContent();
                                }
                                return "";
                            })
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining("; "));
                    row.put(question.getTitle(), answerText);
                }

                exportData.add(row);
            }

            // 使用EasyExcel导出
            EasyExcel.write(response.getOutputStream(), Map.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("问卷数据")
                    .doWrite(exportData);

        } catch (IOException e) {
            throw new RuntimeException("导出失败", e);
        }
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
