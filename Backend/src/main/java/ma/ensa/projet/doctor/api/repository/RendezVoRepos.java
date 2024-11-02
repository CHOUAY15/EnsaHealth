package ma.ensa.projet.doctor.api.repository;




import java.util.List;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensa.projet.doctor.api.entity.RendezVous;
import ma.ensa.projet.doctor.api.entity.RendezVousId;

@Repository
public interface RendezVoRepos extends JpaRepository<RendezVous, RendezVousId> {
    
    List<RendezVous> findByPatientId(Integer patientId);
    List<RendezVous> findByDoctorId(Integer doctorId);
    boolean existsByDate(String date);
    boolean existsByDoctorIdAndDate(Integer doctorId, String date);


    
}
