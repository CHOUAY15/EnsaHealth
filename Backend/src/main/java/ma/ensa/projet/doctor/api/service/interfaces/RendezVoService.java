package ma.ensa.projet.doctor.api.service.interfaces;

import java.util.List;

import ma.ensa.projet.doctor.api.dto.RendezVousDTO;
import ma.ensa.projet.doctor.api.entity.RendezVousId;

public interface RendezVoService {

    RendezVousDTO createRendezVous(RendezVousDTO rendezVousDTO);

    void deleteRendezVous(RendezVousId rendezVousId);

    RendezVousDTO updateRendezVous(Integer doctorId,String date, RendezVousDTO rendezVousDTO);

    List<RendezVousDTO> getAllRendezVous();

    List<RendezVousDTO> getRendezVousByPatientId(Integer patId);

    List<RendezVousDTO> getRendezVousByDoctorId(Integer docId);



}