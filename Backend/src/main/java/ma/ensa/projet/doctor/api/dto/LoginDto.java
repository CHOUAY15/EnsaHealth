package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class LoginDto {
    private String email;
    private String password;
}
