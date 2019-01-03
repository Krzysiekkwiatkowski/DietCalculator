package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.User;

import java.sql.Date;
import java.util.List;

@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {
    int countByUserIdAndDate(Long id, Date date);
    DailyBalance findTopByUserIdAndAndDate(Long id, Date date);
    List<DailyBalance> findAllByUser(User user);
    void deleteAllByUser(User user);
}
