package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.*;
import pl.coderslab.helper.ContextHelper;
import pl.coderslab.helper.DailyBalanceHelper;
import pl.coderslab.helper.UserHelper;
import pl.coderslab.repository.*;
import pl.coderslab.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.DecimalFormat;
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

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DailyBalanceHelper dailyBalanceHelper;

    @Autowired
    private UserHelper userHelper;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerGet(Model model){
        User user = new User();
        Setting setting = new Setting();
        setting.setProteinPart(20);
        setting.setCarbohydratePart(50);
        setting.setFatPart(30);
        user.setSetting(setting);
        user.setCorrect(new Integer(0));
        model.addAttribute("logged", null);
        model.addAttribute("registerUser", "registerUser");
        model.addAttribute("user", user);
        return "home";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPost(@Valid User user, BindingResult result, HttpSession session, Model model, HttpServletRequest request){
        model.addAttribute("logged", null);
        if(result.hasErrors()){
            model.addAttribute("registerUser", "registerUser");
            return "home";
        }
        if(user.isSelfDistribution() && verifySetting(user.getSetting())){
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
        String password = user.getPassword();
        userHelper.calculateMacroElements(user);
        userService.save(user);
        if(!user.isSelfDistribution()){
            user.setSetting(null);
        }
        try {
            request.login(user.getEmail(), password);
            session.setAttribute("logged", "logged");
            session.setAttribute("user", user);
        } catch (ServletException e){
            e.printStackTrace();
        }
        return "redirect:/diet/home";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public String editGet(Model model){
        User user = ContextHelper.getUserFromContext();
        if(user.getSetting() == null){
            user.setSetting(new Setting(0L, 20, 50, 30));
        }
        model.addAttribute("editUser", "editUser");
        model.addAttribute("user", user);
        return "home";
    }

    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public String editPost(@Valid User user, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("editUser", "editUser");
            return "home";
        }
        if(verifySetting(user.getSetting())){
            model.addAttribute("editUser", "editUser");
            model.addAttribute("incorrectSum", "incorrectSum");
            return "home";
        }
        user.setRoles(roleRepository.findAllRolesByUserId(user.getId()));
        userHelper.calculateMacroElements(user);
        userRepository.save(user);
        return "redirect:/diet/user/option";
    }

    @RequestMapping(value = "/correct", method = RequestMethod.GET)
    public String correctGet(Model model){
        User user = ContextHelper.getUserFromContext();
        model.addAttribute("user", user);
        model.addAttribute("correctCalories", "correctCalories");
        return "home";
    }

    @RequestMapping(value = "/correct", method = RequestMethod.POST)
    public String correctPost(@Valid @ModelAttribute User user, BindingResult  result, Model model, HttpSession session){
        if(result.hasErrors()){
            model.addAttribute("correctCalories", "correctCalories");
            return "home";
        }
        userHelper.calculateMacroElements(user);
        userRepository.save(user);
        session.setAttribute("user", user);
        model.addAttribute("userOption","userOption");
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginGet(Model model, HttpSession session){
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(object != null && !object.equals("anonymousUser")){
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) object;
            session.setAttribute("user", userRepository.findTopByEmail(user.getUsername()));
            session.setAttribute("logged", "logged");
            return "redirect:/diet/home";
        }
        model.addAttribute("loginForm", "loginForm");
        return "home";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session){
        session.removeAttribute("user");
        session.setAttribute("logged", null);
        return "redirect:/diet/home";
    }

    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public String changePasswordGet(Model model){
        User user = ContextHelper.getUserFromContext();
        model.addAttribute("user", user);
        model.addAttribute("changePassword", "changePassword");
        return "home";
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST)
    public String changePasswordPost(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Model model){
        User user = ContextHelper.getUserFromContext();
        if(userService.verifyPassword(oldPassword, user.getPassword())){
            user.setPassword(userService.hashPassword(newPassword));
            userRepository.save(user);
            return "redirect:/diet/home";
        }
        model.addAttribute("changePassword", "changePassword");
        model.addAttribute("wrongPassword", "wrongPassword");
        return "home";
    }

    @RequestMapping(value = "/option", method = RequestMethod.GET)
    public String option(Model model){
        model.addAttribute("userOption","userOption");
        return "home";
    }

    @RequestMapping(value = "/delete")
    public String delete(Model model){
        model.addAttribute("deleteUser", "deleteUser");
        return "home";
    }

    @Transactional
    @RequestMapping(value = "/delete/yes", method = RequestMethod.GET)
    public String deleteConfirm(HttpSession session){
        User user = ContextHelper.getUserFromContext();
        if(user.getTraining() != null){
            trainingRepository.delete(user.getTraining());
        }
        if(dailyBalanceRepository.findAllByUser(user) != null) {
            for (DailyBalance dailyBalance : dailyBalanceRepository.findAllByUser(user)) {
                List<Long> numbers = new ArrayList<>();
                for (Meal meal : dailyBalance.getMeals()) {
                    numbers.add(meal.getId());
                }
                for (Long number : numbers) {
                    mealRepository.deleteById(number);
                }
            }
            dailyBalanceRepository.deleteAllByUser(user);
        }
        user.setRoles(null);
        userRepository.save(user);
        userRepository.delete(user);
        session.removeAttribute("user");
        session.removeAttribute("logged");
        SecurityContextHolder.clearContext();
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

    private boolean verifySetting(Setting setting){
        return ((setting != null) && ((setting.getProteinPart() + setting.getCarbohydratePart() + setting.getFatPart()) != 100));
    }
}
