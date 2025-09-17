package galindo.raul.virtualclubs.services;

import galindo.raul.virtualclubs.repositories.UserTokenRepository;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.entities.User;
import galindo.raul.virtualclubs.models.entities.UserToken;
import galindo.raul.virtualclubs.util.TokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final UserTokenRepository tokenRepository;

    @Transactional
    public String createTokenFor(User user, TokenType type, Duration validFor, String meta) {
        tokenRepository.deleteAllByUserAndType(user, type);

        String token = TokenUtils.generateTokenString(32);
        String tokenHash = TokenUtils.sha256Hex(token);

        UserToken ut = UserToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .type(type)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(validFor))
                .consumed(false)
                .meta(meta)
                .build();

        tokenRepository.save(ut);
        return token; // devuelves este token crudo para enviar por email
    }

    @Transactional
    public Optional<User> validateAndConsume(String token, TokenType type) {
        String hash = TokenUtils.sha256Hex(token);
        return tokenRepository.findByTokenHashAndType(hash, type)
                .filter(t -> !t.isConsumed())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(t -> {
                    t.setConsumed(true);
                    tokenRepository.save(t);
                    return t.getUser();
                });
    }

    @Transactional
    public void invalidateTokens(User user, TokenType type) {
        tokenRepository.deleteAllByUserAndType(user, type);
    }
}
