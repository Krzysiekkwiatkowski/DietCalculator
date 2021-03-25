package my.application.repository;

import my.application.entity.DailyBalance;
import my.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {
    int countByUserIdAndDate(Long id, Date date);
    DailyBalance findTopByUserIdAndDate(Long id, Date date);
    List<DailyBalance> findAllByUser(User user);
    @Query(nativeQuery = true, value = "SELECT * FROM daily_balance WHERE user_id = ?1 AND DATE < ?2 AND DATE >= ?3 ORDER BY id DESC")
    List<DailyBalance> findAllByUserToDate(User user, Date date, Date dateTo);
    @Query(nativeQuery = true, value = "SELECT * FROM daily_balance WHERE user_id = ?1 AND DATE < ?2 AND DATE >= ?3 ORDER BY id DESC")
    List<DailyBalance> findAllByUserToDate(User user, Date date, Date dateTo, int limit);
    void deleteAllByUser(User user);
}
