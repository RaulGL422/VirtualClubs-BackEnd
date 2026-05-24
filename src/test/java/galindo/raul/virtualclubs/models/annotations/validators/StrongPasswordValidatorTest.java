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
     * ≥2 mayúsculas, ≥2 minúsculas, ≥1 dígito.
     */
    @ParameterizedTest(name = "[{index}] ''{0}'' → válida={1}")
    @CsvSource({
        "PAss12,   true",   // exactamente el mínimo: 2U 2L 1D
        "AaBb1cDd, true",   // más chars de los necesarios
        "abcde12,  false",  // 0 mayúsculas
        "ABCDE12,  false",  // 0 minúsculas
        "PASSword, false",  // 0 dígitos
        "Aa1bbbbb, false",  // solo 1 mayúscula — necesita ≥2
        "AAbbbbbb, false",  // 2U 2L pero 0D
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
