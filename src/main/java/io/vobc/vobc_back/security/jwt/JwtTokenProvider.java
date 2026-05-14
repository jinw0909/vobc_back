package io.vobc.vobc_back.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Getter
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}") private String secretKey;
    @Value("${jwt.access-token-expiration-ms}") private long accessTokenExpirationMs;
    @Value("${jwt.refresh-token-expiration-ms}") private long refreshTokenExpirationMs;
    private SecretKey key;

    @PostConstruct
    public void init() { this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)); }

    public String createWalletAccessToken(Long walletUserId, String walletAddress) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
        return Jwts.builder()
                .setSubject(String.valueOf(walletUserId))
                .claim("authType", "WALLET")
                .claim("walletAddress", walletAddress)
                .claim("role", "ROLE_USER")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    } public

    String getAuthType(String token) {
        return getClaims(token).get("authType", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String getWalletAddress(String token) {
        return getClaims(token).get("walletAddress", String.class);
    }

    public String createRefreshToken(String walletAddress) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder()
                .claim("authType", "WALLET")
                .claim("tokenType", "REFRESH")
                .claim("walletAddress", walletAddress)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();

    }


    public String createWalletReAuthToken(Long userId, String address) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 5 * 60 * 1000L);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("authType", "WALLET")
                .claim("tokenType", "REAUTH")
                .claim("purpose", "reauth")
                .claim("walletAddress", address)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean isValidWalletReAuthToken(String token, Long userId, String walletAddress) {
        try {
            Claims claims = getClaims(token);

            String subject = claims.getSubject();
            String authType = claims.get("authType", String.class);
            String tokenType = claims.get("tokenType", String.class);
            String purpose = claims.get("purpose", String.class);
            String tokenWalletAddress = claims.get("walletAddress", String.class);

            return String.valueOf(userId).equals(subject)
                    && "WALLET".equals(authType)
                    && "REAUTH".equals(tokenType)
                    && "reauth".equals(purpose)
                    && walletAddress.equalsIgnoreCase(tokenWalletAddress);
        } catch (Exception e) {
            return false;
        }
    }
}
