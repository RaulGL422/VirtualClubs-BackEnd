package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.dtos.request.RefreshRequest;
import galindo.raul.virtualclubs.dtos.request.RegisterRequest;
import galindo.raul.virtualclubs.dtos.request.RequestPasswordResetRequest;
import galindo.raul.virtualclubs.dtos.request.ResetPasswordRequest;
import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.dtos.response.RefreshResponse;
import galindo.raul.virtualclubs.dtos.response.RegisterResponse;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import galindo.raul.virtualclubs.models.exceptions.NoLocalProviderException;
import galindo.raul.virtualclubs.services.NotificationService;
import galindo.raul.virtualclubs.services.RefreshTokenServiceImpl;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import galindo.raul.virtualclubs.services.UserTokenServiceImpl;
import galindo.raul.virtualclubs.utils.CommonUtils;
import galindo.raul.virtualclubs.utils.DeepLinkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserEntityServiceImpl userService;
  private final UserTokenServiceImpl userTokenService;
  private final JwtUtils jwtUtils;
  private final TokensService tokensService;
  private final RefreshTokenServiceImpl refreshTokenService;
  private final NotificationService notificationService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Registra un nuevo usuario y genera los tokens de autenticación iniciales.
   * Envía automáticamente un email de verificación al idioma del cliente.
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest registerRequest,
      HttpServletRequest request,
      Locale locale) {

    String email    = registerRequest.email();
    String password = registerRequest.password();
    log.info("Registration attempt: email='{}'", email);

    UserEntity user     = userService.registerUser(email, password);
    Tokens newTokens    = tokensService.getNewTokens(user, CommonUtils.getDispositiveInfo(request));
    notificationService.sendVerificationEmail(user, locale);

    log.info("User '{}' registered successfully", email);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(new RegisterResponse(newTokens.accessToken(), newTokens.refreshToken(), email)));
  }

  /**
   * Renueva los tokens usando un refresh token válido.
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponse>> refreshToken(
      @Valid @RequestBody RefreshRequest refreshRequest,
      HttpServletRequest request) {

    String refreshToken = refreshRequest.refreshToken();
    String email        = jwtUtils.getUsernameFromToken(refreshToken);
    log.info("Refresh token request for '{}'", email);

    Tokens newTokens = tokensService.refreshTokens(
        userService.getUserFromEmail(email), refreshToken, CommonUtils.getDispositiveInfo(request));

    log.info("Tokens refreshed for '{}'", email);

    return ResponseEntity.ok(ApiResponse.success(new RefreshResponse(newTokens.accessToken(), newTokens.refreshToken())));
  }

  /**
   * Cierra la sesión del dispositivo actual revocando su refresh token.
   */
  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails user  = (UserDetails) authentication.getPrincipal();
    String email      = user.getUsername();
    Dispositive disp  = CommonUtils.getDispositiveInfo(request);

    refreshTokenService.removeTokenFromDevice(userService.getUserFromEmail(email), disp);

    log.info("User '{}' in device '{}' logged out", email, disp.deviceId());

    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }

  /**
   * Solicita el reset de contraseña.
   * Siempre devuelve 200 aunque el email no exista (evita enumeración de usuarios).
   */
  @PostMapping("/requestPasswordReset")
  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
      @Valid @RequestBody RequestPasswordResetRequest req,
      Locale locale) {

    userService.findByEmailOptional(req.email())
        .ifPresent(user -> notificationService.sendPasswordResetEmail(user, locale));

    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }

  /**
   * Recibe el token del email y redirige a la app móvil via deep link.
   * No valida ni consume el token aquí — eso ocurre en /resetPassword.
   */
  @GetMapping("/resetPasswordRedirect")
  public void redirectResetPassword(@RequestParam String token, HttpServletResponse response) throws IOException {
    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
    log.info("Redirecting password reset to deep link");
    response.sendRedirect(DeepLinkUtils.resetPassword(encodedToken));
  }

  /**
   * Establece la nueva contraseña usando el token de reset.
   * Invalida el token y revoca todas las sesiones activas tras el cambio.
   */
  @PostMapping("/resetPassword")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
    UserEntity user = userTokenService.validateAndConsume(req.token(), TokenType.PASSWORD_RESET)
        .orElseThrow(InvalidTokenException::new);

    user.getAuthProviderEntities().stream()
        .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
        .findFirst()
        .orElseThrow(() -> new NoLocalProviderException(user.getEmail()))
        .setPasswordHash(passwordEncoder.encode(req.newPassword()));

    userService.saveUser(user);
    refreshTokenService.revokeAllByUser(user);

    log.info("Password reset successfully for '{}'", user.getEmail());
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }

  /**
   * Verifica el email del usuario usando el token recibido por correo.
   * Redirige a la app móvil via deep link con el resultado (éxito o fallo).
   */
  @GetMapping("/verify")
  public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
    var maybeUser = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);
    if (maybeUser.isEmpty()) {
      response.sendRedirect(DeepLinkUtils.verifyEmail(false));
      return;
    }
    UserEntity user = maybeUser.get();
    user.setEmailVerified(true);
    user.setEmailVerifiedAt(Instant.now());
    userService.saveUser(user);
    log.info("Email verified for '{}'", user.getEmail());
    response.sendRedirect(DeepLinkUtils.verifyEmail(true));
  }

  /**
   * Reenvía el email de verificación al usuario autenticado.
   */
  @PostMapping("/requestVerify")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> requestVerifyEmail(Authentication authentication, Locale locale) {
    String email    = authentication.getName();
    UserEntity user = userService.findByEmailOptional(email)
        .orElseThrow(() -> new EmailNotFoundException(email));

    notificationService.sendVerificationEmail(user, locale);

    log.info("Verify email sent to '{}'", email);
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }
}
