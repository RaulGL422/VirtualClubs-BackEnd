package galindo.raul.virtualclubs.utils;

import galindo.raul.virtualclubs.models.exceptions.InternalErrorException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utilidades para generación y hashing de tokens.
 *
 * <p>Centraliza toda la lógica criptográfica relacionada con tokens para evitar
 * duplicación y garantizar un único punto de cambio si se actualiza el algoritmo.
 *
 * <ul>
 *   <li>{@link #generateTokenString(int)} — genera un token URL-safe aleatorio</li>
 *   <li>{@link #sha256Hex(String)} — hash SHA-256 en hexadecimal (para user_tokens en BD)</li>
 *   <li>{@link #sha256Base64(String)} — hash SHA-256 en Base64 (para refresh_tokens en BD)</li>
 * </ul>
 */
public final class TokenUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    private TokenUtils() {}

    /**
     * Genera un token aleatorio seguro codificado en Base64 URL-safe sin padding.
     *
     * @param byteLength número de bytes aleatorios a generar (mayor = más entropía)
     * @return token en texto plano, seguro para usar en URLs
     */
    public static String generateTokenString(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Calcula el hash SHA-256 de una cadena y lo devuelve en formato hexadecimal.
     * Usado para almacenar tokens de verificación de email y reset de contraseña en BD.
     *
     * @param input cadena a hashear
     * @return hash SHA-256 en hexadecimal (64 caracteres)
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Calcula el hash SHA-256 de una cadena y lo devuelve en Base64 estándar.
     * Usado para almacenar refresh tokens en BD.
     *
     * @param input cadena a hashear
     * @return hash SHA-256 en Base64 (44 caracteres)
     */
    public static String sha256Base64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new InternalErrorException(e.getMessage());
        }
    }
}
