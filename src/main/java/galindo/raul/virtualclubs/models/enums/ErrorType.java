package galindo.raul.virtualclubs.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Códigos de error que se envían al cliente en el campo {@code message} de {@link galindo.raul.virtualclubs.dtos.response.ApiResponse}.
 *
 * <p>Agrupados por categoría:
 * <ul>
 *   <li>1   — Error interno del servidor</li>
 *   <li>2–4 — Autenticación y tokens</li>
 *   <li>5–6 — Recursos</li>
 *   <li>7–10 — Validación de campos</li>
 *   <li>11–13 — Reglas de negocio y límites</li>
 * </ul>
 */
public enum ErrorType {

  // ── Servidor ─────────────────────────────────────────────────
  INTERNAL_ERROR(1),

  // ── Autenticación y tokens ───────────────────────────────────
  INVALID_CREDENTIALS(2),
  INVALID_REFRESH_TOKEN(3),
  INVALID_TOKEN(4),

  // ── Recursos ─────────────────────────────────────────────────
  USER_NOT_FOUND(5),
  EMAIL_ALREADY_EXISTS(6),

  // ── Validación de campos ──────────────────────────────────────
  FIELD_BLANK(7),
  INVALID_EMAIL(8),
  PASSWORD_TOO_SHORT(9),
  PASSWORD_TOO_WEAK(10),

  // ── Reglas de negocio y límites ──────────────────────────────
  EMAIL_NOT_VERIFIED(11),
  NO_LOCAL_PROVIDER(12),
  RATE_LIMIT_EXCEEDED(13);

  private final int code;

  ErrorType(int code) {
    this.code = code;
  }

  @JsonValue
  public int getCode() {
    return code;
  }
}