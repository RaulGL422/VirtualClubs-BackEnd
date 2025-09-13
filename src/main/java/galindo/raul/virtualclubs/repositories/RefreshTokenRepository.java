package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.RefreshToken;
import galindo.raul.virtualclubs.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

}
