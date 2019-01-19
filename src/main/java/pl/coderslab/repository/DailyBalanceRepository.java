package pl.coderslab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query(nativeQuery = true, value = "SELECT * FROM daily_balance WHERE user_id = ?1 AND DATE < ?2 ORDER BY id DESC LIMIT 7")
    List<DailyBalance> findAllByUserAndDate(User user, Date date);
    @Query(nativeQuery = true, value = "SELECT * FROM daily_balance WHERE user_id = ?1 AND DATE < ?2 ORDER BY id ASC LIMIT ?3")
    List<DailyBalance> findAllByUserAndDate(User user, Date date, int limit);
    void deleteAllByUser(User user);
}
