package pl.coderslab.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Meal;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Date;
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

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerGet(Model model){
        model.addAttribute("user", new User());
        return "registerUser";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPost(@Valid User user, BindingResult result, HttpSession session, Model model){
        if(result.hasErrors()){
            return "registerUser";
        }
        for (User check : userRepository.findAll()) {
            if(check.getEmail().equals(user.getEmail())){
                model.addAttribute("repeat", "repeat");
                return "registerUser";
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
        user.setTotalProtein(user.getWeight() * 1.8);
        if(goal.equals("Utrata wagi")){
            user.setTotalFat(user.getWeight() * 3.0);
            user.setTotalCarbohydrates((total - 4.0 * user.getTotalProtein() - 9.0 * user.getTotalFat())/ 4.0);
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(user.getWeight() * 2.5);
            user.setTotalFat((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0);
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(user.getWeight() * 3.5);
            user.setTotalFat((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0);
        }
        userRepository.save(user);
        session.setAttribute("user", user);
        return "home";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editGet(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            return "loginForm";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        model.addAttribute("user", loadedUser);
        return "editUser";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editPost(@Valid User user, BindingResult result){
        if(result.hasErrors()){
            return "editUser";
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
        user.setTotalProtein(user.getWeight() * 1.8);
        if(goal.equals("Utrata wagi")){
            user.setTotalCarbohydrates(user.getWeight() * 1.5);
            user.setTotalFat((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0);
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(user.getWeight() * 2.5);
            user.setTotalFat((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0);
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(user.getWeight() * 3.5);
            user.setTotalFat((total - 4.0 * user.getTotalProtein() - 4.0 * user.getTotalCarbohydrates())/ 9.0);
        }
        userRepository.save(user);
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginGet(){
        return "loginForm";
    }

    @RequestMapping("/actual")
    public String actualBalance(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            return "loginForm";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        double totalProtein = loadedUser.getTotalProtein();
        double totalCarbohydrates = loadedUser.getTotalCarbohydrates();
        double totalFat = loadedUser.getTotalFat();
        int totalCalories = loadedUser.getTotalCalories();
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) == null){
            model.addAttribute("not", "not");
            return "actualBalance";
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
        model.addAttribute("protein", protein.toString());
        model.addAttribute("carbohydrates", carbohydrates.toString());
        model.addAttribute("fat", fat.toString());
        model.addAttribute("calories", calories.toString());
        return "actualBalance";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session){
        User user = userRepository.findTopByEmail(email);
        if(BCrypt.checkpw(password, user.getPassword())){
            session.setAttribute("user", user);
            return "home";
        }
        model.addAttribute("wrong", "wrong");
        return "loginForm";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session){
        session.removeAttribute("user");
        return "home";
    }

    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public String changePasswordGet(HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            return "loginForm";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        model.addAttribute("user", loadedUser);
        return "changePassword";
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public String changePasswordPost(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Model model, HttpSession session){
        System.out.println("stare -> " + oldPassword);
        System.out.println("nowe -> " + newPassword);
        Object object = session.getAttribute("user");
        if(object == null){
            return "loginForm";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(BCrypt.checkpw(oldPassword, loadedUser.getPassword())){
            loadedUser.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            userRepository.save(loadedUser);
            return "home";
        }
        model.addAttribute("wrongPassword", "wrongPassword");
        return "changePassword";
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
