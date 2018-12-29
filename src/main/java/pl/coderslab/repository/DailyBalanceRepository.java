package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.DailyBalance;

import java.sql.Date;

@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {
    int countByUserIdAndDate(Long id, Date date);
    DailyBalance findTopByUserIdAndAndDate(Long id, Date date);
}
