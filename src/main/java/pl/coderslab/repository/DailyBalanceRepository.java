package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Meal;

import java.util.List;


@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {
}
