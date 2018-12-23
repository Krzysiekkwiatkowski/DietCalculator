package pl.coderslab.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private double weight;
    private String activity;
    private String buildType;
    private String goal;
    @OneToOne
    private Require require;
}
