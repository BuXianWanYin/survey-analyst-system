package com.server.surveyanalystserver.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger配置
 */
@Configuration
@EnableOpenApi
@EnableKnife4j
public class SwaggerConfig {

    /**
     * 创建Swagger API文档配置
     * 配置API文档的基本信息、扫描路径、全局请求参数等
     * @return Swagger文档配置对象
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.server.surveyanalystserver.controller"))
                .paths(PathSelectors.any())
                .build()
                .globalRequestParameters(globalRequestParameters());
    }

    /**
     * 配置API文档基本信息
     * 包括标题、描述、版本、联系方式等
     * @return API信息对象
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("在线问卷调查与数据分析系统API文档")
                .description("在线问卷调查与数据分析系统接口文档\n" +
                        "使用说明：\n" +
                        "1. 登录接口（/api/auth/login）无需认证，可直接调用\n" +
                        "2. 其他接口需要在请求头中携带 JWT Token\n" +
                        "3. Token 格式：Bearer {token}\n" +
                        "4. 在 Swagger UI 右上角点击「Authorize」按钮，输入 Token 进行认证")
                .version("1.0.0")
                .contact(new Contact("开发团队", "", ""))
                .build();
    }

    /**
     * 配置全局请求参数
     * 为所有API接口添加JWT Token认证参数
     * @return 全局请求参数列表
     */
    private List<RequestParameter> globalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        RequestParameter tokenParameter = new RequestParameterBuilder()
                .name("Authorization")
                .description("JWT Token，格式：Bearer {token}。可通过 /api/auth/login 接口获取")
                .in(ParameterType.HEADER)
                .required(false)
                .build();
        parameters.add(tokenParameter);
        return parameters;
    }
}

