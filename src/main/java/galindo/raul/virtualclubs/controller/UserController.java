package galindo.raul.virtualclubs.controller;

import galindo.raul.virtualclubs.services.VirtualClubsUsersDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final VirtualClubsUsersDetailsService userDetailsService;

//    @PostMapping("/getUserInfo")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserInfo(Authentication authentication) {
//        /** TODO Este metodo debe devolver:
//         * - Informacion del usuario
//         * - Si email_verified es false solo devuelve eso, para evitar enviar informacion comprometida o inutil cuando no debe
//         * - Informacion de club personal
//         * - Informacion de clubes inscritos
//         */
//        String email = authentication.getName();
//        Optional<User> maybeUser = userDetailsService.findByEmailOptional(email);
//
//        if (maybeUser.isEmpty())
//          throw new EmailNotFoundException(email);
//    }
}
