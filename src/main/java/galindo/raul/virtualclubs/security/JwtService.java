package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Service for generating and validating JWT tokens (access & refresh).
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final VirtualClubsUsersDetailsService userDetailsService;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            VirtualClubsUsersDetailsService userDetailsService,
            @Value("${jwt.expiration:36000000}") long accessTokenExpirationMillis,
            @Value("${jwt.refreshExpiration:604800000}") long refreshTokenExpirationMillis
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.userDetailsService = userDetailsService;
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    // ==========================================================
    // =============== TOKEN GENERATION =========================
    // ==========================================================

    public String generateAccessToken(String email) {
        return buildToken(email, accessTokenExpirationMillis, "access");
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, refreshTokenExpirationMillis, "refresh");
    }

    private String buildToken(String email, long expirationMillis, String type) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==========================================================
    // =============== TOKEN VALIDATION =========================
    // ==========================================================

    public boolean isValidAccessToken(String token) {
        return validateToken(token, "access");
    }

    public boolean isValidRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    private boolean validateToken(String token, String expectedType) {
        try {
            Claims claims = parseClaims(cleanToken(token));
            String type = claims.get("type", String.class);
            return expectedType.equals(type) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JWT] Invalid or expired token: {}", e.getMessage());
            return false;
        }
    }

    // ==========================================================
    // =============== EXTRACTION ===============================
    // ==========================================================

    public String extractEmail(String token) {
        try {
            return parseClaims(cleanToken(token)).getSubject();
        } catch (JwtException e) {
            log.warn("[JWT] Failed to extract email: {}", e.getMessage());
            return null;
        }
    }

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String email = extractEmail(token);
        if (email == null) return null;

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // ==========================================================
    // =============== HELPERS ==================================
    // ==========================================================

    private String cleanToken(String token) {
        return token == null ? null : token.replace("\"", "").trim();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
