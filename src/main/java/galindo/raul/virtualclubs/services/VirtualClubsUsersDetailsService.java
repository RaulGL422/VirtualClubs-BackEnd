package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProvider;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VirtualClubsUsersDetailsService implements UserDetailsService {

    private final VirtualClubsUsersDetailsRepository userRepository;

    public VirtualClubsUsersDetailsService(VirtualClubsUsersDetailsRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(STR."User not found: \{email}"));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    // Asegura prefijo ROLE_
                    if (!role.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority(STR."ROLE_\{role.toUpperCase()}");
                    } else {
                        return new SimpleGrantedAuthority(role.toUpperCase());
                    }
                })
                .collect(Collectors.toSet());

        // Aquí usamos la contraseña del proveedor LOCAL si existe, si no, cadena vacía
        String password = user.getAuthProviders().stream()
                .filter(ap -> "LOCAL".equalsIgnoreCase(ap.getProviderName()))
                .findFirst()
                .map(AuthProvider::getPasswordHash)
                .orElse("");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                authorities
        );
    }

    @Transactional
    public User registerOrLoadUserWithGoogle(String email, String name, String googleId) {
        // 1. Buscar usuario con Google providerId o email
        Optional<User> existingUserOpt = userRepository.findByProvider(
                "GOOGLE", googleId, email);

        if (existingUserOpt.isPresent()) {
            // Usuario encontrado (login)
            return existingUserOpt.get();
        }

        // 2. No existe usuario, crear nuevo
        User newUser = User.builder()
                .email(email)
                .name(name)
                .roles(new HashSet<>(List.of("ROLE_USER")))
                .createdAt(Instant.now())
                .build();

        AuthProvider googleProvider = AuthProvider.builder()
                .providerName("GOOGLE")
                .providerUserId(googleId)
                .user(newUser)
                .build();

        newUser.getAuthProviders().add(googleProvider);

        return userRepository.save(newUser);
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
