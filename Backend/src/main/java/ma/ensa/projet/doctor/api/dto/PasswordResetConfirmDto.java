package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmDto {
    private String email;
    private String token;
    private String newPassword;
}