package galindo.raul.virtualclubs.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUrlUtilsTest {

    private static final String BASE = "https://api-vc.rgal.dev";

    @Test
    void verifyEmailUrl_contieneBaseYEndpoint() {
        String url = AuthUrlUtils.verifyEmailUrl(BASE, "token123");
        assertThat(url).startsWith(BASE + "/v1/auth/verify?token=");
    }

    @Test
    void verifyEmailUrl_contieneToken() {
        String url = AuthUrlUtils.verifyEmailUrl(BASE, "mytoken");
        assertThat(url).contains("mytoken");
    }

    @Test
    void verifyEmailUrl_codificaTokensConCaracteresEspeciales() {
        String url = AuthUrlUtils.verifyEmailUrl(BASE, "tok en+especial");
        // El espacio debe estar codificado, el '+' también
        assertThat(url).doesNotContain(" ");
    }

    @Test
    void resetPasswordUrl_contieneBaseYEndpoint() {
        String url = AuthUrlUtils.resetPasswordUrl(BASE, "reset-token");
        assertThat(url).startsWith(BASE + "/v1/auth/resetPasswordRedirect?token=");
    }

    @Test
    void resetPasswordUrl_contieneToken() {
        String url = AuthUrlUtils.resetPasswordUrl(BASE, "mi-reset");
        assertThat(url).contains("mi-reset");
    }

    @Test
    void verifyEmailUrl_yResetPasswordUrl_sonDistintas() {
        String verify = AuthUrlUtils.verifyEmailUrl(BASE, "tok");
        String reset  = AuthUrlUtils.resetPasswordUrl(BASE, "tok");
        assertThat(verify).isNotEqualTo(reset);
    }
}
