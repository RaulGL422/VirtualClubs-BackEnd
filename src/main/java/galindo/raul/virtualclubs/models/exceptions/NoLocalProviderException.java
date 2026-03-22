package galindo.raul.virtualclubs.models.exceptions;

import lombok.Getter;

@Getter
public class NoLocalProviderException extends RuntimeException {
  private final String email;
  public NoLocalProviderException(String email) {
    this.email = email;
  }
}
