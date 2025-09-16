package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.UserToken;
import galindo.raul.virtualclubs.models.enums.TokenType;
import galindo.raul.virtualclubs.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByTokenHashAndType(String tokenHash, TokenType type);
    void deleteAllByUserAndType(User user, TokenType type);
}
