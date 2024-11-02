package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class PasswordResetRequestDto {
    private String email;
}