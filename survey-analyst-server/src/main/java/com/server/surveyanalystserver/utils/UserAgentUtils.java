package com.server.surveyanalystserver.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * User-Agent 解析工具类
 */
public class UserAgentUtils {
    
    /**
     * 解析 User-Agent 获取浏览器信息
     * @param userAgent User-Agent 字符串
     * @return 浏览器名称
     */
    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("edg")) {
            return "Edge";
        } else if (ua.contains("chrome") && !ua.contains("edg")) {
            return "Chrome";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("opera") || ua.contains("opr")) {
            return "Opera";
        } else if (ua.contains("msie") || ua.contains("trident")) {
            return "IE";
        } else if (ua.contains("micromessenger")) {
            return "WeChat";
        } else if (ua.contains("qqbrowser")) {
            return "QQ Browser";
        } else if (ua.contains("ucbrowser")) {
            return "UC Browser";
        }
        
        return "Unknown";
    }
    
    /**
     * 解析 User-Agent 获取操作系统信息
     * @param userAgent User-Agent 字符串
     * @return 操作系统名称
     */
    public static String getOS(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("windows")) {
            // Windows 11 在 User-Agent 中通常显示为 "Windows NT 10.0"，与 Windows 10 相同
            // 由于无法准确区分，统一显示为 Windows 10/11
            if (ua.contains("windows 11")) {
                return "Windows 11";
            }
            if (ua.contains("windows nt 10.0") || ua.contains("windows 10")) {
                // Windows 10 和 Windows 11 的 User-Agent 都是 NT 10.0，无法区分，统一显示
                return "Windows 10/11";
            } else if (ua.contains("windows nt 6.3") || ua.contains("windows 8.1")) {
                return "Windows 8.1";
            } else if (ua.contains("windows nt 6.2") || ua.contains("windows 8")) {
                return "Windows 8";
            } else if (ua.contains("windows nt 6.1") || ua.contains("windows 7")) {
                return "Windows 7";
            } else if (ua.contains("windows nt 6.0")) {
                return "Windows Vista";
            } else if (ua.contains("windows nt 5.1") || ua.contains("windows xp")) {
                return "Windows XP";
            }
            return "Windows";
        } else if (ua.contains("mac os x") || ua.contains("macintosh")) {
            return "macOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return "iOS";
        } else if (ua.contains("unix")) {
            return "Unix";
        }
        
        return "Unknown";
    }
    
    /**
     * 解析 User-Agent 获取设备类型
     * @param userAgent User-Agent 字符串
     * @return 设备类型：PC 或 MOBILE
     */
    public static String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "PC";
        }
        
        String ua = userAgent.toLowerCase();
        
        // 检查移动设备
        if (ua.contains("mobile") || ua.contains("android") || 
            ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod") ||
            ua.contains("blackberry") || ua.contains("windows phone")) {
            return "MOBILE";
        }
        
        return "PC";
    }
    
    /**
     * 解析 User-Agent 获取完整的 UA 信息（JSON格式）
     * @param userAgent User-Agent 字符串
     * @return UA 信息 Map
     */
    public static Map<String, Object> getUaInfo(String userAgent) {
        Map<String, Object> uaInfo = new HashMap<>();
        uaInfo.put("userAgent", userAgent != null ? userAgent : "");
        uaInfo.put("browser", getBrowser(userAgent));
        uaInfo.put("os", getOS(userAgent));
        uaInfo.put("deviceType", getDeviceType(userAgent));
        return uaInfo;
    }
}

