package ma.ensa.projet.doctor.api.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "specialty")
    private String specialty;

    @Column(name = "imageUrl")
    private String image;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "years_experience")
    private Integer yearsExperience;
    
    @Column(name = "biography", length = 1000)
    private String biography;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RendezVous> rendezVousList;
}

