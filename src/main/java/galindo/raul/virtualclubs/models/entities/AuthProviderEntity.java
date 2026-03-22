package galindo.raul.virtualclubs.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auth_providers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider_name", "provider_user_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthProviderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "provider_name", nullable = false)
    private String providerName; // 'LOCAL', 'GOOGLE', etc.

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "password_hash")
    private String passwordHash;
}
