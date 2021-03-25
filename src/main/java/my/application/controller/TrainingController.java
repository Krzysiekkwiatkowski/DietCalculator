package my.application.controller;

import my.application.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import my.application.entity.Training;
import my.application.helper.ContextHelper;
import my.application.helper.DailyBalanceHelper;
import my.application.helper.TrainingHelper;
import my.application.helper.UserHelper;
import my.application.repository.TrainingRepository;
import my.application.repository.UserRepository;

import javax.validation.Valid;
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
    private UserHelper userHelper;

    @Autowired
    private TrainingHelper trainingHelper;

    @Autowired
    private DailyBalanceHelper dailyBalanceHelper;

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addGet(Model model){
        model.addAttribute("addTraining", "addTraining");
        model.addAttribute("training", new Training());
        return "home";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addPost(@Valid Training training, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("addTraining", "addTraining");
            return "home";
        }
        User user = ContextHelper.getUserFromContext();
        updateEntities(user, training);
        return "home";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String editGet(@PathVariable("id") Long id, Model model){
        model.addAttribute("editTraining", "editTraining");
        model.addAttribute("training", trainingRepository.findTopById(id));
        return "home";
    }

    @RequestMapping(value = "/edit/*", method = RequestMethod.POST)
    public String editPost(@Valid Training training, BindingResult result, Model model){
        if(result.hasErrors()){
            model.addAttribute("editTraining", "editTraining");
            return "home";
        }
        User user = ContextHelper.getUserFromContext();
        updateEntities(user, training);
        return "home";
    }

    @RequestMapping(value = "/option")
    public String option(Model model){
        User user = ContextHelper.getUserFromContext();
        if(user.getTraining() == null){
            model.addAttribute("trainingOption", "trainingOption");
            model.addAttribute("training", null);
            return "home";
        }
        model.addAttribute("trainingOption", "trainingOption");
        model.addAttribute("training", user.getTraining().getId());
        return "home";
    }

    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable("id") Long id){
        User user = ContextHelper.getUserFromContext();
        if(user.getTraining().getId() == id) {
            user.setTraining(null);
            userHelper.calculateMacroElements(user);
            userRepository.save(user);
            dailyBalanceHelper.updateActualDailyBalance(user);
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

    private void updateEntities(User user, Training training){
        user.setTraining(training);
        int calories = trainingHelper.calculateDailyTrainingCalories(user);
        training.setDailyCalories(calories);
        trainingRepository.save(training);
        userHelper.calculateMacroElements(user);
        userRepository.save(user);
        dailyBalanceHelper.updateActualDailyBalance(user);
    }
}
