package galindo.raul.virtualclubs.repositories;

import galindo.raul.virtualclubs.models.entities.DeviceEntity;
import galindo.raul.virtualclubs.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link DeviceEntity} persistence.
 */
public interface DeviceRepository extends JpaRepository<DeviceEntity, Long> {

    /**
     * Finds a device by its identifier and owner user.
     *
     * @param deviceId the unique device identifier
     * @param user     the user associated with the device
     * @return an Optional containing the device entity if found
     */
    Optional<DeviceEntity> findByDeviceIdAndUser(String deviceId, UserEntity user);
}
