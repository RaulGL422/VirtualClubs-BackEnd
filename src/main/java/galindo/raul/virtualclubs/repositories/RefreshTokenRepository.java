package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.RefreshTokenEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing {@link RefreshTokenEntity} persistence.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Deletes all refresh tokens associated with a specific user.
     *
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(UserEntity user);

    /**
     * Finds all active (non-revoked) refresh tokens for a specific user.
     *
     * @param user the user to search tokens for
     * @return a list of non-revoked refresh tokens
     */
    List<RefreshTokenEntity> findByUserAndRevokedFalse(UserEntity user);

    /**
     * Checks if a specific token exists for a user and is not revoked.
     *
     * @param token the token string to verify
     * @param user  the user associated with the token
     * @return true if a valid token exists, false otherwise
     */
    boolean existsByTokenAndUserAndRevokedFalse(String token, UserEntity user);
    
    List<RefreshTokenEntity> findByUserAndDeviceIdAndRevokedFalse(UserEntity user, String deviceId);

    /**
     * Deletes all refresh tokens for a specific user and device.
     *
     * @param user     the user associated with the tokens
     * @param deviceId the device identifier
     */
    void deleteByUserAndDeviceId(UserEntity user, String deviceId);
}
