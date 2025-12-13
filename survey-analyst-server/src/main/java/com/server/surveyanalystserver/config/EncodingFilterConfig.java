package com.server.surveyanalystserver.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 字符编码过滤器配置
 * 排除导出接口的字符编码处理，避免破坏二进制文件
 */
@Configuration
public class EncodingFilterConfig {

    /**
     * 配置自定义字符编码过滤器
     * 为所有接口设置UTF-8编码，但排除导出接口以避免破坏二进制文件
     * @return 过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> customCharacterEncodingFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        
        OncePerRequestFilter encodingFilter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                String requestPath = request.getRequestURI();
                
                // 排除导出接口，避免对二进制文件（Excel）应用字符编码
                if (requestPath != null && requestPath.startsWith("/api/export")) {
                    // 对于导出接口，直接传递，不设置字符编码
                    filterChain.doFilter(request, response);
                } else {
                    // 对于其他接口，设置UTF-8编码
                    request.setCharacterEncoding("UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    filterChain.doFilter(request, response);
                }
            }
        };
        
        registration.setFilter(encodingFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE); // 确保在其他过滤器之前执行
        
        return registration;
    }
}
