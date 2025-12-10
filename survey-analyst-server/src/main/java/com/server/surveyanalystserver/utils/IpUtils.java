package com.server.surveyanalystserver.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * IP地址工具类
 */
public class IpUtils {
    
    private static final Logger logger = Logger.getLogger(IpUtils.class.getName());
    
    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        // 优先从请求头获取IP（适用于经过代理的情况）
        String ip = request.getHeader("X-Forwarded-For");
        String source = "X-Forwarded-For";
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            source = "Proxy-Client-IP";
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            source = "WL-Proxy-Client-IP";
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            source = "X-Real-IP";
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            source = "HTTP_X_FORWARDED_FOR";
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_REAL_IP");
            source = "HTTP_X_REAL_IP";
        }
        
        // 如果所有请求头都没有，使用 getRemoteAddr()
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            source = "getRemoteAddr";
        }
        
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP，格式：client, proxy1, proxy2）
        if (ip != null && !ip.isEmpty()) {
            if (ip.contains(",")) {
                // 取第一个IP（客户端真实IP）
                ip = ip.split(",")[0].trim();
            }
            
            // 处理IPv6的localhost地址
            if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1") || ip.equals("[::1]")) {
                return "127.0.0.1";
            }
            
            // 处理IPv4映射的IPv6地址（格式：::ffff:192.168.1.1）
            if (ip.startsWith("::ffff:") || ip.startsWith("[::ffff:")) {
                String ipv4 = ip.replace("::ffff:", "").replace("[", "").replace("]", "");
                if (ipv4.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                    return ipv4;
                }
            }
            
            // 如果获取到的IP是服务器IP（192.168.31.33），说明可能是直接访问或代理未设置请求头
            // 这种情况下，如果 getRemoteAddr() 返回的是服务器IP，说明请求可能来自服务器本身
            // 或者代理没有正确设置 X-Forwarded-For 头
            if ("getRemoteAddr".equals(source) && ip.equals("192.168.31.33")) {
                logger.warning("获取到的IP是服务器IP，可能是代理未设置X-Forwarded-For头，或请求来自服务器本身");
            }
        }
        
        return ip != null ? ip : "unknown";
    }
}

