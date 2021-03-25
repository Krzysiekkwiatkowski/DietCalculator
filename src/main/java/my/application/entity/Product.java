package my.application.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @Min(+0)
    @Max(+100)
    @Column(scale = 1, precision = 5)
    private double protein;
    @Min(0)
    @Max(100)
    @Column(scale = 1, precision = 5)
    private double carbohydrates;
    @Min(0)
    @Max(100)
    @Column(scale = 1, precision = 5)
    private double fat;
    @Min(0)
    private int calories;
    @Min(0)
    @Max(103)
    private int glycemicIndex;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;
    @Min(0)
    private int weight;

    @Override
    public String toString(){
        return "Product{" +
                "id="+ id +
                ",name="+ name +
                ",protein="+ protein +
                ",carbohydrates="+ carbohydrates +
                ",fat="+ fat +
                ",calories="+ calories +
                ",glycemicIndex="+ glycemicIndex +
                ",category="+ category.getName() +
                ",weight="+ weight +
                "}";
    }
}
