package ma.ensa.projet.doctor.api.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer ";
    private PersonDto personDto;
    private Integer role;

    public AuthResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    public AuthResponseDTO(String accessToken, String tokenType, PersonDto personDto,Integer role) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.personDto = personDto;
        this.role=role;
    }
}