package galindo.raul.virtualclubs.config.security;

import tools.jackson.databind.ObjectMapper;
import galindo.raul.virtualclubs.config.CorsProperties;
import galindo.raul.virtualclubs.config.security.filters.JwtAuthFilter;
import galindo.raul.virtualclubs.config.security.filters.JwtAuthLoginFilter;
import galindo.raul.virtualclubs.config.security.filters.RateLimitingFilter;
import galindo.raul.virtualclubs.config.security.filters.models.RateLimitingProperties;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for Spring Security.
 * Sets up authentication, authorization, and JWT filter chains.
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitingProperties.class)
public class SecurityConfig {
  private final TokensService tokensService;
  private final JwtUtils jwtUtils;
  private final UserService userEntityService;
  private final ObjectMapper objectMapper;
  private final RateLimitingProperties rateLimitingProperties;
  private final CorsProperties corsProperties;
  
  /**
   * Configures the security filter chain.
   * Defines CSRF protection, session management, URL authorization rules,
   * and registers custom JWT filters.
   *
   * @param http                  The HttpSecurity object to configure.
   * @param authenticationManager The AuthenticationManager used by the login filter.
   * @return The configured SecurityFilterChain.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) {
    JwtAuthLoginFilter jwtAuthLoginFilter = new JwtAuthLoginFilter(authenticationManager, userEntityService, tokensService, objectMapper);
    jwtAuthLoginFilter.setFilterProcessesUrl("/v1/auth/login");

    JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userEntityService, objectMapper);
    RateLimitingFilter rateLimitingFilter = new RateLimitingFilter(objectMapper, rateLimitingProperties.limits());

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/v1/auth/login",
                "/v1/auth/register",
                "/v1/auth/refresh",
                "/v1/auth/requestPasswordReset",
                "/v1/auth/resetPasswordRedirect",
                "/v1/auth/resetPassword",
                "/v1/auth/verify",
                "/v1/auth/google"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAt(jwtAuthLoginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitingFilter, JwtAuthFilter.class);
    return http.build();
  }
  
  /**
   * Exposes the AuthenticationManager as a Bean.
   *
   * @param config The AuthenticationConfiguration provided by Spring.
   * @return The AuthenticationManager instance.
   */
  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    return config.getAuthenticationManager();
  }

  /**
   * CORS configuration source used by the security filter chain.
   * Defined here (not in WebMvcConfigurer) so Spring Security procesa las
   * peticiones preflight (OPTIONS) antes de que lleguen a los filtros de auth.
   */
  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(corsProperties.allowedOrigins()));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}