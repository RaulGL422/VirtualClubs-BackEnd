package galindo.raul.virtualclubs.config.security;

import galindo.raul.virtualclubs.config.security.filters.JwtAuthFilter;
import galindo.raul.virtualclubs.config.security.filters.JwtAuthLoginFilter;
import galindo.raul.virtualclubs.config.security.utils.JwtUtils;
import galindo.raul.virtualclubs.services.TokensService;
import galindo.raul.virtualclubs.services.UserEntityServiceImpl;
import lombok.RequiredArgsConstructor;
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

/**
 * Configuration class for Spring Security.
 * Sets up authentication, authorization, and JWT filter chains.
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final TokensService tokensService;
  private final JwtUtils jwtUtils;
  private final UserEntityServiceImpl userEntityService;
  
  /**
   * Configures the security filter chain.
   * Defines CSRF protection, session management, URL authorization rules,
   * and registers custom JWT filters.
   *
   * @param http                  The HttpSecurity object to configure.
   * @param authenticationManager The AuthenticationManager used by the login filter.
   * @return The configured SecurityFilterChain.
   * @throws Exception If an error occurs during configuration.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
    JwtAuthLoginFilter jwtAuthLoginFilter = new JwtAuthLoginFilter(authenticationManager, userEntityService, tokensService);
    jwtAuthLoginFilter.setFilterProcessesUrl("/v1/auth/login");
    
    JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils, userEntityService);
    
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/v1/auth/login",
                "/v1/auth/register",
                "/v1/auth/refresh"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterAt(jwtAuthLoginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  
  /**
   * Exposes the AuthenticationManager as a Bean.
   *
   * @param config The AuthenticationConfiguration provided by Spring.
   * @return The AuthenticationManager instance.
   * @throws Exception If the manager cannot be retrieved.
   */
  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}