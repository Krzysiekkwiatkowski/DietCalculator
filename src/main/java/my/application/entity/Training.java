package my.application.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String strengthIntensity;
    @Min(0)
    private int strengthTime;
    @Min(0)
    private int strengthDays;
    @NotBlank
    private String cardioIntensity;
    @Min(0)
    private int cardioTime;
    @Min(0)
    private int cardioDays;
    private int dailyCalories;
}
