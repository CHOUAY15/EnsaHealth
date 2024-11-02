package ma.ensa.projet.doctor.api.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @NoArgsConstructor @AllArgsConstructor
public class PatientDto extends PersonDto {
    private String phoneNumber;
}
