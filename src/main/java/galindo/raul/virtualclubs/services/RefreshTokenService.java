package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.RefreshToken;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.models.exceptions.EmailNotFoundException;
import galindo.raul.virtualclubs.models.exceptions.RefreshTokenException;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final VirtualClubsUsersDetailsRepository userRepository;

    public void createOrUpdateRefreshToken(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(email));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .build();

        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.save(refreshToken);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);
    }

    public String getEmailFromRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(RefreshTokenException::new);
        return refreshToken.getUser().getEmail();
    }
}
