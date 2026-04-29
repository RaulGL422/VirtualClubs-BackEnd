package galindo.raul.virtualclubs.config;

import galindo.raul.virtualclubs.models.entities.RoleEntity;
import galindo.raul.virtualclubs.models.enums.Role;
import galindo.raul.virtualclubs.repositories.RoleEntityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestRoleSeeder {

    private final RoleEntityRepository roleRepository;

    public TestRoleSeeder(RoleEntityRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void seed() {
        for (Role role : Role.values()) {
            if (roleRepository.findByRole(role).isEmpty()) {
                roleRepository.save(RoleEntity.builder().role(role).build());
            }
        }
    }
}
