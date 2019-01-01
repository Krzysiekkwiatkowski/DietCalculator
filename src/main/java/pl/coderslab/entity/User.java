package pl.coderslab.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String nickname;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String gender;
    @Min(0)
    private int age;
    @Min(0)
    private int height;
    @Min(0)
    private double weight;
    @NotBlank
    private String activity;
    @NotBlank
    private String somatotype;
    @NotBlank
    private String goal;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<DailyBalance> dailyBalances;
    @OneToOne
    private Training training;
    private double totalProtein;
    private double totalCarbohydrates;
    private double totalFat;
    private int totalCalories;
}
