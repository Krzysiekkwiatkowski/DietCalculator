package my.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import my.application.entity.Role;

import java.util.Collection;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    @Query(nativeQuery = true, value = "SELECT role.id, role.name FROM users_roles JOIN role on users_roles.role_id = role.id where users_roles.user_id = ?1")
    Collection<Role> findAllRolesByUserId(Long id);
}
