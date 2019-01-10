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
import java.text.DecimalFormat;
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
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        if(result.hasErrors()){
            model.addAttribute("addTraining", "addTraining");
            return "home";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
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
        loadedUser.setTraining(training);
        loadedUser.setTotalCalories(loadedUser.getTotalCalories() + training.getDailyCalories());
        loadedUser.setTotalProtein(loadedUser.getWeight() * 1.8);
        String goal = loadedUser.getGoal();
        int total = loadedUser.getTotalCalories();
        DecimalFormat decimalFormat =  new DecimalFormat("#.#");
        if(goal.equals("Utrata wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 9.0).replace(",", ".")));
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 9.0).replace(",", ".")));
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 9.0).replace(",", ".")));
        }
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setTotalProtein(loadedUser.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(loadedUser.getTotalCarbohydrates());
            dailyBalance.setTotalFat(loadedUser.getTotalFat());
            dailyBalance.setNeeded(loadedUser.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        userRepository.save(loadedUser);
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
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
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
        loadedUser.setTotalCalories(loadedUser.getTotalCalories() - oldDailyCalories + training.getDailyCalories());
        loadedUser.setTotalProtein(loadedUser.getWeight() * 1.8);
        String goal = loadedUser.getGoal();
        int total = loadedUser.getTotalCalories();
        DecimalFormat decimalFormat =  new DecimalFormat("#.#");
        if(goal.equals("Utrata wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 9.0).replace(",", ".")));
        }
        if(goal.equals("Utrzymanie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.5) / 9.0).replace(",", ".")));
        }
        if(goal.equals("Przybranie wagi")){
            user.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.6) / 4.0).replace(",", ".")));
            user.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * user.getWeight()) * 0.4) / 9.0).replace(",", ".")));
        }
        if(dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now())) != null) {
            DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
            dailyBalance.setTotalProtein(loadedUser.getTotalProtein());
            dailyBalance.setTotalCarbohydrates(loadedUser.getTotalCarbohydrates());
            dailyBalance.setTotalFat(loadedUser.getTotalFat());
            dailyBalance.setNeeded(loadedUser.getTotalCalories());
            dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
            dailyBalanceRepository.save(dailyBalance);
        }
        userRepository.save(loadedUser);
        return "home";
    }

    @RequestMapping(value = "/option")
    public String option(HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(loadedUser.getTraining() == null){
            model.addAttribute("logged", "logged");
            model.addAttribute("trainingOption", "trainingOption");
            model.addAttribute("training", null);
            return "home";
        }
        model.addAttribute("logged", "logged");
        model.addAttribute("trainingOption", "trainingOption");
        model.addAttribute("training", loadedUser.getTraining().getId());
        return "home";
    }

    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session, Model model){
        Object object = session.getAttribute("user");
        if(object == null){
            model.addAttribute("logged", null);
            model.addAttribute("loginForm", "loginForm");
            return "home";
        }
        model.addAttribute("logged", "logged");
        User user = (User)object;
        User loadedUser = userRepository.findTopByEmail(user.getEmail());
        if(loadedUser.getTraining().getId() == id) {
            Training training = loadedUser.getTraining();
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
            loadedUser.setTotalCalories(loadedUser.getTotalCalories() - oldDailyCalories + training.getDailyCalories());
            loadedUser.setTotalProtein(loadedUser.getWeight() * 1.8);
            String goal = loadedUser.getGoal();
            int total = loadedUser.getTotalCalories();
            DecimalFormat decimalFormat =  new DecimalFormat("#.#");
            if(goal.equals("Utrata wagi")){
                loadedUser.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.4) / 4.0).replace(",", ".")));
                loadedUser.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.6) / 9.0).replace(",", ".")));
            }
            if(goal.equals("Utrzymanie wagi")){
                loadedUser.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.5) / 4.0).replace(",", ".")));
                loadedUser.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.5) / 9.0).replace(",", ".")));
            }
            if(goal.equals("Przybranie wagi")){
                loadedUser.setTotalCarbohydrates(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.6) / 4.0).replace(",", ".")));
                loadedUser.setTotalFat(Double.parseDouble(decimalFormat.format(((total - 1.8 * 4 * loadedUser.getWeight()) * 0.4) / 9.0).replace(",", ".")));
            }
            if(dailyBalanceRepository.findTopByUserIdAndAndDate(user.getId(), Date.valueOf(LocalDate.now())) != null) {
                DailyBalance dailyBalance = dailyBalanceRepository.findTopByUserIdAndAndDate(loadedUser.getId(), Date.valueOf(LocalDate.now()));
                dailyBalance.setTotalProtein(loadedUser.getTotalProtein());
                dailyBalance.setTotalCarbohydrates(loadedUser.getTotalCarbohydrates());
                dailyBalance.setTotalFat(loadedUser.getTotalFat());
                dailyBalance.setNeeded(user.getTotalCalories());
                dailyBalance.setBalance(dailyBalance.getReceived() - dailyBalance.getNeeded());
                dailyBalanceRepository.save(dailyBalance);
            }
            loadedUser.setTraining(null);
            userRepository.save(loadedUser);
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
