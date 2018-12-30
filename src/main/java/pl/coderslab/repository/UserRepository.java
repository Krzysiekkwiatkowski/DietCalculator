package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findTopByEmail(String email);
    User findTopById(Long id);
    List<Meal> findAllByDailyBalances(Long id);
}
