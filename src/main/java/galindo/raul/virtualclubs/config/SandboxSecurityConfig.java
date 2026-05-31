package galindo.raul.virtualclubs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Exposes the H2 web console at /h2-console when running the sandbox profile.
 * Declared at @Order(1) so it takes precedence over the main SecurityFilterChain
 * for H2 console paths only.
 */
@Configuration
@Profile("sandbox")
public class SandboxSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/h2-console", "/h2-console/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console", "/h2-console/**"))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }
}
