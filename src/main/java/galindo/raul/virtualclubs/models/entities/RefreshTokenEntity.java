package galindo.raul.virtualclubs.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
    
    @Column(nullable = false)
    private String deviceId;
    
    @Column
    private String deviceName;
    
    @Column
    private String ipAddress;
    
    @Column
    private String deviceType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
}