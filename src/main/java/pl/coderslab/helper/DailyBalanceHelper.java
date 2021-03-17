package pl.coderslab.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;

import java.sql.Date;
import java.time.LocalDate;

@Component
public class DailyBalanceHelper {

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    public void updateActualDailyBalance(User user){
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
        if(dailyBalance != null){
            dailyBalance.setTotalProtein(user.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(user.getTotalCarbohydrates());
            dailyBalance.setTotalFat(user.getTotalFat());
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
    }
}
