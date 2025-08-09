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
public class AuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "provider_name", nullable = false)
    private String providerName; // 'LOCAL', 'GOOGLE', etc.

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "password_hash")
    private String passwordHash;
}
