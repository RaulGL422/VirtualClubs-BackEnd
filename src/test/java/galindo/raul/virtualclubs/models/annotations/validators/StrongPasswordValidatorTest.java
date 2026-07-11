package galindo.raul.virtualclubs.models.annotations.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    /**
     * Tabla exhaustiva de contraseñas contra el regex:
     * ≥1 letra (mayúscula o minúscula), ≥1 dígito, longitud mínima 6.
     */
    @ParameterizedTest(name = "[{index}] ''{0}'' → válida={1}")
    @CsvSource({
        "abc123,    true",   // mínimo estricto: 1 letra, 1 dígito, 6 chars
        "PAss12,    true",   // mezcla de mayúsculas/minúsculas + dígitos
        "password1, true",   // solo minúsculas + 1 dígito
        "UPPER123,  true",   // solo mayúsculas + dígitos
        "abcde,     false",  // sin dígito
        "123456,    false",  // sin letra
        "abc12,     false",  // 5 chars — menos del mínimo de 6
    })
    void validate_variasContrasenas(String password, boolean esperado) {
        assertThat(validator.isValid(password.strip(), null)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullSource
    void validate_null_retornaFalso(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void validate_cadenaVacia_retornaFalso() {
        assertThat(validator.isValid("", null)).isFalse();
    }
}
