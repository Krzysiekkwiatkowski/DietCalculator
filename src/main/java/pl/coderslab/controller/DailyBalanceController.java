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
import java.util.ArrayList;
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
        double glycemicChargeReceived = 0;
        DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
        List<Meal> meals = dailyBalance.getMeals();
        List<Double> glycemicCharges = new ArrayList<>();
        for (Meal meal : meals) {
            proteinReceived += meal.getTotalProtein();
            carbohydratesReceived += meal.getTotalCarbohydrates();
            fatReceived += meal.getTotalFat();
            caloriesReceived += meal.getTotalCalories();
            glycemicCharges.add(meal.getGlycemicCharge());
        }
        StringBuilder sbProtein = new StringBuilder();
        StringBuilder sbCarbohydrates = new StringBuilder();
        StringBuilder sbFat = new StringBuilder();
        StringBuilder sbCalories = new StringBuilder();
        int protein = (int) (proteinReceived * 100 / totalProtein);
        int carbohydrates = (int) (carbohydratesReceived * 100 / totalCarbohydrates);
        int fat = (int) (fatReceived * 100 / totalFat);
        int calories = caloriesReceived * 100 / totalCalories;
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        model.addAttribute("protein", protein);
        model.addAttribute("carbohydrates", carbohydrates);
        model.addAttribute("fat", fat);
        model.addAttribute("calories", calories);
        model.addAttribute("proteinPart", sbProtein.append(" " + Double.parseDouble(decimalFormat.format(proteinReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalProtein).replace(",", "."))));
        model.addAttribute("carbohydratesPart", sbCarbohydrates.append(" " + Double.parseDouble(decimalFormat.format(carbohydratesReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalCarbohydrates).replace(",", "."))));
        model.addAttribute("fatPart", sbFat.append(" " + Double.parseDouble(decimalFormat.format(fatReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalFat).replace(",", "."))));
        model.addAttribute("caloriesPart", sbCalories.append(" " + Double.parseDouble(decimalFormat.format(caloriesReceived).replace(",", ".")) + "/" + totalCalories));
        model.addAttribute("glycemicCharges", glycemicCharges);
        model.addAttribute("exist", "exist");
        model.addAttribute("actualBalance", "actualBalance");
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
        int protein = (int) (proteinReceived * 100 / totalProtein);
        int carbohydrates = (int) (carbohydratesReceived * 100 / totalCarbohydrates);
        int fat = (int) (fatReceived * 100 / totalFat);
        int calories = caloriesReceived * 100 / totalCalories;
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        StringBuilder sbProtein = new StringBuilder();
        StringBuilder sbCarbohydrates = new StringBuilder();
        StringBuilder sbFat = new StringBuilder();
        StringBuilder sbCalories = new StringBuilder();
        model.addAttribute("protein", protein);
        model.addAttribute("carbohydrates", carbohydrates);
        model.addAttribute("fat", fat);
        model.addAttribute("calories", calories);
        model.addAttribute("proteinPart", sbProtein.append(" " + Double.parseDouble(decimalFormat.format(proteinReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalProtein).replace(",", "."))));
        model.addAttribute("carbohydratesPart", sbCarbohydrates.append(" " + Double.parseDouble(decimalFormat.format(carbohydratesReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalCarbohydrates).replace(",", "."))));
        model.addAttribute("fatPart", sbFat.append(" " + Double.parseDouble(decimalFormat.format(fatReceived).replace(",", ".")) + "/" + Double.parseDouble(decimalFormat.format(totalFat).replace(",", "."))));
        model.addAttribute("caloriesPart", sbCalories.append(" " + Double.parseDouble(decimalFormat.format(caloriesReceived).replace(",", ".")) + "/" + totalCalories));
        model.addAttribute("exist", "exist");
        model.addAttribute("weeklyBalance", "weeklyBalance");
        model.addAttribute("days", days);
        return "home";
    }

    @RequestMapping(value = "/long")
    public String longBalance(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        Object dailyObject = dailyBalanceRepository.findAllByUserAndDate(loadedUser, Date.valueOf(LocalDate.now()), 30);
        if(dailyObject == null || ((List) dailyObject).size() == 0){
            model.addAttribute("longBalance", "longBalance");
            model.addAttribute("exist", null);
            return "home";
        }
        List<DailyBalance> dailyBalances = (List<DailyBalance>)dailyObject;
        List<DailyBalance> orderBalanced = new ArrayList<>();
        int days = dailyBalances.size();
        double sumProteinN = 0;
        double sumCarbohydratesN = 0;
        double sumFatN = 0;
        int sumCaloriesN = 0;
        double sumProteinR = 0;
        double sumCarbohydratesR = 0;
        double sumFatR = 0;
        int sumCaloriesR = 0;
        for (int i = days - 1; i >= 0; i--) {
            sumProteinN += dailyBalances.get(i).getTotalProtein();
            sumCarbohydratesN += dailyBalances.get(i).getTotalCarbohydrates();
            sumFatN += dailyBalances.get(i).getTotalFat();
            sumCaloriesN += dailyBalances.get(i).getNeeded();
            orderBalanced.add(dailyBalances.get(i));
            for (Meal meal : dailyBalances.get(i).getMeals()) {
                sumProteinR += meal.getTotalProtein();
                sumCarbohydratesR += meal.getTotalCarbohydrates();
                sumFatR += meal.getTotalFat();
                sumCaloriesR += meal.getTotalCalories();
            }
        }
        int avgProtein = (int)((sumProteinR / sumProteinN) * 100 );
        int avgCarbohydrates = (int)((sumCarbohydratesR / sumCarbohydratesN) * 100);
        int avgFat = (int)((sumFatR / sumFatN) * 100 );
        int avgCalories = sumCaloriesR * 100 / sumCaloriesN;
        model.addAttribute("avgProtein", avgProtein);
        model.addAttribute("avgCarbohydrates", avgCarbohydrates);
        model.addAttribute("avgFat", avgFat);
        model.addAttribute("avgCalories", avgCalories);
        model.addAttribute("balances", orderBalanced);
        model.addAttribute("exist", "exist");
        model.addAttribute("longBalance", "longBalance");
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
