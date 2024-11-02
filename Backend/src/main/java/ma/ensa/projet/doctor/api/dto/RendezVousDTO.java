package ma.ensa.projet.doctor.api.dto;

import lombok.Data;


@Data
public class RendezVousDTO {
 

  
    private String date;

    private Integer patientId;

  
    private Integer doctorId;

    private boolean statut;
    private String ptienName;
    private String doctorName;



 
}
