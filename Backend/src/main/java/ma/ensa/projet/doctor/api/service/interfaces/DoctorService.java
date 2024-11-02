package ma.ensa.projet.doctor.api.service.interfaces;

import java.util.List;
import java.util.Optional;

import ma.ensa.projet.doctor.api.dto.DoctorDto;
import ma.ensa.projet.doctor.api.entity.Doctor;

public interface DoctorService {

    List <DoctorDto> getAllDoctors();
    List <DoctorDto> getAllDoctorsBySpeciality(String speciality);
    Optional<Doctor> findById(Integer id);

}
