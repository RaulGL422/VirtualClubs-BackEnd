package galindo.raul.virtualclubs.config;

import galindo.raul.virtualclubs.models.entities.AuthProviderEntity;
import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.repositories.RoleEntityRepository;
import galindo.raul.virtualclubs.repositories.UserEntityRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;

@Component
@Profile("sandbox")
@RequiredArgsConstructor
@Slf4j
public class SandboxDataInitializer implements ApplicationRunner {

    static final String SANDBOX_EMAIL    = "test@sandbox.local";
    static final String SANDBOX_PASSWORD = "VCtest123!";
    private static final int H2_CONSOLE_PORT = 8082;

    private final RoleEntityRepository roleRepository;
    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${server.port:8080}")
    private int serverPort;

    private Object h2WebServer;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedTestUser();
        startH2Console();
        printBanner();
    }

    @PreDestroy
    public void stopH2Console() {
        if (h2WebServer == null) return;
        try {
            h2WebServer.getClass().getMethod("stop").invoke(h2WebServer);
        } catch (Exception ignored) {
        }
    }

    private void startH2Console() {
        try {
            Class<?> serverClass = Class.forName("org.h2.tools.Server");
            Method createWebServer = serverClass.getMethod("createWebServer", String[].class);
            h2WebServer = createWebServer.invoke(null,
                    (Object) new String[]{"-web", "-webPort", String.valueOf(H2_CONSOLE_PORT), "-webDaemon"});
            serverClass.getMethod("start").invoke(h2WebServer);
        } catch (Exception e) {
            log.warn("[SANDBOX] No se pudo arrancar la consola H2: {}", e.getMessage());
        }
    }

    private void printBanner() {
        String ip = resolveLocalIp();
        log.info("""

                ╔══════════════════════════════════════════════╗
                ║       SANDBOX — Usuario de prueba listo      ║
                ║                                              ║
                ║  Email:    test@sandbox.local                ║
                ║  Password: VCtest123!  (verificado ✓)        ║
                ╚══════════════════════════════════════════════╝

                  App      →  http://{}:{}
                  Swagger  →  http://{}:{}/swagger-ui/index.html
                  H2       →  http://{}:{}  (JDBC: jdbc:h2:mem:virtualclubs)
                """, ip, serverPort, ip, serverPort, ip, H2_CONSOLE_PORT);
    }

    private String resolveLocalIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            // No se conecta realmente — solo pregunta al SO qué interfaz usaría
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(RoleEntity.builder().role(Role.USER).build());
            roleRepository.save(RoleEntity.builder().role(Role.ADMIN).build());
            log.debug("[SANDBOX] Roles USER y ADMIN creados");
        }
    }

    private void seedTestUser() {
        if (userRepository.findByEmail(SANDBOX_EMAIL).isPresent()) return;

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
        log.debug("[SANDBOX] Usuario de prueba creado: {}", SANDBOX_EMAIL);
    }
}
