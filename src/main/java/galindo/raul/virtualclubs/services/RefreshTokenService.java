package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.models.entities.RefreshToken;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.repositories.RefreshTokenRepository;
import galindo.raul.virtualclubs.repositories.VirtualClubsUsersDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final VirtualClubsUsersDetailsRepository userRepository;

    public void createRefreshToken(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }

    public String getEmailFromRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        return refreshToken.getUser().getEmail();
    }
}
