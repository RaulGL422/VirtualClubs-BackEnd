package galindo.raul.virtualclubs.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncoderConfig {
  /**
   * Defines the password encoder bean.
   * Uses BCrypt hashing algorithm for secure password storage.
   *
   * @return A BCryptPasswordEncoder instance.
   */
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
