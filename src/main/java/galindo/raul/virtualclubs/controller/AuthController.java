package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.dtos.request.*;
import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.dtos.response.GoogleResponse;
import galindo.raul.virtualclubs.dtos.response.RefreshResponse;
import galindo.raul.virtualclubs.dtos.response.RegisterResponse;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.InvalidTokenException;
import galindo.raul.virtualclubs.models.exceptions.NoLocalProviderException;
import galindo.raul.virtualclubs.services.*;
import galindo.raul.virtualclubs.utils.CommonUtils;
import galindo.raul.virtualclubs.utils.DeepLinkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Autenticación", description = "Registro, login, gestión de sesiones y recuperación de contraseña")
public class AuthController {
  
  private final UserService userService;
  private final UserTokenService userTokenService;
  private final JwtUtils jwtUtils;
  private final TokensService tokensService;
  private final RefreshTokenService refreshTokenService;
  private final NotificationService notificationService;
  private final PasswordEncoder passwordEncoder;
  private final GoogleAuthService googleAuthService;
  
  @Operation(summary = "Registrar usuario",
      description = "Crea una nueva cuenta y devuelve los tokens JWT. Envía automáticamente un email de verificación.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado correctamente")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos (código 7-10)")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado (código 6)")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Demasiadas peticiones (código 13)")
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest registerRequest,
      HttpServletRequest request,
      Locale locale) {
    
    String email = registerRequest.email();
    String password = registerRequest.password();
    log.info("Registration attempt: email='{}'", email);
    
    UserEntity user = userService.registerUser(email, password);
    Tokens newTokens = tokensService.getNewTokens(user, CommonUtils.getDispositiveInfo(request));
    notificationService.sendVerificationEmail(user, locale);
    
    log.info("User '{}' registered successfully", email);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(new RegisterResponse(newTokens.accessToken(), newTokens.refreshToken(), email)));
  }
  
  @Operation(summary = "Renovar tokens",
      description = "Intercambia un refresh token válido por un nuevo par de tokens. El refresh token anterior queda invalidado.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tokens renovados")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido o revocado (código 3)")
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponse>> refreshToken(
      @Valid @RequestBody RefreshRequest refreshRequest,
      HttpServletRequest request) {
    
    String refreshToken = refreshRequest.refreshToken();
    String email = jwtUtils.getUsernameFromToken(refreshToken);
    log.info("Refresh token request for '{}'", email);
    
    Tokens newTokens = tokensService.refreshTokens(
        userService.getUserFromEmail(email), refreshToken, CommonUtils.getDispositiveInfo(request));
    
    log.info("Tokens refreshed for '{}'", email);
    
    return ResponseEntity.ok(ApiResponse.success(new RefreshResponse(newTokens.accessToken(), newTokens.refreshToken())));
  }
  
  @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token del dispositivo actual. Las otras sesiones permanecen activas.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido o expirado (código 4)")
  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication == null) {
      return ResponseEntity.ok(ApiResponse.emptySuccess());
    }
    
    UserDetails user  = (UserDetails) authentication.getPrincipal();
    String email      = user.getUsername();
    Dispositive disp  = CommonUtils.getDispositiveInfo(request);

    refreshTokenService.removeTokenFromDevice(userService.getUserFromEmail(email), disp);
    
    log.info("User '{}' in device '{}' logged out", email, disp.deviceId());
    
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }
  
  @Operation(summary = "Solicitar reset de contraseña",
      description = "Envía un email con enlace de reset. Siempre devuelve 200 aunque el email no exista (anti-enumeración).")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Solicitud procesada")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Demasiadas peticiones (código 13)")
  @PostMapping("/requestPasswordReset")
  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
      @Valid @RequestBody RequestPasswordResetRequest req,
      Locale locale) {
    
    userService.findByEmailOptional(req.email())
        .ifPresent(user -> notificationService.sendPasswordResetEmail(user, locale));
    
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }
  
  @Operation(summary = "Redirigir reset de contraseña",
      description = "Recibe el token del email y redirige a la app móvil via deep link `virtualclubs://`. No consume el token.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirección al deep link de la app")
  @GetMapping("/resetPasswordRedirect")
  public void redirectResetPassword(@RequestParam String token, HttpServletResponse response) throws IOException {
    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
    log.info("Redirecting password reset to deep link");
    response.sendRedirect(DeepLinkUtils.resetPassword(encodedToken));
  }
  
  @Operation(summary = "Restablecer contraseña",
      description = "Establece la nueva contraseña usando el token de reset (válido 30 min). Revoca todas las sesiones activas.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña restablecida")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido, expirado o ya usado (código 4)")
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
  
  @Operation(summary = "Verificar email",
      description = "Consume el token de verificación (válido 24h) y redirige a la app con el resultado via deep link.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirección al deep link con resultado")
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
  
  @Operation(summary = "Reenviar email de verificación",
      description = "Genera un nuevo token y reenvía el email de verificación al usuario autenticado.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Email enviado")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token inválido o expirado (código 4)")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Demasiadas peticiones (código 13)")
  @PostMapping("/requestVerify")
  public ResponseEntity<ApiResponse<Void>> requestVerifyEmail(Authentication authentication, Locale locale) {
    String email = authentication.getName();
    UserEntity user = userService.findByEmailOptional(email)
        .orElseThrow(() -> new EmailNotFoundException(email));
    
    notificationService.sendVerificationEmail(user, locale);
    
    log.info("Verify email sent to '{}'", email);
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }
  
  @Operation(summary = "Login con Google",
      description = "Autentica o registra un usuario usando un ID Token del SDK de Google Sign-In (Android). Devuelve tokens JWT propios.")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login / registro con Google correcto")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "ID Token de Google inválido (código 4)")
  @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Demasiadas peticiones (código 13)")
  /**
   * Autentica o registra un usuario mediante Google OAuth2.
   *
   * <p>El cliente Android obtiene un ID Token usando el SDK de Google Sign-In y lo envía aquí.
   * El backend verifica la firma y la audiencia del token con {@link GoogleAuthService},
   * luego busca o crea la cuenta de usuario con {@link UserService#registerOrLoadUserWithGoogle}
   * y devuelve los tokens JWT propios de la aplicación.
   *
   * <p>Casos manejados:
   * <ul>
   *   <li>Usuario nuevo → se crea cuenta con proveedor GOOGLE (email verificado por defecto)</li>
   *   <li>Email ya existe con cuenta LOCAL verificada → se vincula el proveedor GOOGLE</li>
   *   <li>Email ya existe con cuenta LOCAL no verificada → se elimina y se crea cuenta GOOGLE</li>
   *   <li>Usuario GOOGLE existente → login directo</li>
   * </ul>
   */
  @PostMapping("/google")
  public ResponseEntity<ApiResponse<GoogleResponse>> googleLogin(
      @Valid @RequestBody GoogleAuthRequest googleAuthRequest,
      HttpServletRequest request) {

    Dispositive disp = CommonUtils.getDispositiveInfo(request);
    log.info("Google login attempt from IP '{}'", disp.ipAddress());

    GoogleIdToken.Payload payload = googleAuthService.verifyToken(googleAuthRequest.idToken());

    String email    = payload.getEmail();
    String name     = (String) payload.get("name");
    String googleId = payload.getSubject();

    UserEntity user      = userService.registerOrLoadUserWithGoogle(email, name, googleId);
    Tokens newTokens     = tokensService.getNewTokens(user, disp);

    log.info("Google login successful for '{}'", email);
    return ResponseEntity.ok(ApiResponse.success(new GoogleResponse(newTokens.accessToken(), newTokens.refreshToken())));
  }
}
