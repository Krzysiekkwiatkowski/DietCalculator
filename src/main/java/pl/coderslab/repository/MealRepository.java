package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.Meal;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    Meal findTopById(Long id);
    @Query(nativeQuery = true, value = "SELECT * FROM daily_balance_meals JOIN meal ON daily_balance_meals.meals_id = meal.id WHERE daily_balance_meals.daily_balance_id = ?1 ORDER BY id ASC")
    List<Meal> findAllById(long id);
}
