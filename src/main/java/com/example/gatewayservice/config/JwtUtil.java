package com.example.gatewayservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public UUID getMemberIdFromToken(String token) {
        //  String tokenValue = token.substring(7);
        System.out.println(token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
//        String tokenValue = token.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get("role");
    }

    public String validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return "Authorized";
        } catch (SecurityException | MalformedJwtException e) {
            // 유효하지 않는 JWT 서명
            return "Unauthorized: Invalid JWT signature";
        } catch (ExpiredJwtException e) {
            // 만료된 JWT 토큰
            return "Unauthorized: Expired JWT token";
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 토큰
            return "Unauthorized: Unsupported JWT token";
        } catch (IllegalArgumentException e) {
            // 잘못된 JWT 토큰
            return "Unauthorized: JWT claims is empty";
        }
    }
}
