package galindo.raul.virtualclubs;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the VirtualClubs Spring Boot application.
 */
@SpringBootApplication
public class VirtualClubsApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 * It loads environment variables from a .env file before launching the context.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
		
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
		
		SpringApplication.run(VirtualClubsApplication.class, args);
	}
}
