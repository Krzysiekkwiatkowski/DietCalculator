package pl.coderslab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.coderslab.entity.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/user")
@Controller
public class UserController {

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model){
        model.addAttribute("user", new User());
        return "addUser";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addPost(@Valid User user, BindingResult result){
        if(result.hasErrors()){
            return "addUser";
        }
        return "home";
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
