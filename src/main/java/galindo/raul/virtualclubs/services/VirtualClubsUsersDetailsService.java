package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.AuthProvider;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualClubsUsersDetailsService implements UserDetailsService {

    private final VirtualClubsUsersDetailsRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    if (!role.startsWith("ROLE_")) {
                        return new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                    } else {
                        return new SimpleGrantedAuthority(role.toUpperCase());
                    }
                })
                .collect(Collectors.toSet());

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
        log.info("🔵 Processing Google login/register for email='{}'", email);

        // 1. Buscar usuario con Google provider
        Optional<User> googleUserOpt = userRepository.findByProvider("GOOGLE", googleId, email);
        if (googleUserOpt.isPresent()) {
            log.info("✅ Found existing Google user '{}'", email);
            return googleUserOpt.get();
        }

        // 2. Buscar usuario con LOCAL
        Optional<User> localUserOpt = userRepository.findByEmail(email);
        if (localUserOpt.isPresent()) {
            User localUser = localUserOpt.get();
            if (localUser.isEmailVerified()) {
                log.info("🔗 Linking Google provider to existing verified LOCAL account '{}'", email);
                addGoogleProvider(localUser, googleId);
                return userRepository.save(localUser);
            } else {
                log.warn("⚠️ Local account '{}' is unverified, deleting and creating Google account", email);
                userRepository.delete(localUser);
            }
        }

        // 3. Crear nuevo usuario con Google
        log.info("🆕 Creating new user with Google account '{}'", email);
        User newUser = createGoogleUser(email, name, googleId);
        return userRepository.save(newUser);
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    // -------------------------------
    // Métodos auxiliares
    // -------------------------------

    private void addGoogleProvider(User user, String googleId) {
        AuthProvider googleProvider = AuthProvider.builder()
                .providerName("GOOGLE")
                .providerUserId(googleId)
                .user(user)
                .build();
        user.addAuthProvider(googleProvider);
    }

    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    private User createGoogleUser(String email, String name, String googleId) {
        User user = User.builder()
                .email(email)
                .name(name)
                .roles(new HashSet<>(List.of("ROLE_USER")))
                .createdAt(Instant.now())
                .emailVerified(true) // Google ya valida el correo
                .emailVerifiedAt(Instant.now())
                .build();

        addGoogleProvider(user, googleId);
        return user;
    }
}
