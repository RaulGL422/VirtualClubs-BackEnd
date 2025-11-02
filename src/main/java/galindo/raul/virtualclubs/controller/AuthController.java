package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.dtos.*;
import galindo.raul.virtualclubs.models.entities.AuthProvider;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.models.enums.ResponseType;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.exceptions.*;
import galindo.raul.virtualclubs.security.JwtService;
import galindo.raul.virtualclubs.services.*;
import galindo.raul.virtualclubs.utils.DeepLinkUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  
  private final RefreshTokenService refreshTokenService;
  private final AuthenticationManager authenticationManager;
  private final VirtualClubsUsersDetailsService userDetailsService;
  private final GoogleAuthService googleAuthService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final UserTokenService userTokenService;
  private final MailerService mailerService;
  
  /**
   * Authenticate a user with email and password.
   * <p>
   * Endpoint: POST /api/auth/authenticate
   * <p>
   * Request body: JSON with email and password.
   * <p>
   * Example:
   * {
   * "email": "user@example.com",
   * "password": "secret"
   * }
   * <p>
   * Response: ApiResponse with JWT access and refresh tokens.
   * <p>
   * {
   * "success": true,
   * "message": "",
   * "type": "NONE",
   * "data": {
   * "accessToken": "...",
   * "refreshToken": "..."
   * }
   * }
   * <p>
   * Notes: <p>
   * - Returns 403 if credentials are invalid. <p>
   * - Tokens are generated and stored for refresh handling. <p>
   */
  @PostMapping("/authenticate")
  public ResponseEntity<ApiResponse<TokenResponse>> authenticate(@Valid @RequestBody AuthRequest authRequest) {
    String email = authRequest.email();
    log.info("🔑 Login attempt: email='{}'", email);
    
    try {
      // Auth the user
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, authRequest.password())
      );
      log.info("✅ Credentials verified for '{}'", email);
    } catch (BadCredentialsException e) {
      throw new WrongCredentialsException(email);
    }
    
    // Create new tokens
    String accessToken = jwtService.generateAccessToken(email);
    String refreshToken = jwtService.generateRefreshToken(email);
    refreshTokenService.createOrUpdateRefreshToken(email, refreshToken);
    log.info("🎟️ Tokens generated and user '{}' logged in successfully", email);
    
    // Return both tokens
    return ResponseEntity.ok(
        new ApiResponse<>(true, null, ResponseType.NONE,
            new TokenResponse(accessToken, refreshToken))
    );
  }
  
  /**
   * Register a new user with email and password.
   * <p>
   * Endpoint: POST /api/auth/register
   * <p>
   * Request body: JSON with email and password.
   * <p>
   * Example:
   * {
   * "email": "newuser@example.com",
   * "password": "secret"
   * }
   * <p>
   * Response: ApiResponse<Void> <p>
   * - success=true if registration succeeded <p>
   * - success=false if user already exists or internal error
   * <p>
   * Notes: <p>
   * - Password is hashed and stored securely. <p>
   * - User is created with ROLE_USER and LOCAL auth provider. <p>
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
    String email = registerRequest.email();
    log.info("📝 Registration attempt: email='{}'", email);
    
    // Verify if user with email already exists
    if (userDetailsService.userExists(email))
      throw new UserAlreadyExistException(email);
    
    log.info("✅ User '{}' not found, proceeding with registration", email);
    
    // Create the user
    String encodedPassword = passwordEncoder.encode(registerRequest.password());
    User newUser = new User();
    newUser.setEmail(email);
    newUser.setRoles(Set.of("ROLE_USER"));
    newUser.addAuthProvider(AuthProvider.builder()
        .providerName("LOCAL")
        .passwordHash(encodedPassword)
        .build()
    );
    userDetailsService.saveUser(newUser);
    log.info("✅ User '{}' created successfully", email);
    
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
  }
  
  /**
   * Refresh the access token using a valid refresh token.
   * <p>
   * Endpoint: POST /api/auth/refresh
   * <p>
   * Request body: JSON with refreshToken.
   * <p>
   * Example:
   * {
   * "refreshToken": "..."
   * }
   * <p>
   * Response: ApiResponse with new accessToken and same refreshToken. <p>
   * - Returns 401 if refresh token is invalid or expired. <p>
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
    String refreshToken = refreshRequest.refreshToken();
    log.info("🔄 Refresh token request");
    
    if (!jwtService.isValidRefreshToken(refreshToken)) {
      refreshTokenService.deleteByToken(refreshToken);
      throw new RefreshTokenException();
    }
    
    // Generate a new access token
    String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
    String newAccessToken = jwtService.generateAccessToken(email);
    log.info("✅ New access token issued for '{}'", email);
    
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE,
        new TokenResponse(newAccessToken, refreshToken)));
  }
  
  /**
   * Logout the authenticated user and invalidate their refresh token.
   * <p>
   * Endpoint: POST /api/auth/logout
   * <p>
   * Security: Requires JWT access token (isAuthenticated()).
   * <p>
   * Response: ApiResponse<Void> <p>
   * - success=true if logout succeeded <p>
   * - success=false if internal error <p>
   */
  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
    String email = authentication.getName();
    var maybeUser = userDetailsService.findByEmailOptional(email);
    
    if (maybeUser.isEmpty())
      log.error("❌ User not found to logout: {}", email);
    else
      refreshTokenService.deleteByUser(maybeUser.get());
    
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
  }
  
  /**
   * Authenticate or register a user via Google ID token.
   * <p>
   * Endpoint: POST /api/auth/google
   * <p>
   * Request body: JSON with idToken.
   * <p>
   * Example:
   * {
   * "idToken": "..."
   * }
   * <p>
   * Response: ApiResponse with JWT access and refresh tokens. <p>
   * - Registers user if new, or logs in if exists. <p>
   * - Returns 403 if Google token is invalid. <p>
   */
  @PostMapping("/google")
  public ResponseEntity<ApiResponse<TokenResponse>> google(@Valid @RequestBody GoogleAuthRequest request) {
    log.info("🔵 Google login attempt");
    
    GoogleIdToken.Payload payload;
    try {
      payload = googleAuthService.verifyToken(request.idToken());
    } catch (Exception e) {
      throw new GoogleIdException(e.getMessage());
    }
    
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    String googleId = payload.getSubject();
    
    User user = userDetailsService.registerOrLoadUserWithGoogle(email, name, googleId);
    
    // Create Tokens
    String accessToken = jwtService.generateAccessToken(user.getEmail());
    String refreshToken = jwtService.generateRefreshToken(user.getEmail());
    refreshTokenService.createOrUpdateRefreshToken(user.getEmail(), refreshToken);
    log.info("🎟️ Tokens generated and user '{}' logged in with Google", user.getEmail());
    
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE,
        new TokenResponse(accessToken, refreshToken)));
  }
  
  /**
   * Request a password reset email.
   * <p>
   * Endpoint: POST /api/auth/requestPasswordReset
   * <p>
   * Request body: JSON with email.
   * <p>
   * Example:
   * {
   * "email": "user@example.com"
   * }
   * <p>
   * Response: ApiResponse<Void> <p>
   * - success=true even if email does not exist (for security reasons) <p>
   * - Sends a password reset email with token. <p>
   */
  @PostMapping("/requestPasswordReset")
  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest req) {
    var maybeUser = userDetailsService.findByEmailOptional(req.email());
    // If the user with email don't exist, send an empty response
    if (maybeUser.isEmpty()) {
      log.info("ℹ️ Password reset requested for non-existing email (ignored): '{}'", req.email());
      return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
    }
    
    User user = maybeUser.get();
    String token = userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET,
        Duration.ofMinutes(5), "request-password-reset");
    
    // Setup Email
    String emailContent = mailerService.generatePasswordResetEmail(user.getName() != null ? user.getName() : user.getEmail(), token);
    mailerService.sendHtmlEmail(user.getEmail(), "Reset VirtualClubs Password", emailContent);
    
    log.info("✉️ Password reset email sent to '{}'", user.getEmail());
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
  }
  
  /**
   * Redirects user to mobile app or frontend for password reset.
   * <p>
   * Endpoint: GET /api/auth/resetPasswordRedirect
   * <p>
   * Query parameter: <p>
   * - token: password reset token <p>
   * <p>
   * Notes: <p>
   * - Redirects to deeplink with encoded token. <p>
   */
  @GetMapping("/resetPasswordRedirect")
  public void redirectResetPassword(@RequestParam String token, HttpServletResponse response) throws IOException {
    String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
    String deeplink = DeepLinkUtils.resetPassword(encodedToken);
    log.info("✉️ Password Reset Redirecting to '{}'", deeplink);
    response.sendRedirect(deeplink);
  }
  
  /**
   * Reset the user's password using a valid password reset token.
   * <p>
   * Endpoint: POST /api/auth/resetPassword
   * <p>
   * Request body: JSON with token and newPassword.
   * <p>
   * Example:
   * {
   * "token": "...",
   * "newPassword": "newSecret"
   * }
   * <p>
   * Response: ApiResponse<Void> <p>
   * - success=true if password reset succeeded <p>
   * - success=false if token invalid/expired or user has no LOCAL provider <p>
   */
  @PostMapping("/resetPassword")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
    var maybeUser = userTokenService.validateAndConsume(req.token(), TokenType.PASSWORD_RESET);
    if (maybeUser.isEmpty())
      throw new InvalidTokenException();
    
    User user = maybeUser.get();
    // Search if user has a LOCAL provider
    var localProviderOpt = user.getAuthProviders().stream()
        .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
        .findFirst();
    
    if (localProviderOpt.isEmpty())
      throw new NoLocalProviderException(user.getEmail());
    
    // Reset password
    localProviderOpt.get().setPasswordHash(passwordEncoder.encode(req.newPassword()));
    refreshTokenService.deleteByUser(user);
    userDetailsService.saveUser(user);
    
    log.info("🔑 Password reset successfully for '{}'", user.getEmail());
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, null));
  }
  
  /**
   * Verify email using a token (deeplink for mobile apps).
   * <p>
   * Endpoint: GET /api/auth/verify
   * <p>
   * Query parameter: <p>
   * - token: email verification token <p>
   * <p>
   * Notes: <p>
   * - Marks email as verified if token is valid. <p>
   * - Redirects to a deeplink indicating success or failure. <p>
   */
  @GetMapping("/verify")
  public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
    var maybeUser = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);
    if (maybeUser.isEmpty()) {
      String deeplink = DeepLinkUtils.verifyEmail(false);
      response.sendRedirect(deeplink);
      return;
    }
    
    // Update the user
    User user = maybeUser.get();
    user.setEmailVerified(true);
    user.setEmailVerifiedAt(Instant.now());
    userDetailsService.saveUser(user);
    
    log.info("✅ Email verified for '{}'", user.getEmail());
    String deeplink = DeepLinkUtils.verifyEmail(true);
    response.sendRedirect(deeplink);
  }
  
  /**
   * Request a new email verification token for the authenticated user.
   * <p>
   * Endpoint: POST /api/auth/requestVerify
   * <p>
   * Security: Requires JWT access token (isAuthenticated()).
   * <p>
   * Response: ApiResponse<Void> <p>
   * - Sends verification email with token <p>
   * - success=false if user not found or email sending fails <p>
   */
  @PostMapping("/requestVerify")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> requestVerifyEmail(Authentication authentication) {
    String email = authentication.getName();
    
    var maybeUser = userDetailsService.findByEmailOptional(email);
    if (maybeUser.isEmpty())
      throw new EmailNotFoundException(email);
    
    User user = maybeUser.get();
    String token = userTokenService.createTokenFor(user, TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(5), "request-verify");
    
    String emailContent = mailerService.generateVerifyEmail(user.getName() != null ? user.getName() : user.getEmail(), token);
    mailerService.sendHtmlEmail(user.getEmail(), "Verify VirtualClubs Email", emailContent);
    
    log.info("✉️ Verify email sent to '{}'", user.getEmail());
    return ResponseEntity.ok(
        new ApiResponse<>(true, null, ResponseType.NONE, null)
    );
  }
}