package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class PatientRegistrationDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
