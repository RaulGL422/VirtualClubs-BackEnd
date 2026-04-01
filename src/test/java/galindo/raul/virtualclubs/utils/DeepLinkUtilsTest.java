package galindo.raul.virtualclubs.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeepLinkUtilsTest {

    @Test
    void resetPassword_contieneEsquemaVirtualclubs() {
        String link = DeepLinkUtils.resetPassword("abc123");
        assertThat(link).startsWith("virtualclubs://");
    }

    @Test
    void resetPassword_contieneRutaYToken() {
        String link = DeepLinkUtils.resetPassword("mi-token");
        assertThat(link).contains("pass/reset-password?token=mi-token");
    }

    @Test
    void verifyEmail_exitoso_contieneStatus1() {
        String link = DeepLinkUtils.verifyEmail(true);
        assertThat(link).contains("status=1");
    }

    @Test
    void verifyEmail_fallido_contieneStatus0() {
        String link = DeepLinkUtils.verifyEmail(false);
        assertThat(link).contains("status=0");
    }

    @Test
    void verifyEmail_contieneEsquemaVirtualclubs() {
        assertThat(DeepLinkUtils.verifyEmail(true)).startsWith("virtualclubs://");
        assertThat(DeepLinkUtils.verifyEmail(false)).startsWith("virtualclubs://");
    }

    @Test
    void verifyEmail_exitosoYFallidoSonDistintos() {
        assertThat(DeepLinkUtils.verifyEmail(true)).isNotEqualTo(DeepLinkUtils.verifyEmail(false));
    }
}
