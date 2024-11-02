package ma.ensa.projet.doctor.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensa.projet.doctor.api.entity.Patient;

@Repository

public interface PatientRepo extends JpaRepository<Patient,Integer> {
    Optional<Patient> findByUserId(Integer userId);


    
}
