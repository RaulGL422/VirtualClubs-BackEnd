package galindo.raul.virtualclubs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class EmailProperties {

  private String from;
  private String fromName;
  private Token token = new Token();

  @Getter
  @Setter
  public static class Token {
    private int verificationExpiry = 1440;
    private int resetExpiry = 30;
  }
}