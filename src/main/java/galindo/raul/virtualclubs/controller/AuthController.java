package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.dtos.request.RefreshRequest;
import galindo.raul.virtualclubs.dtos.request.RegisterRequest;
import galindo.raul.virtualclubs.dtos.response.ApiResponse;
import galindo.raul.virtualclubs.dtos.response.RefreshResponse;
import galindo.raul.virtualclubs.dtos.response.RegisterResponse;
import galindo.raul.virtualclubs.models.Dispositive;
import galindo.raul.virtualclubs.models.Tokens;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import galindo.raul.virtualclubs.services.RefreshTokenServiceImpl;
import galindo.raul.virtualclubs.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final UserEntityServiceImpl userService;
  private final JwtUtils jwtUtils;
  private final TokensService tokensService;
  private final RefreshTokenServiceImpl refreshTokenService;
  
  @PostMapping("/register")
  /**
   * Registers a new user in the system and generates initial authentication tokens.
   *
   * @param registerRequest the registration details containing email and password
   * @param request the HTTP request used to extract device information
   * @return a response entity containing the access token, refresh token, and user email
   */
  public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest registerRequest,
                                                                HttpServletRequest request) {
    String email = registerRequest.email();
    String password = registerRequest.password();
    log.info("📝 Registration attempt: email='{}'", email);
    
    UserEntity user = userService.registerUser(email, password);
    Tokens newTokens = tokensService.getNewTokens(user, CommonUtils.getDispositiveInfo(request));
    
    log.info("✅ User '{}' registered", email);
    
    RegisterResponse response = new RegisterResponse(newTokens.accessToken(), newTokens.refreshToken(), email);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
  
  /**
   * Refreshes the authentication tokens using a valid refresh token.
   *
   * @param refreshRequest the request containing the current refresh token
   * @param request the HTTP request used to extract device information
   * @return a response entity containing the new access and refresh tokens
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponse>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest,
                                                                   HttpServletRequest request) {
    String refreshToken = refreshRequest.refreshToken();
    String email = jwtUtils.getUsernameFromToken(refreshToken);
    log.info("🔄 Refresh token request for '{}'", email);
    
    Tokens newTokens = tokensService.refreshTokens(userService.getUserFromEmail(email), refreshToken, CommonUtils.getDispositiveInfo(request));
    
    log.info("✅ Tokens refreshed for '{}'", email);
    
    return ResponseEntity.ok(ApiResponse.success(new RefreshResponse(newTokens.accessToken(), newTokens.refreshToken())));
  }
  
  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetails user = (UserDetails) authentication.getPrincipal();
    String email = user.getUsername();
    Dispositive dispositive = CommonUtils.getDispositiveInfo(request);
    
    refreshTokenService.removeTokenFromDevice(userService.getUserFromEmail(email), dispositive);
    
    log.info("ℹ️ User '{}' in device '{}' logged out", email, dispositive.deviceId());
    
    return ResponseEntity.ok(ApiResponse.emptySuccess());
  }

//  /**
//   * Authenticate or register a user via Google ID token.
//   * <p>
//   * Endpoint: POST /api/auth/google
//   * <p>
//   * Request body: JSON with idToken.
//   * <p>
//   * Example:
//   * {
//   * "idToken": "..."
//   * }
//   * <p>
//   * Response: ApiResponse with JWT access and refresh tokens. <p>
//   * - Registers user if new, or logs in if exists. <p>
//   * - Returns 403 if Google token is invalid. <p>
//   */
//  @PostMapping("/google")
//  public ResponseEntity<ApiResponse<TokenResponse>> google(@Valid @RequestBody GoogleAuthRequest request) {
//    log.info("🔵 Google login attempt");
//
//    GoogleIdToken.Payload payload;
//    try {
//      payload = googleAuthService.verifyToken(request.idToken());
//    } catch (Exception e) {
//      throw new GoogleIdException(e.getMessage());
//    }
//
//    String email = payload.getEmail();
//    String name = (String) payload.get("name");
//    String googleId = payload.getSubject();
//
//    UserEntity user = userDetailsService.registerOrLoadUserWithGoogle(email, name, googleId);
//
//    // Create Tokens
//    String accessToken = jwtService.generateAccessToken(user.getEmail());
//    String refreshToken = jwtService.generateRefreshToken(user.getEmail());
//    refreshTokenService.saveOrUpdate(user.getEmail(), refreshToken);
//    log.info("🎟️ Tokens generated and user '{}' logged in with Google", user.getEmail());
//
//    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE,
//        new TokenResponse(accessToken, refreshToken)));
//  }
//
//  /**
//   * Request a password reset email.
//   * <p>
//   * Endpoint: POST /api/auth/requestPasswordReset
//   * <p>
//   * Request body: JSON with email.
//   * <p>
//   * Example:
//   * {
//   * "email": "user@example.com"
//   * }
//   * <p>
//   * Response: ApiResponse<Void> <p>
//   * - success=true even if email does not exist (for security reasons) <p>
//   * - Sends a password reset email with token. <p>
//   */
//  @PostMapping("/requestPasswordReset")
//  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest req) {
//    var maybeUser = userDetailsService.findByEmailOptional(req.email());
//    // If the user with email don't exist, send an empty response
//    if (maybeUser.isEmpty()) {
//      log.info("ℹ️ Password reset requested for non-existing email (ignored): '{}'", req.email());
//      return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
//    }
//
//    UserEntity user = maybeUser.get();
//    String token = userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET,
//        Duration.ofMinutes(5), "request-password-reset");
//
//    // Setup Email
//    String emailContent = mailerService.generatePasswordResetEmail(user.getName() != null ? user.getName() : user.getEmail(), token);
//    mailerService.sendHtmlEmail(user.getEmail(), "Reset VirtualClubs Password", emailContent);
//
//    log.info("✉️ Password reset email sent to '{}'", user.getEmail());
//    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
//  }
//
//  /**
//   * Redirects user to mobile app or frontend for password reset.
//   * <p>
//   * Endpoint: GET /api/auth/resetPasswordRedirect
//   * <p>
//   * Query parameter: <p>
//   * - token: password reset token <p>
//   * <p>
//   * Notes: <p>
//   * - Redirects to deeplink with encoded token. <p>
//   */
//  @GetMapping("/resetPasswordRedirect")
//  public void redirectResetPassword(@RequestParam String token, HttpServletResponse response) throws IOException {
//    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
//    String deeplink = DeepLinkUtils.resetPassword(encodedToken);
//    log.info("✉️ Password Reset Redirecting to '{}'", deeplink);
//    response.sendRedirect(deeplink);
//  }
//
//  /**
//   * Reset the user's password using a valid password reset token.
//   * <p>
//   * Endpoint: POST /api/auth/resetPassword
//   * <p>
//   * Request body: JSON with token and newPassword.
//   * <p>
//   * Example:
//   * {
//   * "token": "...",
//   * "newPassword": "newSecret"
//   * }
//   * <p>
//   * Response: ApiResponse<Void> <p>
//   * - success=true if password reset succeeded <p>
//   * - success=false if token invalid/expired or user has no LOCAL provider <p>
//   */
//  @PostMapping("/resetPassword")
//  public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
//    var maybeUser = userTokenService.validateAndConsume(req.token(), TokenType.PASSWORD_RESET);
//    if (maybeUser.isEmpty())
//      throw new InvalidTokenException();
//
//    UserEntity user = maybeUser.get();
//    // Search if user has a LOCAL provider
//    var localProviderOpt = user.getAuthProviderEntities().stream()
//        .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
//        .findFirst();
//
//    if (localProviderOpt.isEmpty())
//      throw new NoLocalProviderException(user.getEmail());
//
//    // Reset password
//    localProviderOpt.get().setPasswordHash(passwordEncoder.encode(req.newPassword()));
//    refreshTokenService.deleteByUser(user);
//    userDetailsService.saveUser(user);
//
//    log.info("🔑 Password reset successfully for '{}'", user.getEmail());
//    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
//  }
//
//  /**
//   * Verify email using a token (deeplink for mobile apps).
//   * <p>
//   * Endpoint: GET /api/auth/verify
//   * <p>
//   * Query parameter: <p>
//   * - token: email verification token <p>
//   * <p>
//   * Notes: <p>
//   * - Marks email as verified if token is valid. <p>
//   * - Redirects to a deeplink indicating success or failure. <p>
//   */
//  @GetMapping("/verify")
//  public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
//    var maybeUser = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);
//    if (maybeUser.isEmpty()) {
//      String deeplink = DeepLinkUtils.verifyEmail(false);
//      response.sendRedirect(deeplink);
//      return;
//    }
//
//    // Update the user
//    UserEntity user = maybeUser.get();
//    user.setEmailVerified(true);
//    user.setEmailVerifiedAt(Instant.now());
//    userDetailsService.saveUser(user);
//
//    log.info("✅ Email verified for '{}'", user.getEmail());
//    String deeplink = DeepLinkUtils.verifyEmail(true);
//    response.sendRedirect(deeplink);
//  }
//
//  /**
//   * Request a new email verification token for the authenticated user.
//   * <p>
//   * Endpoint: POST /api/auth/requestVerify
//   * <p>
//   * Security: Requires JWT access token (isAuthenticated()).
//   * <p>
//   * Response: ApiResponse<Void> <p>
//   * - Sends verification email with token <p>
//   * - success=false if user not found or email sending fails <p>
//   */
//  @PostMapping("/requestVerify")
//  @PreAuthorize("isAuthenticated()")
//  public ResponseEntity<ApiResponse<Void>> requestVerifyEmail(Authentication authentication) {
//    String email = authentication.getName();
//
//    var maybeUser = userDetailsService.findByEmailOptional(email);
//    if (maybeUser.isEmpty())
//      throw new EmailNotFoundException(email);
//
//    UserEntity user = maybeUser.get();
//    String token = userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(5), "request-verify");
//
//    String emailContent = mailerService.generateVerifyEmail(user.getName() != null ? user.getName() : user.getEmail(), token);
//    mailerService.sendHtmlEmail(user.getEmail(), "Verify VirtualClubs Email", emailContent);
//
//    log.info("✉️ Verify email sent to '{}'", user.getEmail());
//    return ResponseEntity.ok(
//        new ApiResponse<>(true, null, ResponseType.NONE, null)
//    );
//  }
}