package pl.coderslab.pojo;

public class BasicMacro {
    private double protein;
    private double carbohydrates;
    private double fat;
    private int calories;

    public BasicMacro(){

    }

    public BasicMacro(double protein, double carbohydrates, double fat, int calories){
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

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
