package com.server.surveyanalystserver.utils;

import com.server.surveyanalystserver.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 生成JWT Token
     * 使用HS512算法和配置的密钥生成Token，Token包含用户名、签发时间和过期时间
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从JWT Token中提取用户名
     * @param token JWT Token字符串
     * @return 用户名，如果Token无效则返回null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 验证JWT Token是否有效
     * 检查Token的签名和过期时间
     * @param token JWT Token字符串
     * @return true表示Token有效，false表示Token无效或已过期
     */
    public Boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从JWT Token中解析Claims
     * 使用配置的密钥解析Token并提取Claims信息
     * @param token JWT Token字符串
     * @return Claims对象，如果解析失败则返回null
     */
    private Claims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断Token是否已过期
     * @param claims JWT Claims对象
     * @return true表示Token已过期，false表示Token未过期
     */
    private Boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}

