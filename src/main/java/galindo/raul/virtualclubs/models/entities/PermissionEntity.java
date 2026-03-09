package galindo.raul.virtualclubs.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false, unique = true, updatable = false)
  private String name;
  
  @Column(nullable = false)
  private String description;
}
