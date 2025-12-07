package galindo.raul.virtualclubs.models.exceptions;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {
  private final String email;
  
  public UserNotFoundException(String email) {
    this.email = email;
  }
}
