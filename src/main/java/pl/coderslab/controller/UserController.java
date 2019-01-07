package pl.coderslab.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;
import pl.coderslab.repository.MealRepository;
import pl.coderslab.repository.TrainingRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/user")
@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private MealRepository mealRepository;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerGet(Model model){
        model.addAttribute("logged", null);
        model.addAttribute("registerUser", "registerUser");
        model.addAttribute("user", new User());
        return "home";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPost(@Valid User user, BindingResult result, HttpSession session, Model model){
        model.addAttribute("logged", null);
        if(result.hasErrors()){
            model.addAttribute("registerUser", "registerUser");
            return "home";
        }
        for (User check : userRepository.findAll()) {
            if(check.getEmail().equals(user.getEmail())){
                model.addAttribute("registerUser", "registerUser");
                model.addAttribute("repeat", "repeat");
                return "home";
            }
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        String activity = user.getActivity();
        String somatotype = user.getSomatotype();
        String goal = user.getGoal();
        double metabolism = 0.0;
        if(user.getGender().equals("Mężczyzna")){
            metabolism = ((9.99 * user.getWeight()) + (6.25 * user.getHeight()) - (4.92 * user.getAge()) + 5) * 1.1;
        } else if(user.getGender().equals("Kobieta")){
            metabolism = ((9.99 * user.getWeight()) + (6.25 * user.getHeight()) - (4.92 * user.getAge()) - 161) * 1.1;
        }
        int activityFactor = 0;
        int somatotypeFactor = 0;
        int goalFactor = 0;
        if(somatotype.equals("Ektomorfik")){
            somatotypeFactor = 700;
        } else if(somatotype.equals("Endomorfik")){
            somatotypeFactor = 200;
        } else if(somatotype.equals("Mezomorfik")){
            somatotypeFactor = 400;
        }
        if(activity.equals("Średnia")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 50;
            } else {
                activityFactor = 100;
            }
        } else if(activity.equals("Duża")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 100;
            } else {
                activityFactor = 200;
            }
        } else if(activity.equals("Umiarkowana fizyczna")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 150;
            } else {
                activityFactor = 300;
            }
        } else if(activity.equals("Ciężka fizyczna")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 200;
            } else {
                activityFactor = 400;
            }
        }
        if(goal.equals("Utrata wagi")){
            goalFactor = -500;
        } else if(goal.equals("Przybranie wagi")){
            goalFactor = 500;
        }
        int total = (int) metabolism + activityFactor + somatotypeFactor + goalFactor;
        user.setTotalCalories(total);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        user.setTotalProtein(Double.parseDouble(decimalFormat.format(user.getWeight() * 1.8).replace(",", ".")));
        if(goal.equals("Utrata wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 1.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 2.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 3.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        userRepository.save(user);
        session.setAttribute("user", user);
        return "redirect:/diet/home";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editGet(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        model.addAttribute("editUser", "editUser");
        model.addAttribute("user", loadedUser);
        return "home";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editPost(@Valid User user, BindingResult result, Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(result.hasErrors()){
            model.addAttribute("editUser", "editUser");
            return "home";
        }
        String activity = user.getActivity();
        String somatotype = user.getSomatotype();
        String goal = user.getGoal();
        double metabolism = 0.0;
        if(user.getGender().equals("Mężczyzna")){
            metabolism = ((9.99 * user.getWeight()) + (6.25 * user.getHeight()) - (4.92 * user.getAge()) + 5) * 1.1;
        } else if(user.getGender().equals("Kobieta")){
            metabolism = ((9.99 * user.getWeight()) + (6.25 * user.getHeight()) - (4.92 * user.getAge()) - 161) * 1.1;
        }
        int activityFactor = 0;
        int somatotypeFactor = 0;
        int goalFactor = 0;
        if(somatotype.equals("Ektomorfik")){
            somatotypeFactor = 800;
        } else if(somatotype.equals("Endomorfik")){
            somatotypeFactor = 400;
        } else if(somatotype.equals("Mezomorfik")){
            somatotypeFactor = 450;
        }
        if(activity.equals("Średnia")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 50;
            } else {
                activityFactor = 100;
            }
        } else if(activity.equals("Duża")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 100;
            } else {
                activityFactor = 200;
            }
        } else if(activity.equals("Umiarkowana fizyczna")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 150;
            } else {
                activityFactor = 300;
            }
        } else if(activity.equals("Ciężka fizyczna")){
            if(somatotype.equals("Mezomorfik")){
                activityFactor = 200;
            } else {
                activityFactor = 400;
            }
        }
        if(goal.equals("Utrata wagi")){
            goalFactor = -500;
        } else if(goal.equals("Przybranie wagi")){
            goalFactor = 500;
        }
        int total = (int) metabolism + activityFactor + somatotypeFactor + goalFactor;
        user.setTotalCalories(total);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        user.setTotalProtein(Double.parseDouble(decimalFormat.format(user.getWeight() * 1.8).replace(",", ".")));
        if(goal.equals("Utrata wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 1.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 2.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(user.getWeight() * 3.5).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0).replace(",", ".")));
        }
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        userRepository.save(user);
        return "redirect:/diet/home";
    }

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

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginGet(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("loginForm", "loginForm");
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session){
        Object object = userRepository.findTopByEmail(email);
        if(object == null) {
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            model.addAttribute("wrong", "wrong");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        if(BCrypt.checkpw(password, user.getPassword())){
            session.setAttribute("user", user);
            return "redirect:/diet/home";
        }
        model.addAttribute("loginForm", "loginForm");
        model.addAttribute("wrong", "wrong");
        return "home";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(Model model, HttpSession session){
        session.removeAttribute("user");
        model.addAttribute("logged", null);
        return "redirect:/diet/home";
    }

    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public String changePasswordGet(HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        model.addAttribute("user", loadedUser);
        model.addAttribute("changePassword", "changePassword");
        return "home";
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public String changePasswordPost(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(BCrypt.checkpw(oldPassword, loadedUser.getPassword())){
            loadedUser.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            userRepository.save(loadedUser);
            return "redirect:/diet/home";
        }
        model.addAttribute("changePassword", "changePassword");
        model.addAttribute("wrongPassword", "wrongPassword");
        return "home";
    }

    @Transactional
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public String delete(HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(loadedUser.getTraining() != null){
            trainingRepository.delete(loadedUser.getTraining());
        }
        if(dailyBalanceRepository.findAllByUser(loadedUser) != null) {
            for (DailyBalance dailyBalance : dailyBalanceRepository.findAllByUser(loadedUser)) {
                List<Long> numbers = new ArrayList<>();
                for (Meal meal : dailyBalance.getMeals()) {
                    numbers.add(meal.getId());
                }
                for (Long number : numbers) {
                    mealRepository.deleteById(number);
                }
            }
            dailyBalanceRepository.deleteAllByUser(loadedUser);
        }
        userRepository.delete(loadedUser);
        session.removeAttribute("user");
        return "redirect:/diet/home";
    }

    @ModelAttribute("genderList")
    public List<String> allGenders(){
        List<String> genders = new ArrayList<>();
        genders.add("Mężczyzna");
        genders.add("Kobieta");
        return genders;
    }

    @ModelAttribute("activityList")
    public List<String> allActivities(){
        List<String> activities = new ArrayList<>();
        activities.add("Mała");
        activities.add("Średnia");
        activities.add("Duża");
        activities.add("Umiarkowana fizyczna");
        activities.add("Ciężka fizyczna");
        return activities;
    }

    @ModelAttribute("somatotypeList")
    public List<String> allSomatotypes(){
        List<String> somatotypes = new ArrayList<>();
        somatotypes.add("Ektomorfik");
        somatotypes.add("Endomorfik");
        somatotypes.add("Mezomorfik");
        return somatotypes;
    }

    @ModelAttribute("goalList")
    public List<String> allGoals(){
        List<String> goals = new ArrayList<>();
        goals.add("Utrata wagi");
        goals.add("Utrzymanie wagi");
        goals.add("Przybranie wagi");
        return goals;
    }
}
