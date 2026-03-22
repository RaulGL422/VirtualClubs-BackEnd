package galindo.raul.virtualclubs;

import galindo.raul.virtualclubs.services.GoogleAuthService;
import galindo.raul.virtualclubs.services.MailerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({"dev", "test"})
class VirtualClubsApplicationTests {

	// Evita que el contexto intente conectarse a Google o a un servidor SMTP
	@MockBean GoogleAuthService googleAuthService;
	@MockBean MailerService mailerService;

	@Test
	void contextLoads() {
	}

}
