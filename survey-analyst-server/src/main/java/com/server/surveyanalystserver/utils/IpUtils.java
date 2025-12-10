package com.server.surveyanalystserver.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 */
public class IpUtils {
    
    /**
     * 获取客户端IP地址
     * @param request HTTP请求
     * @return IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && !ip.isEmpty()) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            // 处理IPv6的localhost地址
            if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1") || ip.equals("[::1]")) {
                return "127.0.0.1";
            }
            
            // 处理IPv4映射的IPv6地址
            if (ip.startsWith("::ffff:") || ip.startsWith("[::ffff:")) {
                String ipv4 = ip.replace("::ffff:", "").replace("[", "").replace("]", "");
                if (ipv4.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                    return ipv4;
                }
            }
        }
        
        return ip != null ? ip : "unknown";
    }
}

