package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.Require;

@Repository
public interface RequireRepository extends JpaRepository<Require, Long> {
}
