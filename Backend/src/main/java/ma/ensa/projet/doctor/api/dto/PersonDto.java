package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public abstract class PersonDto {
    private Integer id;
    private String firstName;
    private String lastName;
}
