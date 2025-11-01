package galindo.raul.virtualclubs.models.exceptions;

import lombok.Getter;

@Getter
public class UserAlreadyExistException extends RuntimeException {
  private final String email;
  
  public UserAlreadyExistException(String email) {
    this.email = email;
  }
}