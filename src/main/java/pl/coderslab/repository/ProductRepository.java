package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.Category;
import pl.coderslab.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1%")
    List<Product> findByNameContaining(String name);
    Product findTopById(Long id);
    @Query(nativeQuery = true, value = "SELECT * FROM product ORDER BY name LIMIT ?1 OFFSET ?2")
    List<Product> findAll(int limit, int offset);
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM product")
    int countAllProduct();
}
