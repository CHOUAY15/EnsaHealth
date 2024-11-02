package ma.ensa.projet.doctor.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensa.projet.doctor.api.entity.Doctor;
@Repository


public interface DoctorRepo extends JpaRepository<Doctor,Integer> {

        Optional<Doctor> findByUserId(Integer userId);
        
        List<Doctor> findBySpecialty(String specialty);

    
}
