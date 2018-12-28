package pl.coderslab.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private int mealNumber;
    @ManyToMany
    private List<Product> products;
    @Column(scale = 1, precision = 5)
    private double totalProtein;
    @Column(scale = 1, precision = 5)
    private double totalCarbohydrates;
    @Column(scale = 1, precision = 5)
    private double totalFat;
    private int totalCalories;
}
