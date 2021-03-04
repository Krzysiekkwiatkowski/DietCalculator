package pl.coderslab.pojo;

public class MissingMacro {
    private double protein;
    private double carbohydrates;
    private double fat;
    private int calories;

    public MissingMacro(double protein, double carbohydrates, double fat, int calories){
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public double getFat() {
        return fat;
    }

    public int getCalories() {
        return calories;
    }
}
