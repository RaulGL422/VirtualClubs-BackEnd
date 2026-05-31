package galindo.raul.virtualclubs.config;

import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.repositories.RoleEntityRepository;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Profile("sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxDataInitializer implements ApplicationRunner {

    static final String SANDBOX_EMAIL    = "test@sandbox.local";
    static final String SANDBOX_PASSWORD = "VCtest123!";

    private final RoleEntityRepository roleRepository;
    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedTestUser();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(RoleEntity.builder().role(Role.USER).build());
            roleRepository.save(RoleEntity.builder().role(Role.ADMIN).build());
            log.debug("[SANDBOX] Roles USER y ADMIN creados");
        }
    }

    private void seedTestUser() {
        if (userRepository.findByEmail(SANDBOX_EMAIL).isPresent()) {
            return;
        }

        RoleEntity userRole = roleRepository.findByRole(Role.USER)
                .orElseThrow(() -> new IllegalStateException("[SANDBOX] Role USER no encontrado"));

        UserEntity user = UserEntity.builder()
                .email(SANDBOX_EMAIL)
                .emailVerified(true)
                .emailVerifiedAt(Instant.now())
                .build();

        user.addRole(userRole);

        AuthProviderEntity localProvider = AuthProviderEntity.builder()
                .providerName("LOCAL")
                .passwordHash(passwordEncoder.encode(SANDBOX_PASSWORD))
                .build();

        user.addAuthProvider(localProvider);
        userRepository.save(user);

        log.info("""

                ╔══════════════════════════════════════════════════════╗
                ║          SANDBOX — Usuario de prueba listo           ║
                ║                                                      ║
                ║  Email:      test@sandbox.local                      ║
                ║  Password:   VCtest123!                              ║
                ║  Estado:     email verificado ✓                      ║
                ║                                                      ║
                ║  Consola H2: http://localhost:8080/h2-console/       ║
                ║  JDBC URL:   jdbc:h2:mem:virtualclubs                ║
                ║  Swagger:    http://localhost:8080/swagger-ui/index.html ║
                ╚══════════════════════════════════════════════════════╝
                """);
    }
}
