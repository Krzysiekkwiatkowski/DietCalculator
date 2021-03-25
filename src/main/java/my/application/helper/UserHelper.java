package my.application.helper;

import my.application.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserHelper {

    @Autowired
    private TrainingHelper trainingHelper;

    @Autowired
    private NumberHelper numberHelper;

    private static final double PROTEIN_CALORIES = 4.0;
    private static final double CARBOHYDRATES_CALORIES = 4.0;
    private static final double FAT_CALORIES = 9.0;
    private static final double WEIGHT_FACTOR = 9.99;
    private static final double HEIGHT_FACTOR = 6.25;
    private static final double AGE_FACTOR = 4.92;
    private static final double AVERAGE_FOOD_THERMAL_EFFECT = 1.1;
    private static final double PROTEIN_PART = 1.8;
    private static final int GENDER_FACTOR_MAN = 5;
    private static final int GENDER_FACTOR_WOMAN = -161;


    public void calculateMacroElements(User user){
        int basicMetabolism = calculateBasicMetabolism(user);
        int somatotypeFactor = calculateSomatotypeFactor(user);
        int activityFactor = calculateActivityFactor(user);
        int goalFactor = calculateGoalFactor(user);
        int trainingFactor = trainingHelper.calculateDailyTrainingCalories(user);
        int total = basicMetabolism + somatotypeFactor + activityFactor + goalFactor + trainingFactor + user.getCorrect();
        user.setTotalCalories(total);
        setMacroElements(user);
    }

    private int calculateBasicMetabolism(User user) {
        int genderFactor;
        if(user.getGender().equals("Mężczyzna")){
            genderFactor = GENDER_FACTOR_MAN;
        } else {
            genderFactor = GENDER_FACTOR_WOMAN;
        }
        return (int)(((WEIGHT_FACTOR * user.getWeight()) + (HEIGHT_FACTOR * user.getHeight()) - (AGE_FACTOR * user.getAge()) + genderFactor) * AVERAGE_FOOD_THERMAL_EFFECT);
    }

    private int calculateSomatotypeFactor(User user){
        switch(user.getSomatotype()){
            case "Ektomorfik":
                return 800;
            case "Endomorfik":
                return 400;
            case "Mezomorfik":
                return 450;
            default:
                return 0;
        }
    }

    private int calculateActivityFactor(User user){
        int activityFactor = 0;
        switch(user.getActivity()){
            case "Średnia":
                activityFactor = 100;
                break;
            case "Duża":
                activityFactor = 200;
                break;
            case "Umiarkowana fizyczna":
                activityFactor = 300;
                break;
            case "Ciężka fizyczna":
                activityFactor = 400;
                break;
        }
        if(user.getSomatotype().equals("Mezomorfik")){
            return activityFactor / 2;
        }
        return activityFactor;
    }

    private int calculateGoalFactor(User user){
        switch(user.getGoal()){
            case "Utrata wagi":
                return -500;
            case "Przybranie wagi":
                return 500;
            default:
                return 0;
        }
    }

    private void setMacroElements(User user){
        double protein;
        double carbohydrates;
        double fat;
        int calories;
        if(!user.isSelfDistribution()){
            protein = numberHelper.roundDouble(user.getWeight() * PROTEIN_PART);
            MacroParam macroParam = getMacroElementsParam(user);
            carbohydrates = numberHelper.roundDouble((((user.getTotalCalories() - PROTEIN_PART * PROTEIN_CALORIES * user.getWeight()) * macroParam.carbohydratesParam) / CARBOHYDRATES_CALORIES));
            fat = numberHelper.roundDouble((((user.getTotalCalories() - PROTEIN_PART * PROTEIN_CALORIES * user.getWeight()) * macroParam.fatParam) / FAT_CALORIES));
            calories = user.getTotalCalories();
        } else {
            protein = numberHelper.roundDouble((user.getTotalCalories() * user.getSetting().getProteinPart()) / (PROTEIN_CALORIES * 100.0));
            carbohydrates = numberHelper.roundDouble((user.getTotalCalories() * user.getSetting().getCarbohydratePart()) / (CARBOHYDRATES_CALORIES * 100.0));
            fat = numberHelper.roundDouble((user.getTotalCalories() * user.getSetting().getFatPart()) / (FAT_CALORIES * 100.0));
            calories = (int)(protein * PROTEIN_CALORIES + carbohydrates * CARBOHYDRATES_CALORIES + fat * FAT_CALORIES);
        }
        user.setTotalProtein(protein);
        user.setTotalCarbohydrates(carbohydrates);
        user.setTotalFat(fat);
        user.setTotalCalories(calories);
    }

    private MacroParam getMacroElementsParam(User user){
        switch (user.getGoal()){
            case "Utrata wagi":
                return new MacroParam(0.4 , 0.6);
            case "Utrzymanie wagi":
                return new MacroParam(0.5, 0.5);
            case "Przybranie wagi":
                return new MacroParam(0.6, 0.4);
            default:
                return new MacroParam(0.55, 0.45);
        }
    }

    private class MacroParam {
        private double carbohydratesParam;
        private double fatParam;

        private MacroParam(double carbohydratesParam, double fatParam){
            this.carbohydratesParam = carbohydratesParam;
            this.fatParam = fatParam;
        }
    }
}
