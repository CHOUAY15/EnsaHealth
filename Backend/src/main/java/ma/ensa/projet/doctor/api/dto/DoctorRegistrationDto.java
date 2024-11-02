package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class DoctorRegistrationDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String specialty;
    private String image;
    private String address;
    private Integer yearsExperience;
    private String biography;
}