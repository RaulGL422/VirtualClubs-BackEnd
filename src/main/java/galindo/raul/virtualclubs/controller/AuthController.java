package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.dtos.*;
import galindo.raul.virtualclubs.models.entities.AuthProvider;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.exceptions.MailSendException;
import galindo.raul.virtualclubs.security.JwtService;
import galindo.raul.virtualclubs.services.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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

    private static final String INTERNALERROR = "internal_error";
    private static final String REFRESHTOKEN = "accessToken";
    private static final String ACCESSTOKEN = "refreshToken";


    // -----------------------------
    // LOGIN (local)
    // -----------------------------
    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticate(@RequestBody AuthRequest authRequest) {
        String email = authRequest.email();
        log.info("🔑 Login attempt: email='{}'", email);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, authRequest.password())
            );
            log.info("✅ Credentials verified for '{}'", email);
        } catch (BadCredentialsException e) {
            log.warn("❌ Invalid credentials for '{}'", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(false, "invalid_credentials", ResponseType.ERROR, null)
            );
        } catch (Exception e) {
            log.error("⚠️ Unexpected error during authentication for '{}': {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, INTERNALERROR, ResponseType.ERROR, null)
            );
        }

        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);
        refreshTokenService.createOrUpdateRefreshToken(email, refreshToken);

        log.info("🎟️ Tokens generated and user '{}' logged in successfully", email);

        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE,
                Map.of(ACCESSTOKEN, accessToken, REFRESHTOKEN, refreshToken)));
    }

    // -----------------------------
    // REGISTER (local)
    // -----------------------------
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        String email = registerRequest.email();
        log.info("📝 Registration attempt: email='{}'", email);

        try {
            userDetailsService.loadUserByUsername(email);
            log.warn("⚠️ Registration failed: user '{}' already exists", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(false, "user_already_exists", ResponseType.ERROR, null)
            );
        } catch (UsernameNotFoundException ignored) {
            log.info("✅ User '{}' not found, proceeding with registration", email);
        } catch (Exception e) {
            log.error("⚠️ Error checking existence of '{}': {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, INTERNALERROR, ResponseType.ERROR, null)
            );
        }

        try {
            // Crear usuario
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

            return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
        } catch (Exception e) {
            log.error("❌ Registration failed for '{}': {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, INTERNALERROR, ResponseType.ERROR, null)
            );
        }
    }

    // -----------------------------
    // REFRESH TOKEN
    // -----------------------------
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();
        log.info("🔄 Refresh token request");

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            log.warn("❌ Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "invalid_or_expired_refresh_token", ResponseType.ERROR, null));
        }

        String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(email);
        log.info("✅ New access token issued for '{}'", email);

        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE,
                Map.of(ACCESSTOKEN, newAccessToken, REFRESHTOKEN, refreshToken)));
    }

    // -----------------------------
    // LOGOUT
    // -----------------------------
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        String refreshToken = request.refreshToken();
        try {
            if (!refreshToken.isBlank()) {
                refreshTokenService.deleteByToken(refreshToken);
                log.info("👋 User logged out, refresh token invalidated");
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
        } catch (Exception e) {
            log.error("⚠️ Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, INTERNALERROR, ResponseType.ERROR, null)
            );
        }
    }

    // -----------------------------
    // LOGIN / REGISTER WITH GOOGLE
    // -----------------------------
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<Map<String, String>>> google(@Valid @RequestBody GoogleAuthRequest request) {
        log.info("🔵 Google login attempt");

        GoogleIdToken.Payload payload;
        try {
            payload = googleAuthService.verifyToken(request.idToken());
        } catch (Exception e) {
            log.warn("❌ Invalid Google ID token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(false, "invalid_google_token", ResponseType.ERROR, null)
            );
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String googleId = payload.getSubject();

        User user = userDetailsService.registerOrLoadUserWithGoogle(email, name, googleId);

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenService.createOrUpdateRefreshToken(user.getEmail(), refreshToken);

        log.info("🎟️ Tokens generated and user '{}' logged in with Google", user.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE,
                Map.of(ACCESSTOKEN, accessToken, REFRESHTOKEN, refreshToken)));
    }

    // -----------------------------
    // REQUEST PASSWORD RESET
    // -----------------------------
    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@RequestBody RequestPasswordResetRequest req) {
        var maybeUser = userDetailsService.findByEmailOptional(req.email());
        if (maybeUser.isEmpty()) {
            log.info("ℹ️ Password reset requested for non-existing email (ignored): '{}'", req.email());
            return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
        }

        User user = maybeUser.get();
        String token = userTokenService.createTokenFor(user, TokenType.PASSWORD_RESET,
                Duration.ofMinutes(5), "request-password-reset");

        String emailContent = mailerService.generatePasswordResetEmail(user.getName() != null ? user.getName() : user.getEmail(), token);
        try {
            mailerService.sendHtmlEmail(user.getEmail(), "Reset VirtualClubs Password", emailContent);
        } catch (MailSendException e) {
            log.error("❌ Failed to send mail: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, INTERNALERROR, ResponseType.ERROR, null)
            );
        }


        log.info("✉️ Password reset email sent to '{}'", user.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
    }

    @GetMapping("/reset-password-redirect")
    public void redirectResetPassword(@RequestParam String token, HttpServletResponse response) throws IOException {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String deeplink = "virtualclubs://pass/reset-password?token=" + encodedToken;
        log.info("✉️ Password Reset Redirecting to '{}'", deeplink);
        response.sendRedirect(deeplink);
    }

    // -----------------------------
    // RESET PASSWORD
    // -----------------------------
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest req) {
        var maybeUser = userTokenService.validateAndConsume(req.token(), TokenType.PASSWORD_RESET);
        if (maybeUser.isEmpty()) {
            log.warn("❌ Invalid or expired password reset token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "invalid_or_expired_token", ResponseType.ERROR, null));
        }

        User user = maybeUser.get();
        var localProviderOpt = user.getAuthProviders().stream()
                .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
                .findFirst();

        if (localProviderOpt.isEmpty()) {
            log.warn("⚠️ User '{}' requested password reset but has no LOCAL provider", user.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "no_local_provider", ResponseType.ERROR, null));
        }

        localProviderOpt.get().setPasswordHash(passwordEncoder.encode(req.newPassword()));
        refreshTokenService.deleteByUser(user);
        userDetailsService.saveUser(user);

        log.info("🔑 Password reset successfully for '{}'", user.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
    }

    // -----------------------------
    // VERIFY EMAIL
    // -----------------------------
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        var maybeUser = userTokenService.validateAndConsume(token, TokenType.EMAIL_VERIFICATION);
        if (maybeUser.isEmpty()) {
            log.warn("❌ Invalid or expired email verification token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "invalid_or_expired_token", ResponseType.ERROR, null));
        }

        User user = maybeUser.get();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(Instant.now());
        userDetailsService.saveUser(user);

        log.info("✅ Email verified for '{}'", user.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "", ResponseType.NONE, null));
    }
}
