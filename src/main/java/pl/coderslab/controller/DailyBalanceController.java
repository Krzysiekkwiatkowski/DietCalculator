package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/diet/daily")
@Controller
public class DailyBalanceController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @RequestMapping("/actual")
    public String actualBalance(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        double totalProtein = loadedUser.getTotalProtein();
        double totalCarbohydrates = loadedUser.getTotalCarbohydrates();
        double totalFat = loadedUser.getTotalFat();
        int totalCalories = loadedUser.getTotalCalories();
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) == null){
            model.addAttribute("actualBalance", "actualBalance");
            model.addAttribute("exist", null);
            return "home";
        }
        double proteinReceived = 0.0;
        double carbohydratesReceived = 0.0;
        double fatReceived = 0.0;
        int caloriesReceived = 0;
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
        List<Meal> meals = dailyBalance.getMeals();
        for (Meal meal : meals) {
            proteinReceived += meal.getTotalProtein();
            carbohydratesReceived += meal.getTotalCarbohydrates();
            fatReceived += meal.getTotalFat();
            caloriesReceived += meal.getTotalCalories();
        }
        StringBuilder protein = new StringBuilder();
        StringBuilder carbohydrates = new StringBuilder();
        StringBuilder fat = new StringBuilder();
        StringBuilder calories = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        for (int i = 0; i < 100; i++) {
            if(i < (proteinReceived / totalProtein) * 100){
                protein.append("|");
            } else if(i == 99){
                protein.append("|");
            } else {
                protein.append(".");
            }
            if(i < (carbohydratesReceived / totalCarbohydrates) * 100){
                carbohydrates.append("|");
            } else if(i == 99){
                carbohydrates.append("|");
            } else {
                carbohydrates.append(".");
            }
            if(i < (fatReceived / totalFat) * 100){
                fat.append("|");
            } else if(i == 99){
                fat.append("|");
            } else {
                fat.append(".");
            }
            if(i < ((caloriesReceived  * 100)/ totalCalories)){
                calories.append("|");
            } else if(i == 99){
                calories.append("|");
            } else {
                calories.append(".");
            }
        }
        StringBuilder sbProtein = new StringBuilder();
        StringBuilder sbCarbohydrates = new StringBuilder();
        StringBuilder sbFat = new StringBuilder();
        StringBuilder sbCalories = new StringBuilder();
        model.addAttribute("proteinPart", sbProtein.append(" " + Double.parseDouble(decimalFormat.format(proteinReceived).replace(",", ".")) + "/" + totalProtein).toString());
        model.addAttribute("carbohydratesPart", sbCarbohydrates.append(" " + Double.parseDouble(decimalFormat.format(carbohydratesReceived).replace(",", ".")) + "/" + totalCarbohydrates).toString());
        model.addAttribute("fatPart", sbFat.append(" " + Double.parseDouble(decimalFormat.format(fatReceived).replace(",", ".")) + "/" + totalFat).toString());
        model.addAttribute("caloriesPart", sbCalories.append(" " + Double.parseDouble(decimalFormat.format(caloriesReceived).replace(",", ".")) + "/" + totalCalories));
        model.addAttribute("exist", "exist");
        model.addAttribute("actualBalance", "actualBalance");
        model.addAttribute("protein", protein.toString());
        model.addAttribute("carbohydrates", carbohydrates.toString());
        model.addAttribute("fat", fat.toString());
        model.addAttribute("calories", calories.toString());
        return "home";
    }

    @RequestMapping(value = "/weekly")
    public String last(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object dailyObject = dailyBalanceRepository.findAllByUserAndDate(loadedUser, Date.valueOf(LocalDate.now()));
        if(dailyObject == null || ((List) dailyObject).size() == 0){
            model.addAttribute("weeklyBalance", "weeklyBalance");
            model.addAttribute("exist", null);
            return "home";
        }
        List<DailyBalance> dailyBalances = (List<DailyBalance>)dailyObject;
        int days = dailyBalances.size();
        double totalProtein = 0.0;
        double totalCarbohydrates = 0.0;
        double totalFat = 0.0;
        int totalCalories = 0;
        double proteinReceived = 0.0;
        double carbohydratesReceived = 0.0;
        double fatReceived = 0.0;
        int caloriesReceived = 0;
        for (DailyBalance dailyBalance : dailyBalances) {
            totalProtein += dailyBalance.getTotalProtein();
            totalCarbohydrates += dailyBalance.getTotalCarbohydrates();
            totalFat += dailyBalance.getTotalFat();
            totalCalories += dailyBalance.getNeeded();
            for (Meal meal : dailyBalance.getMeals()) {
                proteinReceived += meal.getTotalProtein();
                carbohydratesReceived += meal.getTotalCarbohydrates();
                fatReceived += meal.getTotalFat();
                caloriesReceived += meal.getTotalCalories();
            }
        }
        StringBuilder protein = new StringBuilder();
        StringBuilder carbohydrates = new StringBuilder();
        StringBuilder fat = new StringBuilder();
        StringBuilder calories = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        for (int i = 0; i < 100; i++) {
            if(i < (proteinReceived / totalProtein) * 100){
                protein.append("|");
            } else if(i == 99){
                protein.append("|");
            } else {
                protein.append(".");
            }
            if(i < (carbohydratesReceived / totalCarbohydrates) * 100){
                carbohydrates.append("|");
            } else if(i == 99){
                carbohydrates.append("|");
            } else {
                carbohydrates.append(".");
            }
            if(i < (fatReceived / totalFat) * 100){
                fat.append("|");
            } else if(i == 99){
                fat.append("|");
            } else {
                fat.append(".");
            }
            if(i < ((caloriesReceived  * 100)/ totalCalories)){
                calories.append("|");
            } else if(i == 99){
                calories.append("|");
            } else {
                calories.append(".");
            }
        }
        StringBuilder sbProtein = new StringBuilder();
        StringBuilder sbCarbohydrates = new StringBuilder();
        StringBuilder sbFat = new StringBuilder();
        StringBuilder sbCalories = new StringBuilder();
        model.addAttribute("proteinPart", sbProtein.append(" " + Double.parseDouble(decimalFormat.format(proteinReceived).replace(",", ".")) + "/" + totalProtein).toString());
        model.addAttribute("carbohydratesPart", sbCarbohydrates.append(" " + Double.parseDouble(decimalFormat.format(carbohydratesReceived).replace(",", ".")) + "/" + totalCarbohydrates).toString());
        model.addAttribute("fatPart", sbFat.append(" " + Double.parseDouble(decimalFormat.format(fatReceived).replace(",", ".")) + "/" + totalFat).toString());
        model.addAttribute("caloriesPart", sbCalories.append(" " + Double.parseDouble(decimalFormat.format(caloriesReceived).replace(",", ".")) + "/" + totalCalories));
        model.addAttribute("exist", "exist");
        model.addAttribute("weeklyBalance", "weeklyBalance");
        model.addAttribute("protein", protein.toString());
        model.addAttribute("carbohydrates", carbohydrates.toString());
        model.addAttribute("fat", fat.toString());
        model.addAttribute("calories", calories.toString());
        model.addAttribute("days", days);
        return "home";
    }

    @RequestMapping(value = "/option", method = RequestMethod.GET)
    public String option(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("balanceOption","balanceOption");
        return "home";
    }
}
