package pl.coderslab.helper;

import org.springframework.stereotype.Component;
import pl.coderslab.entity.Training;
import pl.coderslab.entity.User;

import java.time.DayOfWeek;

@Component
public class TrainingHelper {

    private static final int DAYS_IN_WEEK = DayOfWeek.values().length;
    private static final double STRENGTH_INTENSITY_LOW = 8.0;
    private static final double STRENGTH_INTENSITY_MEDIUM = 10.0;
    private static final double STRENGTH_INTENSITY_HIGH = 12.0;
    private static final double CARDIO_INTENSITY_VERY_LOW = 4.0;
    private static final double CARDIO_INTENSITY_LOW = 6.5;
    private static final double CARDIO_INTENSITY_MEDIUM = 8.0;
    private static final double CARDIO_INTENSITY_HIGH = 9.5;
    private static final double CARDIO_INTENSITY_VERY_HIGH = 11.0;


    public int calculateDailyTrainingCalories(User user){
        int strengthCalories = calculateStrengthTrainingWeeklyCalories(user);
        int cardioCalories = calculateCardioTrainingWeeklyCalories(user);
        return (strengthCalories + cardioCalories) / DAYS_IN_WEEK;
    }

    private int calculateStrengthTrainingWeeklyCalories(User user){
        Training training = user.getTraining();
        if(training != null){
            double strengthIntensity = 0.0;
            switch(training.getStrengthIntensity()){
                case "Umiarkowana":
                    strengthIntensity = STRENGTH_INTENSITY_LOW;
                    break;
                case "Średnia":
                    strengthIntensity = STRENGTH_INTENSITY_MEDIUM;
                    break;
                case "Wysoka":
                    strengthIntensity = STRENGTH_INTENSITY_HIGH;
                    break;
            }
            return (int)(training.getStrengthDays() * training.getStrengthTime() * strengthIntensity);
        }
        return 0;
    }

    private int calculateCardioTrainingWeeklyCalories(User user){
        Training training = user.getTraining();
        if(training != null){
            double cardioIntensity = 0.0;
            switch(training.getCardioIntensity()){
                case "Niska":
                    cardioIntensity = CARDIO_INTENSITY_VERY_LOW;
                    break;
                case "Umiarkowana":
                    cardioIntensity = CARDIO_INTENSITY_LOW;
                    break;
                case "Średnia":
                    cardioIntensity = CARDIO_INTENSITY_MEDIUM;
                    break;
                case "Wysoka":
                    cardioIntensity = CARDIO_INTENSITY_HIGH;
                    break;
                case "Bardzo wysoka":
                    cardioIntensity = CARDIO_INTENSITY_VERY_HIGH;
                    break;
            }
            return (int)(training.getCardioDays() * training.getCardioTime() * cardioIntensity);
        }
        return 0;
    }
}
