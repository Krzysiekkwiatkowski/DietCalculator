package pl.coderslab.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pl.coderslab.entity.DailyBalance;
import pl.coderslab.entity.Training;
import pl.coderslab.entity.User;
import pl.coderslab.repository.DailyBalanceRepository;
import pl.coderslab.repository.TrainingRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/diet/training")
@Controller
public class TrainingController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private DailyBalanceRepository dailyBalanceRepository;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("addTraining", "addTraining");
        model.addAttribute("training", new Training());
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@Valid Training training, BindingResult result, HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("addTraining", "addTraining");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(result.hasErrors()){
            model.addAttribute("addTraining", "addTraining");
            return "home";
        }
        User user = (User)object;
        double strIntensity = 0.0;
        double cardioIntensity = 0.0;
        if(training.getStrengthIntensity().equals("Umiarkowana")){
            strIntensity = 8.0;
        } else if(training.getStrengthIntensity().equals("Średnia")){
            strIntensity = 10.0;
        } else if(training.getStrengthIntensity().equals("Wysoka")){
            strIntensity = 12.0;
        }
        if(training.getCardioIntensity().equals("Niska")){
            cardioIntensity = 4;
        } else if(training.getCardioIntensity().equals("Umiarkowana")){
            cardioIntensity = 6.5;
        } else if(training.getCardioIntensity().equals("Średnia")){
            cardioIntensity = 8.0;
        } else if(training.getCardioIntensity().equals("Wysoka")){
            cardioIntensity = 9.5;
        } else if(training.getCardioIntensity().equals("Bardzo wysoka")){
            cardioIntensity = 11;
        }
        double calories = ((training.getStrengthDays() * training.getStrengthTime() * strIntensity) + (training.getCardioDays() * training.getCardioTime() * cardioIntensity)) / 7;
        training.setDailyCalories((int)calories);
        trainingRepository.save(training);
        user.setTraining(training);
        user.setTotalCalories(user.getTotalCalories() + training.getDailyCalories());
        user.setTotalProtein(user.getWeight() * 1.8);
        String goal = user.getGoal();
        int total = user.getTotalCalories();
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
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        userRepository.save(user);
        return "home";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editGet(@PathVariable("id") Long id, Model model, HttpSession session){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("editTraining", "editTraining");
        model.addAttribute("training", trainingRepository.findTopById(id));
        return "home";
    }

    @RequestMapping(value = "/edit/*", method = RequestMethod.POST)
    public String editPost(@Valid Training training, BindingResult result, HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(result.hasErrors()){
            model.addAttribute("editTraining", "editTraining");
            return "home";
        }
        User user = (User)object;
        double strIntensity = 0.0;
        double cardioIntensity = 0.0;
        if(training.getStrengthIntensity().equals("Umiarkowana")){
            strIntensity = 8.0;
        } else if(training.getStrengthIntensity().equals("Średnia")){
            strIntensity = 10.0;
        } else if(training.getStrengthIntensity().equals("Wysoka")){
            strIntensity = 12.0;
        }
        if(training.getCardioIntensity().equals("Niska")){
            cardioIntensity = 4;
        } else if(training.getCardioIntensity().equals("Umiarkowana")){
            cardioIntensity = 6.5;
        } else if(training.getCardioIntensity().equals("Średnia")){
            cardioIntensity = 8.0;
        } else if(training.getCardioIntensity().equals("Wysoka")){
            cardioIntensity = 9.5;
        } else if(training.getCardioIntensity().equals("Bardzo wysoka")){
            cardioIntensity = 11;
        }
        int oldDailyCalories = training.getDailyCalories();
        double calories = ((training.getStrengthDays() * training.getStrengthTime() * strIntensity) + (training.getCardioDays() * training.getCardioTime() * cardioIntensity)) / 7;
        training.setDailyCalories((int)calories);
        trainingRepository.save(training);
        user.setTotalCalories(user.getTotalCalories() - oldDailyCalories + training.getDailyCalories());
        user.setTotalProtein(user.getWeight() * 1.8);
        String goal = user.getGoal();
        int total = user.getTotalCalories();
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
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setNeeded(user.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        userRepository.save(user);
        return "home";
    }

    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        if(user.getTraining().getId() == id) {
            user.setTraining(null);
            userRepository.save(user);
            trainingRepository.deleteById(id);
        }
        return "home";
    }

    @ModelAttribute("intensityListStrength")
    public List<String> allIntensityStrength(){
        List<String> intensities = new ArrayList<>();
        intensities.add("Umiarkowana");
        intensities.add("Średnia");
        intensities.add("Wysoka");
        return intensities;
    }

    @ModelAttribute("intensityListCardio")
    public List<String> allIntensityCardio(){
        List<String> intensities = new ArrayList<>();
        intensities.add("Niska");
        intensities.add("Umiarkowana");
        intensities.add("Średnia");
        intensities.add("Wysoka");
        intensities.add("Bardzo wysoka");
        return intensities;
    }
}
