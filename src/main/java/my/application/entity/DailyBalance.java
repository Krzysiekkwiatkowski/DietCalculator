package my.application.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DailyBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    @OneToMany
    private List<Meal> meals;
    @ManyToOne
    private User user;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalProtein;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalCarbohydrates;
    @Min(0)
    @Column(scale = 1, precision = 5)
    private double totalFat;
    private int needed;
    private int received;
    private int balance;
}
