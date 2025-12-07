package galindo.raul.virtualclubs.models.exceptions;

import lombok.Getter;

@Getter
public class EmailNotVerifiedException extends RuntimeException {
  private final String email;
  
  public EmailNotVerifiedException(String email) {
    this.email = email;
  }
}
