package my.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import my.application.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findTopById(Long id);
}
