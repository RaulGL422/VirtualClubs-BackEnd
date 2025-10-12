package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.RefreshToken;
import galindo.raul.virtualclubs.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

}
