package my.application.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int mealNumber;
    @ManyToMany
    @NotNull
    private List<Product> products;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalProtein;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalCarbohydrates;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalFat;
    @Min(0)
    private int totalCalories;
    @Min(0)
    private double glycemicCharge;
}
