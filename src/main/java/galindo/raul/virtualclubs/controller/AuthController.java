package galindo.raul.virtualclubs.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import galindo.raul.virtualclubs.dtos.*;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.security.JwtService;
import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.RefreshTokenService;
import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // Login user (email + password)
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest authRequest) {
        log.info("🔑 Login attempt: email='{}'", authRequest.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
            );
            log.info("✅ Credentials verified for '{}'", authRequest.email());
        } catch (BadCredentialsException e) {
            log.warn("❌ Invalid credentials for '{}'", authRequest.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(false, "invalid_credentials", ResponseType.ERROR, null)
            );
        } catch (Exception e) {
            log.error("⚠️ Unexpected error during authentication for '{}': {}", authRequest.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "internal_error", ResponseType.ERROR, null)
            );
        }

        var userDetails = userDetailsService.loadUserByUsername(authRequest.email());
        String accessToken = jwtService.generateAccessToken(userDetails.getUsername());
        String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());

        refreshTokenService.createOrUpdateRefreshToken(userDetails.getUsername(), refreshToken);

        log.info("🎟️ Tokens generated and user '{}' logged in successfully", authRequest.email());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "", ResponseType.NONE,
                        Map.of(
                                "accessToken", accessToken,
                                "refreshToken", refreshToken
                        )
                )
        );
    }

    // Register user (local email + password)
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("📝 Registration attempt: email='{}'", registerRequest.email());

        try {
            userDetailsService.loadUserByUsername(registerRequest.email());
            log.warn("⚠️ Registration failed: user '{}' already exists", registerRequest.email());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(false, "user_already_exists", ResponseType.ERROR, null)
            );
        } catch (UsernameNotFoundException ignored) {
            log.info("✅ User '{}' not found, proceeding with registration", registerRequest.email());
        } catch (Exception e) {
            log.error("⚠️ Unexpected error during user existence check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "internal_error", ResponseType.ERROR, null)
            );
        }

        try {
            String encodedPassword = passwordEncoder.encode(registerRequest.password());

            User newUser = new User();
            newUser.setEmail(registerRequest.email());
            newUser.setRoles(Set.of("ROLE_USER"));
            newUser.addAuthProvider(
                    galindo.raul.virtualclubs.models.entities.AuthProvider.builder()
                            .providerName("LOCAL")
                            .passwordHash(encodedPassword)
                            .build()
            );

            userDetailsService.saveUser(newUser);

            log.info("✅ User '{}' registered successfully", registerRequest.email());

            String accessToken = jwtService.generateAccessToken(newUser.getEmail());
            String refreshToken = jwtService.generateRefreshToken(newUser.getEmail());

            refreshTokenService.createOrUpdateRefreshToken(newUser.getEmail(), refreshToken);
            log.info("🎟️ Tokens generated for new user '{}'", registerRequest.email());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "", ResponseType.NONE,
                            Map.of(
                                    "accessToken", accessToken,
                                    "refreshToken", refreshToken
                            )
                    )
            );
        } catch (Exception e) {
            log.error("❌ Registration failed for user '{}': {}", registerRequest.email(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "internal_error", ResponseType.ERROR, null)
            );
        }
    }

    // Refresh token endpoint
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        log.info("[AUTH] Refresh token request received");
        String refreshToken = refreshRequest.refreshToken();

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            log.warn("[AUTH] Invalid or expired refresh token: {}", refreshToken);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "invalid_or_expired_refresh_token", ResponseType.ERROR, null));
        }

        String email = refreshTokenService.getEmailFromRefreshToken(refreshToken);
        String newAccessToken = jwtService.generateAccessToken(email);
        log.info("[AUTH] New access token generated successfully");

        return ResponseEntity.ok(
                new ApiResponse<>(true, "", ResponseType.NONE,
                        Map.of(
                                "accessToken", newAccessToken,
                                "refreshToken", refreshToken
                        )
                )
        );
    }

    // Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        String refreshToken = request.refreshToken();
        try {
            if (!refreshToken.isBlank()) {
                refreshTokenService.deleteByToken(refreshToken);
                log.info("👋 User logged out, refresh token invalidated");
            }
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "", ResponseType.NONE, null)
            );
        } catch (Exception e) {
            log.error("⚠️ Error during logout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse<>(false, "internal_error", ResponseType.ERROR, null)
            );
        }
    }

    // Login or register with Google
    @PostMapping("/google")
    public ResponseEntity<?> google(@RequestBody GoogleAuthRequest request) {
        String idToken = request.idToken();
        log.info("🔵 Google login attempt");

        GoogleIdToken.Payload payload;
        try {
            payload = googleAuthService.verifyToken(idToken);
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

        return ResponseEntity.ok(
                new ApiResponse<>(true, "", ResponseType.NONE,
                        Map.of(
                                "accessToken", accessToken,
                                "refreshToken", refreshToken
                        )
                )
        );
    }
}
