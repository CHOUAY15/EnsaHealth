package ma.ensa.projet.doctor.api.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ErrorResponse {
    private String message;
   
    private String code;
    
   
}
