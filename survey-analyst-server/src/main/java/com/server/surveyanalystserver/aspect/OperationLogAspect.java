package com.server.surveyanalystserver.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.surveyanalystserver.entity.OperationLog;
import com.server.surveyanalystserver.entity.User;
import com.server.surveyanalystserver.mapper.UserMapper;
import com.server.surveyanalystserver.service.OperationLogService;
import com.server.surveyanalystserver.utils.IpUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 定义切点：拦截所有Controller包下的方法
     */
    @Pointcut("execution(* com.server.surveyanalystserver.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 记录操作日志
     * 在Controller方法执行成功后，自动记录操作日志，包括用户信息、操作类型、请求参数、IP地址等
     * @param joinPoint 连接点，包含被拦截方法的信息
     * @param result 方法返回值
     */
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
            
            // 跳过登录接口，因为登录接口已经在AuthController中手动记录了日志
            if (requestUrl.contains("/auth/login")) {
                return;
            }

            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                String username = authentication.getName();
                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
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
            String ipAddress = IpUtils.getIpAddress(request);

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

    /**
     * 根据HTTP方法和URL获取操作类型
     * @param method HTTP请求方法（GET、POST、PUT、DELETE等）
     * @param url 请求URL
     * @return 操作类型（登录、登出、注册、查询、新增、更新、删除等）
     */
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

    /**
     * 根据HTTP方法、URL和连接点获取操作描述
     * 根据URL路径和HTTP方法生成详细的操作描述信息
     * @param method HTTP请求方法
     * @param url 请求URL
     * @param joinPoint 连接点对象
     * @return 操作描述信息
     */
    private String getOperationDesc(String method, String url, JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // 认证相关
        if (url.contains("/auth/login")) {
            return "用户登录系统";
        } else if (url.contains("/auth/register")) {
            return "用户注册账号";
        } else if (url.contains("/user/logout")) {
            return "用户退出登录";
        } else if (url.contains("/user/current")) {
            return "获取当前用户信息";
        } else if (url.contains("/user/info")) {
            return "更新个人用户信息";
        } else if (url.contains("/user/password")) {
            return "修改用户密码";
        }
        // 管理员-用户管理
        else if (url.contains("/admin/user/list")) {
            return "管理员查询用户列表";
        } else if (url.contains("/admin/user/") && url.matches(".*/admin/user/\\d+$") && method.equals("GET")) {
            return "管理员查看用户详情";
        } else if (url.contains("/admin/user") && method.equals("POST")) {
            return "管理员创建新用户";
        } else if (url.contains("/admin/user/") && url.contains("/status")) {
            return "管理员修改用户状态（启用/禁用）";
        } else if (url.contains("/admin/user/") && method.equals("PUT") && !url.contains("/status")) {
            return "管理员更新用户信息";
        } else if (url.contains("/admin/user/") && method.equals("DELETE")) {
            return "管理员删除用户";
        } else if (url.contains("/admin/user/statistics")) {
            return "管理员查看用户统计数据";
        }
        // 管理员-问卷管理
        else if (url.contains("/admin/survey/list")) {
            return "管理员查询问卷列表";
        } else if (url.contains("/admin/survey/") && url.matches(".*/admin/survey/\\d+$") && method.equals("GET")) {
            return "管理员查看问卷详情";
        } else if (url.contains("/admin/survey/") && url.contains("/status")) {
            return "管理员修改问卷状态";
        } else if (url.contains("/admin/survey/") && method.equals("DELETE")) {
            return "管理员删除问卷";
        } else if (url.contains("/admin/survey/statistics")) {
            return "管理员查看问卷统计数据";
        }
        // 管理员-模板管理
        else if (url.contains("/admin/template/page")) {
            return "管理员查询公共模板列表";
        } else if (url.contains("/admin/template/details")) {
            return "管理员查看公共模板详情";
        } else if (url.contains("/admin/template/create")) {
            return "管理员创建公共模板";
        } else if (url.contains("/admin/template/update")) {
            return "管理员更新公共模板";
        } else if (url.contains("/admin/template/delete")) {
            return "管理员删除公共模板";
        }
        // 管理员-数据管理
        else if (url.contains("/admin/data/response/list")) {
            return "管理员查询填写记录列表";
        } else if (url.contains("/admin/data/response/") && method.equals("GET")) {
            return "管理员查看填写记录详情";
        } else if (url.contains("/admin/data/response/") && method.equals("DELETE")) {
            return "管理员删除填写记录";
        } else if (url.contains("/admin/data/statistics")) {
            return "管理员查看数据统计数据";
        }
        // 管理员-系统管理
        else if (url.contains("/admin/system/overview")) {
            return "管理员查看系统概览数据";
        } else if (url.contains("/admin/system/logs")) {
            return "管理员查询系统操作日志";
        }
        // 用户-问卷管理
        else if (url.contains("/survey") && method.equals("GET") && url.contains("/list")) {
            return "用户查询我的问卷列表";
        } else if (url.contains("/survey") && method.equals("GET") && url.matches(".*/survey/\\d+$")) {
            return "用户查看问卷详情";
        } else if (url.contains("/survey") && method.equals("POST") && !url.contains("/publish") && !url.contains("/stop") && !url.contains("/use-template")) {
            return "用户创建新问卷";
        } else if (url.contains("/survey") && method.equals("PUT")) {
            return "用户更新问卷信息";
        } else if (url.contains("/survey") && method.equals("DELETE")) {
            return "用户删除问卷";
        } else if (url.contains("/survey") && url.contains("/publish")) {
            return "用户发布问卷";
        } else if (url.contains("/survey") && url.contains("/stop")) {
            return "用户停止发布问卷";
        } else if (url.contains("/survey") && url.contains("/use-template/create")) {
            return "用户使用模板创建问卷";
        } else if (url.contains("/survey") && url.contains("/verify-password")) {
            return "用户验证问卷访问密码";
        }
        // 用户-问卷填写
        else if (url.contains("/response") && method.equals("POST") && !url.contains("/draft")) {
            return "用户提交问卷填写";
        } else if (url.contains("/response") && url.contains("/draft")) {
            return "用户保存问卷草稿";
        } else if (url.contains("/response") && method.equals("GET") && url.matches(".*/response/\\d+$")) {
            return "用户查看填写记录详情";
        } else if (url.contains("/response") && method.equals("GET") && url.contains("/list")) {
            return "用户查询填写记录列表";
        } else if (url.contains("/response") && url.contains("/count")) {
            return "用户查询问卷填写数量";
        }
        // 用户-表单模板
        else if (url.contains("/form/template/type/list")) {
            return "用户查询模板分类列表";
        } else if (url.contains("/form/template/page")) {
            return "用户查询模板列表";
        } else if (url.contains("/form/template/details")) {
            return "用户查看模板详情";
        } else if (url.contains("/form/template/create")) {
            return "用户创建模板";
        } else if (url.contains("/form/template/delete")) {
            return "用户删除模板";
        } else if (url.contains("/form/template/category/create")) {
            return "用户创建模板分类";
        } else if (url.contains("/form/template/category/update")) {
            return "用户更新模板分类";
        } else if (url.contains("/form/template/category/delete")) {
            return "用户删除模板分类";
        }
        // 用户-表单配置
        else if (url.contains("/form/config") && method.equals("POST")) {
            return "用户保存表单配置";
        } else if (url.contains("/form/config") && method.equals("GET")) {
            return "用户获取表单配置";
        } else if (url.contains("/form/item/batch")) {
            return "用户批量保存表单项";
        } else if (url.contains("/form/item/list")) {
            return "用户查询表单项列表";
        } else if (url.contains("/form/item/") && method.equals("DELETE")) {
            return "用户删除表单项";
        } else if (url.contains("/form/logic") && method.equals("POST")) {
            return "用户保存表单逻辑规则";
        } else if (url.contains("/form/logic") && method.equals("GET")) {
            return "用户获取表单逻辑规则";
        } else if (url.contains("/form/logic") && method.equals("DELETE")) {
            return "用户删除表单逻辑规则";
        } else if (url.contains("/form/theme") && method.equals("POST")) {
            return "用户保存表单主题";
        } else if (url.contains("/form/theme") && method.equals("GET")) {
            return "用户获取表单主题";
        } else if (url.contains("/form/theme") && method.equals("DELETE")) {
            return "用户删除表单主题";
        } else if (url.contains("/form/setting") && method.equals("POST")) {
            return "用户保存表单设置";
        } else if (url.contains("/form/setting") && method.equals("GET")) {
            return "用户获取表单设置";
        } else if (url.contains("/form/setting") && method.equals("DELETE")) {
            return "用户删除表单设置";
        }
        // 用户-表单数据
        else if (url.contains("/form/data") && method.equals("POST")) {
            return "用户保存表单数据";
        } else if (url.contains("/form/data") && method.equals("GET") && url.contains("/list")) {
            return "用户查询表单数据列表";
        } else if (url.contains("/form/data") && method.equals("GET") && url.matches(".*/form/data/\\d+$")) {
            return "用户查看表单数据详情";
        } else if (url.contains("/form/data") && method.equals("DELETE")) {
            return "用户删除表单数据";
        }
        // 用户-数据统计
        else if (url.contains("/statistics/survey/")) {
            return "用户查看问卷统计数据";
        } else if (url.contains("/statistics/question/")) {
            return "用户查看题目统计数据";
        } else if (url.contains("/statistics/option/")) {
            return "用户查看选项统计数据";
        } else if (url.contains("/statistics/trend")) {
            return "用户查看填写趋势统计";
        } else if (url.contains("/statistics/source")) {
            return "用户查看填写来源统计";
        } else if (url.contains("/statistics/device")) {
            return "用户查看设备类型统计";
        }
        // 用户-数据分析
        else if (url.contains("/analysis/cross")) {
            return "用户进行交叉分析";
        } else if (url.contains("/analysis/trend")) {
            return "用户进行趋势分析";
        }
        // 用户-数据导出
        else if (url.contains("/export/survey/") && url.contains("/data")) {
            return "用户导出问卷数据";
        } else if (url.contains("/export/survey/") && url.contains("/report")) {
            return "用户导出分析报告";
        }
        // 用户-文件管理
        else if (url.contains("/file/upload")) {
            return "用户上传文件";
        } else if (url.contains("/file/") && method.equals("GET")) {
            return "用户下载或查看文件";
        } else if (url.contains("/file/") && method.equals("DELETE")) {
            return "用户删除文件";
        }
        // 通用操作描述
        else if (url.contains("/user") || url.contains("/admin/user")) {
            if (method.equals("GET")) {
                return "查询用户相关信息";
            } else if (method.equals("POST")) {
                return "创建用户相关数据";
            } else if (method.equals("PUT")) {
                return "更新用户相关信息";
            } else if (method.equals("DELETE")) {
                return "删除用户相关数据";
            }
        } else if (url.contains("/survey") || url.contains("/admin/survey")) {
            if (method.equals("GET")) {
                return "查询问卷相关信息";
            } else if (method.equals("POST")) {
                return "创建问卷相关数据";
            } else if (method.equals("PUT")) {
                return "更新问卷相关信息";
            } else if (method.equals("DELETE")) {
                return "删除问卷相关数据";
            }
        } else if (url.contains("/response") || url.contains("/form/data")) {
            if (method.equals("GET")) {
                return "查询填写记录或表单数据";
            } else if (method.equals("POST")) {
                return "提交填写记录或保存表单数据";
            } else if (method.equals("DELETE")) {
                return "删除填写记录或表单数据";
            }
        } else if (url.contains("/template") || url.contains("/form/template")) {
            if (method.equals("GET")) {
                return "查询模板相关信息";
            } else if (method.equals("POST")) {
                return "创建或更新模板";
            } else if (method.equals("DELETE")) {
                return "删除模板";
            }
        } else if (url.contains("/statistics") || url.contains("/analysis")) {
            return "查询统计数据或进行数据分析";
        } else if (url.contains("/export")) {
            return "导出数据或报告";
        } else if (url.contains("/file")) {
            if (method.equals("POST")) {
                return "上传文件";
            } else if (method.equals("GET")) {
                return "下载或查看文件";
            } else if (method.equals("DELETE")) {
                return "删除文件";
            }
        }
        
        // 如果无法匹配，返回类名和方法名的中文描述
        String desc = className.replace("Controller", "");
        if (desc.contains("Admin")) {
            desc = desc.replace("Admin", "管理员");
        }
        if (desc.contains("User")) {
            desc = desc.replace("User", "用户");
        }
        if (desc.contains("Survey")) {
            desc = desc.replace("Survey", "问卷");
        }
        if (desc.contains("Template")) {
            desc = desc.replace("Template", "模板");
        }
        if (desc.contains("Response")) {
            desc = desc.replace("Response", "填写记录");
        }
        if (desc.contains("Form")) {
            desc = desc.replace("Form", "表单");
        }
        if (desc.contains("Statistics")) {
            desc = desc.replace("Statistics", "统计");
        }
        if (desc.contains("Analysis")) {
            desc = desc.replace("Analysis", "分析");
        }
        if (desc.contains("Export")) {
            desc = desc.replace("Export", "导出");
        }
        if (desc.contains("File")) {
            desc = desc.replace("File", "文件");
        }
        
        String action = "";
        if (method.equals("GET")) {
            action = "查询";
        } else if (method.equals("POST")) {
            action = "创建或保存";
        } else if (method.equals("PUT")) {
            action = "更新";
        } else if (method.equals("DELETE")) {
            action = "删除";
        } else {
            action = "操作";
        }
        
        return action + desc + "数据";
    }

    /**
     * 获取请求参数
     * 从连接点中提取请求参数，过滤掉HttpServletRequest等对象，处理大文件数据
     * @param joinPoint 连接点对象
     * @return 请求参数的JSON字符串，如果参数为空或处理失败则返回null
     */
    private String getRequestParams(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            
            // 过滤掉HttpServletRequest等对象
            List<Object> params = new ArrayList<>();
            for (Object arg : args) {
                if (arg != null && !(arg instanceof javax.servlet.http.HttpServletRequest) 
                    && !(arg instanceof javax.servlet.http.HttpServletResponse)) {
                    // 处理包含 base64 图片数据的 Map
                    Object processedArg = processLargeData(arg);
                    params.add(processedArg);
                }
            }
            
            if (params.isEmpty()) {
                return null;
            }
            
            String json = objectMapper.writeValueAsString(params);
            // 限制总长度，防止超过数据库字段限制（text 类型最大 65535 字节）
            int maxLength = 50000; // 预留一些空间
            if (json != null && json.length() > maxLength) {
                json = json.substring(0, maxLength) + "...[数据已截断]";
            }
            
            return json;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 处理包含大数据的参数
     * 将base64图片数据和长字符串替换为简短描述，避免日志数据过大
     * @param arg 待处理的参数对象
     * @return 处理后的参数对象
     */
    private Object processLargeData(Object arg) {
        if (arg instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) arg;
            Map<String, Object> processedMap = new HashMap<>(map);
            
            // 遍历 Map，查找并替换 base64 图片数据
            for (Map.Entry<String, Object> entry : processedMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) {
                    String strValue = (String) value;
                    // 检查是否是 base64 图片数据
                    if (strValue.startsWith("data:image") || strValue.startsWith("data:application")) {
                        entry.setValue("[图片/文件数据已省略，长度: " + strValue.length() + " 字符]");
                    } else if (strValue.length() > 1000) {
                        // 其他长字符串也截断
                        entry.setValue(strValue.substring(0, 1000) + "...[数据已截断，原长度: " + strValue.length() + " 字符]");
                    }
                } else if (value instanceof List) {
                    // 处理 List 中的 base64 数据
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) value;
                    List<Object> processedList = new ArrayList<>();
                    for (Object item : list) {
                        if (item instanceof String) {
                            String strItem = (String) item;
                            if (strItem.startsWith("data:image") || strItem.startsWith("data:application")) {
                                processedList.add("[图片/文件数据已省略，长度: " + strItem.length() + " 字符]");
                            } else if (strItem.length() > 1000) {
                                processedList.add(strItem.substring(0, 1000) + "...[数据已截断，原长度: " + strItem.length() + " 字符]");
                            } else {
                                processedList.add(item);
                            }
                        } else {
                            processedList.add(item);
                        }
                    }
                    entry.setValue(processedList);
                }
            }
            return processedMap;
        }
        return arg;
    }
}

