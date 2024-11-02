package ma.ensa.projet.doctor.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor
public class DoctorDto extends PersonDto {
    private String specialty;
    private String address;
    private Integer yearsExperience;
    private String biography;
    private String image;
}
