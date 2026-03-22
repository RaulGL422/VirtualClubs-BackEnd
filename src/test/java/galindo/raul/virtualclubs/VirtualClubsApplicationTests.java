package galindo.raul.virtualclubs;

import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.MailerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({"dev", "test"})
class VirtualClubsApplicationTests {

	// Evita que el contexto intente conectarse a Google o a un servidor SMTP
	@MockitoBean GoogleAuthService googleAuthService;
	@MockitoBean MailerService mailerService;

	@Test
	void contextLoads() {
	}

}
