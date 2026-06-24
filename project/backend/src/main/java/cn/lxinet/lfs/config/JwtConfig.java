package cn.lxinet.lfs.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import cn.lxinet.lfs.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 配置
 * 
 * 【原理讲解】
 * JWT (JSON Web Token) 是一种无状态的认证方式：
 * 1. 结构：Header.Payload.Signature
 *    - Header: 算法类型（如 HMAC SHA256）
 *    - Payload: 数据（如 userId, username, 过期时间）
 *    - Signature: 签名，防止篡改
 * 
 * 2. 流程：
 *    登录成功 → 服务端生成 Token → 返回给客户端
 *    客户端请求 → 携带 Token → 服务端验证 Token
 * 
 * 3. 优点：
 *    - 无状态，服务端不存储 session
 *    - 可扩展，适合分布式系统
 */
@Component
public class JwtConfig {
    @Value("${config.app-id}")
    private String appId;
    @Value("${config.app-secret}")
    private String appSecret;
    @Value("${config.token-expire}")
    private long expire;
    private static String ISSUER = "";

    /**
     * 生成 Token（原有的 appId/appSecret 方式）
     */
    public String genToken(String id, String secret){
        if (!appId.equals(id) || !appSecret.equals(secret)){
            throw new BaseException("appId或者appSecret不正确");
        }
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        JWTCreator.Builder builder = JWT.create().withIssuer(ISSUER).withIssuedAt(new Date()).
                withExpiresAt(new Date((Instant.now().getEpochSecond() + expire) * 1000));
        Map<String, String> claims = new HashMap<>();
        claims.put("appId", appId);
        claims.forEach((key,value)-> builder.withClaim(key, value));
        return builder.sign(algorithm);
    }

    /**
     * 为用户生成 Token
     * 
     * 【原理】
     * 1. 使用 HMAC256 算法 + appSecret 作为密钥
     * 2. Payload 中存入 userId 和 username
     * 3. 设置过期时间
     * 4. 生成签名
     */
    public String genUserToken(Long userId, String username) {
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        JWTCreator.Builder builder = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date((Instant.now().getEpochSecond() + expire) * 1000))
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("type", "user"); // 标记为用户 Token
        return builder.sign(algorithm);
    }

    /**
     * 解析 JWT Token
     * 
     * 【原理】
     * 1. 使用相同的密钥和算法验证签名
     * 2. 检查是否过期
     * 3. 提取 Payload 中的数据
     */
    public boolean parseToken(String token) {
        try {
            if (StringUtils.isBlank(token)){
                return false;
            }
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            Map<String, Claim> map = jwt.getClaims();
            
            // 支持两种 Token：appId 方式和用户方式
            if (map.containsKey("appId")) {
                String thisAppId = map.get("appId").asString();
                return appId.equals(thisAppId);
            } else if (map.containsKey("userId")) {
                // 用户 Token，只要能解析成功就有效
                return map.get("userId").asLong() != null;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            return null;
        }
    }
}
