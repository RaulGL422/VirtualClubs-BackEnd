package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VirtualClubsUsersDetailsRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.authProviders ap " +
            "WHERE ap.providerName = :providerName AND " +
            "(ap.providerUserId = :providerUserId OR u.email = :email)")
    Optional<User> findByProvider(@Param("providerName") String providerName,
                                                              @Param("providerUserId") String providerUserId,
                                                              @Param("email") String email);
}
