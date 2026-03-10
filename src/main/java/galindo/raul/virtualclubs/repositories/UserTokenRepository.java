//package galindo.raul.virtualclubs.repositories;
//
//import galindo.raul.virtualclubs.models.entities.UserEntity;
//import galindo.raul.virtualclubs.models.entities.UserTokenEntity;
//import galindo.raul.virtualclubs.models.enums.TokenType;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
///**
// * Repository interface for managing {@link UserTokenEntity} instances.
// */
//public interface UserTokenRepository extends JpaRepository<UserTokenEntity, Long> {
//
//    /**
//     * Finds a user token by its hash and type.
//     *
//     * @param tokenHash the hashed token string
//     * @param type      the type of the token (e.g., REFRESH, VERIFICATION)
//     * @return an Optional containing the found token, or empty if not found
//     */
//    Optional<UserTokenEntity> findByTokenHashAndType(String tokenHash, TokenType type);
//
//    /**
//     * Deletes all tokens associated with a specific user and token type.
//     *
//     * @param user the user whose tokens should be deleted
//     * @param type the type of tokens to remove
//     */
//    void deleteAllByUserAndType(UserEntity user, TokenType type);
//}
