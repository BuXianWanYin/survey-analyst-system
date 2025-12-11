package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.service.SurveyPublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 问卷发布与推广控制器
 */
@RestController
@RequestMapping("/api/survey")
@Api(tags = "问卷发布与推广")
public class SurveyPublishController {

    @Autowired
    private SurveyPublishService surveyPublishService;

    @ApiOperation(value = "获取问卷链接", notes = "生成问卷访问链接")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}/link")
    public Result<String> getSurveyLink(@PathVariable Long id) {
        String link = surveyPublishService.generateSurveyLink(id);
        return Result.success("获取成功", link);
    }

    @ApiOperation(value = "获取问卷二维码", notes = "生成问卷二维码（Base64格式）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}/qrcode")
    public Result<String> getQRCode(@PathVariable Long id) {
        String qrcode = surveyPublishService.generateQRCode(id);
        return Result.success("获取成功", qrcode);
    }

    @ApiOperation(value = "获取嵌入代码", notes = "生成问卷嵌入代码（iframe）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}/embed-code")
    public Result<String> getEmbedCode(@PathVariable Long id) {
        String embedCode = surveyPublishService.generateEmbedCode(id);
        return Result.success("获取成功", embedCode);
    }
}

