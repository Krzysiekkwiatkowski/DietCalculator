package my.application.controller;

import my.application.entity.DailyBalance;
import my.application.entity.Meal;
import my.application.entity.User;
import my.application.helper.ContextHelper;
import my.application.helper.DailyBalanceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import my.application.helper.NumberHelper;
import my.application.pojo.DailyBalanceData;
import my.application.pojo.ExtendMacro;
import my.application.pojo.GraphResult;
import my.application.repository.DailyBalanceRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestMapping("/diet/daily")
@Controller
public class DailyBalanceController {

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @Autowired
    private DailyBalanceHelper dailyBalanceHelper;

    @Autowired
    private NumberHelper numberHelper;

    @RequestMapping("/actual")
    public String actualBalance(Model model){
        User user = ContextHelper.getUserFromContext();
        double totalProtein = user.getTotalProtein();
        double totalCarbohydrates = user.getTotalCarbohydrates();
        double totalFat = user.getTotalFat();
        int totalCalories = user.getTotalCalories();
        if(dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now())) == null){
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            return "home";
        }
        ExtendMacro extendMacro = new ExtendMacro();
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndDate(user.getId(), Date.valueOf(LocalDate.now()));
        List<Meal> meals = dailyBalance.getMeals();
        meals.sort((m1, m2) -> Integer.compare(m1.getMealNumber(), m2.getMealNumber()));
        List<Double> glycemicCharges = new ArrayList<>();
        meals.forEach(m -> {
            extendMacro.setProtein(extendMacro.getProtein() + m.getTotalProtein());
            extendMacro.setCarbohydrates(extendMacro.getCarbohydrates() + m.getTotalCarbohydrates());
            extendMacro.setFat(extendMacro.getFat() + m.getTotalFat());
            extendMacro.setCalories(extendMacro.getCalories() + m.getTotalCalories());
            glycemicCharges.add(m.getGlycemicCharge());
        });
        int protein = (int) (extendMacro.getProtein() * 100 / totalProtein);
        int carbohydrates = (int) (extendMacro.getCarbohydrates() * 100 / totalCarbohydrates);
        int fat = (int) (extendMacro.getFat() * 100 / totalFat);
        int calories = extendMacro.getCalories() * 100 / totalCalories;
        List<GraphResult> results = new ArrayList<>();
        results.add(new GraphResult("Białko: " + formatMacroData(extendMacro.getProtein(), totalProtein), protein + "%", "width: " + (protein * 3) + "px; background-color: green;", true));
        results.add(new GraphResult("Węglowodany: " + formatMacroData(extendMacro.getCarbohydrates(), totalCarbohydrates), carbohydrates + "%", "width: " + (carbohydrates * 3) + "px; background-color: red;", true));
        results.add(new GraphResult("Tłuszcz: " + formatMacroData(extendMacro.getFat(), totalFat), fat + "%", "width: " + (fat * 3) + "px; background-color: yellow;", true));
        results.add(new GraphResult("Kalorie: " + extendMacro.getCalories() + "/" + totalCalories, calories + "%", "width: " + (calories * 3) + "px; background-color: blue;", true));
        results.add(new GraphResult("%", "", "", false));
        for(int i = 0; i < glycemicCharges.size(); i++){
            results.add(new GraphResult("Posiłek " + (i + 1), numberHelper.roundDouble(glycemicCharges.get(i)) + "","width: " + ((int)(glycemicCharges.get(i) * 30) / 2) + "px; background-color: orange;", true));
        }
        results.add(new GraphResult("", "", "", false));
        model.addAttribute("results", results);
        model.addAttribute("exist", "exist");
        model.addAttribute("balance", "balance");
        return "home";
    }

    @RequestMapping(value = "/weekly")
    public String last(Model model){
        User user = ContextHelper.getUserFromContext();
        Object dailyObject = dailyBalanceRepository.findAllByUserToDate(user, Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().minusDays(7)));
        if(dailyObject == null || ((List) dailyObject).size() == 0){
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            model.addAttribute("days", 0);
            return "home";
        }
        List<DailyBalance> dailyBalances = (List<DailyBalance>)dailyObject;
        int days = dailyBalances.size();
        DailyBalanceData data = dailyBalanceHelper.getBalance(dailyBalances, true);
        if((data.getNeededMacro().getProtein() == 0) || (data.getNeededMacro().getCarbohydrates() == 0) || (data.getNeededMacro().getFat() == 0) || (data.getNeededMacro().getCalories() == 0)){
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            model.addAttribute("days", 0);
            return "home";
        }
        int protein = (int) (data.getReceivedMacro().getProtein() * 100 / data.getNeededMacro().getProtein());
        int carbohydrates = (int) (data.getReceivedMacro().getCarbohydrates() * 100 / data.getNeededMacro().getCarbohydrates());
        int fat = (int) (data.getReceivedMacro().getFat() * 100 / data.getNeededMacro().getFat());
        int calories = data.getReceivedMacro().getCalories() * 100 / data.getNeededMacro().getCalories();
        List<GraphResult> results = new ArrayList<>();
        results.add(new GraphResult("Białko: " + formatMacroData(data.getReceivedMacro().getProtein(), data.getNeededMacro().getProtein()), protein + "%", "width: " + (protein * 3) + "px; background-color: green;", true));
        results.add(new GraphResult("Węglowodany: " + formatMacroData(data.getReceivedMacro().getCarbohydrates(), data.getNeededMacro().getCarbohydrates()), carbohydrates + "%", "width: " + (carbohydrates * 3) + "px; background-color: red;", true));
        results.add(new GraphResult("Tłuszcz: " + formatMacroData(data.getReceivedMacro().getFat(), data.getNeededMacro().getFat()), fat + "%", "width: " + (fat * 3) + "px; background-color: yellow;", true));
        results.add(new GraphResult("Kalorie: " + data.getReceivedMacro().getCalories() + "/" + data.getNeededMacro().getCalories(), calories + "%", "width: " + (calories * 3) + "px; background-color: blue;", true));
        results.add(new GraphResult("%", "", "", false));
        List<Object> objectsList = data.getDataList();
        for(int i = 0; i < objectsList.size(); i++){
            double number = (Double) objectsList.get(i);
            results.add(new GraphResult("Dzień " + (i + 1), numberHelper.roundDouble(number) + "","width: " + ((int)(number * 30) / 2) + "px; background-color: orange;", true));
        }
        results.add(new GraphResult("", "", "", false));
        data.clearAll();
        model.addAttribute("results", results);
        model.addAttribute("exist", "exist");
        model.addAttribute("balance", "balance");
        model.addAttribute("days", days);
        return "home";
    }

    @RequestMapping(value = "/long")
    public String longBalance(Model model){
        User user = ContextHelper.getUserFromContext();
        Object dailyObject = dailyBalanceRepository.findAllByUserToDate(user, Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().minusDays(30)));
        if(dailyObject == null || ((List) dailyObject).size() == 0){
            model.addAttribute("longBalance", "longBalance");
            model.addAttribute("exist", null);
            return "home";
        }
        List<DailyBalance> dailyBalances = (List<DailyBalance>)dailyObject;
        int days = dailyBalances.size();
        Collections.reverse(dailyBalances);
        DailyBalanceData data = dailyBalanceHelper.getBalance(dailyBalances, false);
        if((data.getNeededMacro().getProtein() == 0) || (data.getNeededMacro().getCarbohydrates() == 0) || (data.getNeededMacro().getFat() == 0) || (data.getNeededMacro().getCalories() == 0)){
            model.addAttribute("longBalance", "longBalance");
            model.addAttribute("exist", null);
            return "home";
        }
        int avgProtein = (int)((data.getReceivedMacro().getProtein() / data.getNeededMacro().getProtein()) * 100);
        int avgCarbohydrates = (int)((data.getReceivedMacro().getCarbohydrates() / data.getNeededMacro().getCarbohydrates()) * 100);
        int avgFat = (int)((data.getReceivedMacro().getFat() / data.getNeededMacro().getFat()) * 100 );
        int avgCalories = (data.getReceivedMacro().getCalories() * 100) / data.getNeededMacro().getCalories();
        data.clearAll();
        model.addAttribute("avgProtein", avgProtein);
        model.addAttribute("avgCarbohydrates", avgCarbohydrates);
        model.addAttribute("avgFat", avgFat);
        model.addAttribute("avgCalories", avgCalories);
        model.addAttribute("balances", dailyBalances);
        model.addAttribute("exist", "exist");
        model.addAttribute("longBalance", "longBalance");
        model.addAttribute("days", days);
        return "home";
    }

    @RequestMapping(value = "/option", method = RequestMethod.GET)
    public String option(Model model){
        model.addAttribute("balanceOption","balanceOption");
        return "home";
    }

    private String formatMacroData(double received, double total){
        return numberHelper.roundDouble(received) + "/" + numberHelper.roundDouble(total);
    }
}
