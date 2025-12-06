package com.server.surveyanalystserver.controller.user;

import com.server.surveyanalystserver.common.Result;
import com.server.surveyanalystserver.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/api/file")
@Api(tags = "文件管理")
public class FileController {

    @Autowired
    private FileService fileService;

    @ApiOperation(value = "上传文件", notes = "上传文件（图片、文档等）")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileUrl = fileService.uploadFile(file);
        return Result.success("上传成功", fileUrl);
    }

    @ApiOperation(value = "删除文件", notes = "删除文件")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @DeleteMapping
    public Result<Void> deleteFile(@RequestParam String fileUrl) {
        boolean success = fileService.deleteFile(fileUrl);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    @GetMapping("/info")
    public Result<String> getFileInfo(@RequestParam String fileUrl) {
        String fileInfo = fileService.getFileInfo(fileUrl);
        return Result.success("获取成功", fileInfo);
    }
}

