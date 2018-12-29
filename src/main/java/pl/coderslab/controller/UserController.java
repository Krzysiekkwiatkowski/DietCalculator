package pl.coderslab.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import pl.coderslab.entity.User;
import pl.coderslab.repository.UserRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/user")
@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerGet(Model model){
        model.addAttribute("user", new User());
        return "registerUser";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerPost(@Valid User user, BindingResult result){
        if(result.hasErrors()){
            return "registerUser";
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
        System.out.println(activity);
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
        }
        if(goal.equals("Utrata wagi")){
            goalFactor = -500;
        } else if(goal.equals("Przybranie wagi")){
            goalFactor = 500;
        }
        System.out.println((int) metabolism + activityFactor + somatotypeFactor + goalFactor);
        userRepository.save(user);
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginGet(){
        return "loginForm";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost(@RequestParam("email") String email, @RequestParam("password") String password, Model model){
        User user = userRepository.findTopByEmail(email);
        if(BCrypt.checkpw(password, user.getPassword())){
            return "home";
        }
        model.addAttribute("wrong", "wrong");
        return "loginForm";
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
