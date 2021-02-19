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
import pl.coderslab.entity.Setting;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;
import pl.coderslab.repository.MealRepository;
import pl.coderslab.repository.TrainingRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
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
        if(verifySetting(user.getSetting())){
            model.addAttribute("registerUser", "registerUser");
            model.addAttribute("incorrectSum", "incorrectSum");
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
        userRepository.save(calculateMacroelements(user));
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
        if(loadedUser.getSetting() == null){
            loadedUser.setSetting(new Setting(0L, 20, 50, 30));
        }
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
        if(verifySetting(user.getSetting())){
            model.addAttribute("editUser", "editUser");
            model.addAttribute("incorrectSum", "incorrectSum");
            return "home";
        }
        userRepository.save(calculateMacroelements(user));
        return "redirect:/diet/user/option";
    }

    @RequestMapping(value = "/correct", method = RequestMethod.GET)
    public String correctGet(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        User user = (User) object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        model.addAttribute("user", loadedUser);
        model.addAttribute("logged", "logged");
        model.addAttribute("correctCalories", "correctCalories");
        return "home";
    }

    @RequestMapping(value = "/correct", method = RequestMethod.POST)
    public String correctPost(@ModelAttribute User user, BindingResult  result, Model model, HttpSession session, HttpServletRequest request){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(result.hasErrors()){
            model.addAttribute("userOption","userOption");
            model.addAttribute("correctCalories", "correctCalories");
            return "home";
        }
        String correctString = request.getParameter("correct");
        int correct = 0;
        if(correctString != null){
            try {
                correct = Integer.parseInt(correctString);
            } catch (NumberFormatException e){
                correct = 0;
            }
        }
        userRepository.save(calculateMacroelements(user));
        session.setAttribute("user", user);
        model.addAttribute("userOption","userOption");
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
        return "redirect:/diet/user/option";
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
        model.addAttribute("userOption","userOption");
        return "home";
    }

    @RequestMapping(value = "/delete")
    public String delete(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("deleteUser", "deleteUser");
        return "home";
    }

    @Transactional
    @RequestMapping(value = "/delete/yes", method = RequestMethod.GET)
    public String deleteConfirm(HttpSession session, Model model){
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

    private User calculateMacroelements(User user){
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
        int total = (int) metabolism + activityFactor + somatotypeFactor + goalFactor + user.getCorrect();
        if(!user.isSelfDistribution()) {
            user.setTotalCalories(total);
            DecimalFormat decimalFormat = new DecimalFormat("#.#");
            user.setTotalProtein(Double.parseDouble(decimalFormat.format(user.getWeight() * 1.8).replace(",", ".")));
            if (goal.equals("Utrata wagi")) {
                user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 4.0).replace(",", ".")));
                user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 9.0).replace(",", ".")));
            }
            if (goal.equals("Utrzymanie wagi")) {
                user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 4.0).replace(",", ".")));
                user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 9.0).replace(",", ".")));
            }
            if (goal.equals("Przybranie wagi")) {
                user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 4.0).replace(",", ".")));
                user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 9.0).replace(",", ".")));
            }
        } else {
            int protein = (total * user.getSetting().getProteinPart()) / 400;
            int carbohydrates = (total * user.getSetting().getCarbohydratePart()) / 400;
            int fat = (total * user.getSetting().getFatPart()) / 900;
            total = protein * 4 + carbohydrates * 4 + fat * 9;
            user.setTotalCalories(total);
            user.setTotalProtein(protein);
            user.setTotalCarbohydrates(carbohydrates);
            user.setTotalFat(fat);
        }
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setTotalProtein(user.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(user.getTotalCarbohydrates());
            dailyBalance.setTotalFat(user.getTotalFat());
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        return user;
    }

    private boolean verifySetting(Setting setting){
        return ((setting != null) && ((setting.getProteinPart() + setting.getCarbohydratePart() + setting.getFatPart()) != 100));
    }
}
