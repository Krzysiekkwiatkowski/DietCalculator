package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.User;
import pl.coderslab.pojo.GraphResult;
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
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");


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
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            return "home";
        }
        double proteinReceived = 0.0;
        double carbohydratesReceived = 0.0;
        double fatReceived = 0.0;
        int caloriesReceived = 0;
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
        int protein = (int) (proteinReceived * 100 / totalProtein);
        int carbohydrates = (int) (carbohydratesReceived * 100 / totalCarbohydrates);
        int fat = (int) (fatReceived * 100 / totalFat);
        int calories = caloriesReceived * 100 / totalCalories;
        List<GraphResult> results = new ArrayList<>();
        results.add(new GraphResult("Białko: " + formatMacroData(proteinReceived, totalProtein), protein + "%", "width: " + (protein * 3) + "px; background-color: green;", true));
        results.add(new GraphResult("Węglowodany: " + formatMacroData(carbohydratesReceived, totalCarbohydrates), carbohydrates + "%", "width: " + (carbohydrates * 3) + "px; background-color: red;", true));
        results.add(new GraphResult("Tłuszcz: " + formatMacroData(fatReceived, totalFat), fat + "%", "width: " + (fat * 3) + "px; background-color: yellow;", true));
        results.add(new GraphResult("Kalorie: " + caloriesReceived + "/" + totalCalories, calories + "%", "width: " + (calories * 3) + "px; background-color: blue;", true));
        results.add(new GraphResult("%", "", "", false));
        for(int i = 0; i < glycemicCharges.size(); i++){
            results.add(new GraphResult("Posiłek " + (i + 1), roundDouble(glycemicCharges.get(i)) + "","width: " + ((int)(glycemicCharges.get(i) * 30) / 2) + "px; background-color: orange;", true));
        }
        results.add(new GraphResult("", "", "", false));
        model.addAttribute("results", results);
        model.addAttribute("exist", "exist");
        model.addAttribute("balance", "balance");
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
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            model.addAttribute("days", 0);
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
        List<Double> glycemicChargesDay = new ArrayList<>();
        double glycemicChargesSum = 0.0;
        int glycemicChargesCount = 0;
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
                glycemicChargesSum += meal.getGlycemicCharge();
                glycemicChargesCount++;
            }
            glycemicChargesDay.add(glycemicChargesSum / glycemicChargesCount);
            glycemicChargesSum = 0.0;
            glycemicChargesCount = 0;
        }
        if((totalProtein == 0) || (totalCarbohydrates == 0) || (totalFat == 0) || (totalCalories==0)){
            model.addAttribute("balance", "balance");
            model.addAttribute("exist", null);
            model.addAttribute("days", 0);
            return "home";
        }
        int protein = (int) (proteinReceived * 100 / totalProtein);
        int carbohydrates = (int) (carbohydratesReceived * 100 / totalCarbohydrates);
        int fat = (int) (fatReceived * 100 / totalFat);
        int calories = caloriesReceived * 100 / totalCalories;
        List<GraphResult> results = new ArrayList<>();
        results.add(new GraphResult("Białko: " + formatMacroData(proteinReceived, totalProtein), protein + "%", "width: " + (protein * 3) + "px; background-color: green;", true));
        results.add(new GraphResult("Węglowodany: " + formatMacroData(carbohydratesReceived, totalCarbohydrates), carbohydrates + "%", "width: " + (carbohydrates * 3) + "px; background-color: red;", true));
        results.add(new GraphResult("Tłuszcz: " + formatMacroData(fatReceived, totalFat), fat + "%", "width: " + (fat * 3) + "px; background-color: yellow;", true));
        results.add(new GraphResult("Kalorie: " + caloriesReceived + "/" + totalCalories, calories + "%", "width: " + (calories * 3) + "px; background-color: blue;", true));
        results.add(new GraphResult("%", "", "", false));
        for(int i = 0; i < glycemicChargesDay.size(); i++){
            results.add(new GraphResult("Dzień " + (i + 1), roundDouble(glycemicChargesDay.get(i)) + "","width: " + ((int)(glycemicChargesDay.get(i) * 30) / 2) + "px; background-color: orange;", true));
        }
        results.add(new GraphResult("", "", "", false));
        model.addAttribute("results", results);
        model.addAttribute("exist", "exist");
        model.addAttribute("balance", "balance");
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
        if((sumProteinN == 0) || (sumCarbohydratesN == 0) || (sumFatN == 0) || (sumCaloriesN == 0)){
            model.addAttribute("longBalance", "longBalance");
            model.addAttribute("exist", null);
            return "home";
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

    private String formatMacroData(double received, double total){
        return roundDouble(received) + "/" + roundDouble(total);
    }

    private double roundDouble(double number){
        return Double.parseDouble(DECIMAL_FORMAT.format(number).replace(",", "."));
    }
}
