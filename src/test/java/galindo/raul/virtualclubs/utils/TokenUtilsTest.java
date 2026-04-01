package galindo.raul.virtualclubs.utils;

import galindo.raul.virtualclubs.models.exceptions.InternalErrorException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenUtilsTest {

    // ─────────────────────────────────────────────────────────────
    // generateTokenString
    // ─────────────────────────────────────────────────────────────

    @Test
    void generateTokenString_devuelveTokenNoNulo() {
        String token = TokenUtils.generateTokenString(32);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateTokenString_dosLlamadasProducenTokensDistintos() {
        String a = TokenUtils.generateTokenString(32);
        String b = TokenUtils.generateTokenString(32);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void generateTokenString_longitudProporcionalABytes() {
        // Base64 URL sin padding: ceil(byteLength * 4/3)
        String token16 = TokenUtils.generateTokenString(16);
        String token32 = TokenUtils.generateTokenString(32);
        assertThat(token32.length()).isGreaterThan(token16.length());
    }

    // ─────────────────────────────────────────────────────────────
    // sha256Hex
    // ─────────────────────────────────────────────────────────────

    @Test
    void sha256Hex_mismaPalabraProduceMismoHash() {
        String h1 = TokenUtils.sha256Hex("hello");
        String h2 = TokenUtils.sha256Hex("hello");
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void sha256Hex_distintoInputProduceDistintoHash() {
        assertThat(TokenUtils.sha256Hex("hello")).isNotEqualTo(TokenUtils.sha256Hex("world"));
    }

    @Test
    void sha256Hex_produceSiempre64Caracteres() {
        // SHA-256 en hex = 32 bytes * 2 chars/byte = 64 chars
        assertThat(TokenUtils.sha256Hex("cualquier entrada")).hasSize(64);
    }

    @Test
    void sha256Hex_resultadoEsHexadecimal() {
        String hash = TokenUtils.sha256Hex("test");
        assertThat(hash).matches("[0-9a-f]{64}");
    }

    @Test
    void sha256Hex_stringVacioDevuelveHash() {
        // SHA-256 de "" es un hash conocido
        assertThat(TokenUtils.sha256Hex("")).hasSize(64);
    }

    // ─────────────────────────────────────────────────────────────
    // sha256Base64
    // ─────────────────────────────────────────────────────────────

    @Test
    void sha256Base64_mismaPalabraProduceMismoHash() {
        String h1 = TokenUtils.sha256Base64("hello");
        String h2 = TokenUtils.sha256Base64("hello");
        assertThat(h1).isEqualTo(h2);
    }

    @Test
    void sha256Base64_distintoInputProduceDistintoHash() {
        assertThat(TokenUtils.sha256Base64("hello")).isNotEqualTo(TokenUtils.sha256Base64("world"));
    }

    @Test
    void sha256Base64_produceSiempre44Caracteres() {
        // SHA-256 = 32 bytes → Base64 = ceil(32/3)*4 = 44 chars (con padding)
        assertThat(TokenUtils.sha256Base64("cualquier entrada")).hasSize(44);
    }

    @Test
    void sha256Base64_resultadoDistintoDeSha256Hex() {
        String input = "mismo-input";
        assertThat(TokenUtils.sha256Base64(input)).isNotEqualTo(TokenUtils.sha256Hex(input));
    }
}
