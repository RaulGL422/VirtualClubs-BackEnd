package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for {@link UserEntity} instances.
 * Provides standard CRUD operations and custom queries for user management.
 */
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return An Optional containing the user if found.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Finds a user by email eagerly loading roles, their permissions and auth providers
     * in a single query to avoid N+1 queries in {@code loadUserByUsername}.
     * @param email The email to search for.
     * @return An Optional containing the user with all security-related associations loaded.
     */
    @EntityGraph(attributePaths = {"roles", "roles.permissionList", "authProviderEntities"})
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithRolesAndProviders(@Param("email") String email);

    /**
     * Finds a user based on OAuth2 provider information or email.
     * This is used to link social accounts to existing local accounts.
     * @param providerName The name of the provider (e.g., "google", "github").
     * @param providerUserId The unique ID provided by the OAuth2 provider.
     * @param email The email address associated with the provider account.
     * @return An Optional containing the user if a match is found.
     */
    @Query("SELECT u FROM UserEntity u JOIN u.authProviderEntities ap " +
            "WHERE ap.providerName = :providerName AND " +
            "(ap.providerUserId = :providerUserId OR u.email = :email)")
    Optional<UserEntity> findByProvider(@Param("providerName") String providerName,
                                        @Param("providerUserId") String providerUserId,
                                        @Param("email") String email);
}
