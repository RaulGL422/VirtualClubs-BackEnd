package galindo.raul.virtualclubs.security;

import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Servicio para generación y validación de tokens JWT (acceso y refresh).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final SecretKey secretKey;
    private final VirtualClubsUsersDetailsService userDetailsService;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;

    @Autowired
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

    /**
     * Genera un token JWT de acceso con claim "type" = "access".
     */
    public String generateAccessToken(String email) {
        log.debug("Generando token de acceso para {}", email);
        return buildToken(email, accessTokenExpirationMillis, "access");
    }

    /**
     * Genera un token JWT de refresh con claim "type" = "refresh".
     */
    public String generateRefreshToken(String email) {
        log.debug("Generando token refresh para {}", email);
        return buildToken(email, refreshTokenExpirationMillis, "refresh");
    }

    /**
     * Construye el token JWT con la expiración indicada y tipo.
     */
    private String buildToken(String email, long expirationMillis, String type) {
        return Jwts.builder()
                .setSubject(email)
                .claim("type", type)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el email (subject) del token JWT.
     * Retorna null si el token es inválido.
     */
    public String extractEmail(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException e) {
            log.warn("Token inválido al extraer email: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valida un token de acceso: verifica tipo, expiración y firma.
     */
    public boolean isValidAccessToken(String token) {
        return isValidTokenOfType(token, "access");
    }

    /**
     * Valida un token de refresh: verifica tipo, expiración y firma.
     */
    public boolean isValidRefreshToken(String token) {
        return isValidTokenOfType(token, "refresh");
    }

    /**
     * Valida que el token sea válido, no expirado y del tipo indicado.
     */
    private boolean isValidTokenOfType(String token, String expectedType) {
        try {
            token = token.replace("\"", "");

            Claims claims = parseClaims(token);
            String type = claims.get("type", String.class);
            if (!expectedType.equals(type)) {
                log.warn("Token inválido: tipo esperado {}, encontrado {}", expectedType, type);
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            log.warn("Token inválido o expirado: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae y valida el token JWT del header Authorization Bearer.
     * Retorna null si no existe o no es válido el formato.
     */
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Construye un Authentication basado en el token.
     */
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String email = extractEmail(token);
        if (email == null) return null;

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Parse Claims sin excepción.
     */
    private Claims parseClaims(String token) throws JwtException {
        return parseClaimsJws(token).getBody();
    }

    private Jws<Claims> parseClaimsJws(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
    }
}
