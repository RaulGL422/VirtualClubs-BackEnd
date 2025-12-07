package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.dtos.ApiResponse;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.models.enums.ResponseType;
import galindo.raul.virtualclubs.models.exceptions.EmailNotVerifiedException;
import galindo.raul.virtualclubs.models.exceptions.UserNotFoundException;
import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  
  private final VirtualClubsUsersDetailsService userDetailsService;
  
  @GetMapping("/getUserInfo")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo(Authentication authentication) {
    /** TODO Este metodo debe devolver:
     * - Informacion del usuario
     * - Si email_verified es false solo devuelve eso, para evitar enviar informacion comprometida o inutil cuando no debe
     * - Informacion de club personal
     * - Informacion de clubes inscritos
     */
    String email = authentication.getName();
    Optional<User> maybeUser = userDetailsService.findByEmailOptional(email);
    
    if (maybeUser.isEmpty())
      throw new UserNotFoundException(email);
    
    User user = maybeUser.get();
    if (!user.isEmailVerified())
      throw new EmailNotVerifiedException(email);
    
    return ResponseEntity.ok(new ApiResponse<>(true, null, ResponseType.NONE, Map.of()));
  }
}
