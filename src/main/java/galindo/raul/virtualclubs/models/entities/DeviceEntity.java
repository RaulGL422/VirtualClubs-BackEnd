package galindo.raul.virtualclubs.models.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a physical device registered by a user.
 * A device is uniquely identified by the combination of deviceId and user,
 * allowing the same physical device to be associated with only one user.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device", uniqueConstraints = @UniqueConstraint(columnNames = {"device_id", "user_id"}))
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "ip_address")
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
