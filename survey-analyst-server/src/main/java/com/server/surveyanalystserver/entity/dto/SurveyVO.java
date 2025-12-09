package com.server.surveyanalystserver.entity.dto;

import com.server.surveyanalystserver.entity.Survey;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷VO（包含用户名）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "问卷VO")
public class SurveyVO extends Survey {

    @ApiModelProperty(value = "创建用户名")
    private String username;
}

