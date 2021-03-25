package my.application.helper;

import my.application.entity.DailyBalance;
import my.application.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import my.application.pojo.DailyBalanceData;
import my.application.repository.DailyBalanceRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class DailyBalanceHelper {

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @Autowired
    private DailyBalanceData result;

    public void updateActualDailyBalance(User user){
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
        if(dailyBalance != null){
            dailyBalance.setTotalProtein(user.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(user.getTotalCarbohydrates());
            dailyBalance.setTotalFat(user.getTotalFat());
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
    }

    public DailyBalanceData getBalance(List<DailyBalance> dailyBalances, boolean needGlycemicCharges){
        dailyBalances.forEach(d -> {
            result.getNeededMacro().setProtein(result.getNeededMacro().getProtein() + d.getTotalProtein());
            result.getNeededMacro().setCarbohydrates(result.getNeededMacro().getCarbohydrates() + d.getTotalCarbohydrates());
            result.getNeededMacro().setFat(result.getNeededMacro().getFat() + d.getTotalFat());
            result.getNeededMacro().setCalories(result.getNeededMacro().getCalories() + d.getNeeded());
            d.getMeals().forEach(m -> {
                result.getReceivedMacro().setProtein(result.getReceivedMacro().getProtein() + m.getTotalProtein());
                result.getReceivedMacro().setCarbohydrates(result.getReceivedMacro().getCarbohydrates() + m.getTotalCarbohydrates());
                result.getReceivedMacro().setFat(result.getReceivedMacro().getFat() + m.getTotalFat());
                result.getReceivedMacro().setCalories(result.getReceivedMacro().getCalories() + m.getTotalCalories());
                if(needGlycemicCharges){
                    result.addData(m.getGlycemicCharge());
                }
            });
            if(needGlycemicCharges) {
                result.submit();
            }
        });
        return result;
    }
}
