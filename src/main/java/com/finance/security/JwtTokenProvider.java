package com.finance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT生成与解析
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        // 把密钥转成 JWT 需要的格式
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 Token
     * @param userId
     * @param role
     * @return
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);// 过期时间

        return Jwts.builder()
                .claim("userId", userId)// 存入用户ID
                .claim("role", role)// 存入角色
                .issuedAt(now) // 签发时间
                .expiration(expiryDate) // 过期时间
                .signWith(secretKey)// 签名（防篡改）
                .compact();// 生成最终Token
    }

    /**
     * 解析 Token
     * @param token
     * @return
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)// 用密钥验证
                .build()
                .parseSignedClaims(token)// 解析
                .getPayload(); // 拿到里面的数据
    }

    /**
     * 从 Token 里拿 用户 ID
     * @param token
     * @return
     */
    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    /**
     * 从 Token 里拿 角色
     * @param token
     * @return
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     *  验证 Token 是否有效
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
