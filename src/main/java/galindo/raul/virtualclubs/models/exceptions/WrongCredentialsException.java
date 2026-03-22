package galindo.raul.virtualclubs.models.exceptions;

import lombok.Getter;

@Getter
public class WrongCredentialsException extends RuntimeException {
  private final String email;
  
  public WrongCredentialsException(String email) {
    this.email = email;
  }
}