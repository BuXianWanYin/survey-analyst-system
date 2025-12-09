package com.server.surveyanalystserver.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.mapper.user.UserMapper;
import com.server.surveyanalystserver.service.OperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* com.server.surveyanalystserver.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logOperation(JoinPoint joinPoint, Object result) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String requestUrl = request.getRequestURI();
            String requestMethod = request.getMethod();

            // 跳过日志查询接口本身，避免循环记录
            if (requestUrl.contains("/admin/system/logs") || requestUrl.contains("/admin/system/overview")) {
                return;
            }

            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                String username = authentication.getName();
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                wrapper.eq(User::getAccount, username);
                User user = userMapper.selectOne(wrapper);
                if (user != null) {
                    userId = user.getId();
                }
            }

            // 获取操作类型和描述
            String operationType = getOperationType(requestMethod, requestUrl);
            String operationDesc = getOperationDesc(requestMethod, requestUrl, joinPoint);

            // 获取请求参数
            String requestParams = getRequestParams(joinPoint);

            // 获取响应结果（记录成功）
            String responseResult = "成功";

            // 获取IP地址
            String ipAddress = getIpAddress(request);

            // 创建操作日志
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setOperationType(operationType);
            log.setOperationDesc(operationDesc);
            log.setRequestUrl(requestUrl);
            log.setRequestMethod(requestMethod);
            log.setRequestParams(requestParams);
            log.setResponseResult(responseResult);
            log.setIpAddress(ipAddress);
            log.setCreateTime(LocalDateTime.now());

            // 异步保存日志
            operationLogService.saveLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务
            e.printStackTrace();
        }
    }

    private String getOperationType(String method, String url) {
        if (url.contains("/login")) {
            return "登录";
        } else if (url.contains("/logout")) {
            return "登出";
        } else if (url.contains("/register")) {
            return "注册";
        } else if (method.equals("GET")) {
            return "查询";
        } else if (method.equals("POST")) {
            return "新增";
        } else if (method.equals("PUT")) {
            return "更新";
        } else if (method.equals("DELETE")) {
            return "删除";
        }
        return "操作";
    }

    private String getOperationDesc(String method, String url, JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        if (url.contains("/login")) {
            return "用户登录";
        } else if (url.contains("/logout")) {
            return "用户登出";
        } else if (url.contains("/register")) {
            return "用户注册";
        } else if (url.contains("/user")) {
            if (method.equals("GET")) {
                return "查询用户信息";
            } else if (method.equals("POST")) {
                return "创建用户";
            } else if (method.equals("PUT")) {
                return "更新用户信息";
            } else if (method.equals("DELETE")) {
                return "删除用户";
            }
        } else if (url.contains("/survey")) {
            if (method.equals("GET")) {
                return "查询问卷";
            } else if (method.equals("POST")) {
                return "创建问卷";
            } else if (method.equals("PUT")) {
                return "更新问卷";
            } else if (method.equals("DELETE")) {
                return "删除问卷";
            }
        } else if (url.contains("/response")) {
            if (method.equals("POST")) {
                return "提交问卷填写";
            } else if (method.equals("GET")) {
                return "查询填写记录";
            }
        }
        
        return className + "." + methodName;
    }

    private String getRequestParams(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            
            // 过滤掉HttpServletRequest等对象
            java.util.List<Object> params = new java.util.ArrayList<>();
            for (Object arg : args) {
                if (arg != null && !(arg instanceof javax.servlet.http.HttpServletRequest) 
                    && !(arg instanceof javax.servlet.http.HttpServletResponse)) {
                    params.add(arg);
                }
            }
            
            if (params.isEmpty()) {
                return null;
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

