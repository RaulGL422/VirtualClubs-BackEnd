package galindo.raul.virtualclubs;

import galindo.raul.virtualclubs.config.CorsProperties;
import galindo.raul.virtualclubs.config.JwtProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the VirtualClubs Spring Boot application.
 */
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
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
