package ma.ensa.projet.doctor.api.service.impl;

import lombok.RequiredArgsConstructor;
import ma.ensa.projet.doctor.api.dto.DoctorDto;
import ma.ensa.projet.doctor.api.entity.Doctor;
import ma.ensa.projet.doctor.api.repository.DoctorRepo;
import ma.ensa.projet.doctor.api.service.interfaces.DoctorService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepo doctorRepo;

    @Override
    public List<DoctorDto> getAllDoctors() {
        List<Doctor> doctors = doctorRepo.findAll();
        return doctors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> getAllDoctorsBySpeciality(String specialty) {
        List<Doctor> doctors = doctorRepo.findBySpecialty(specialty);
        return doctors.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DoctorDto convertToDto(Doctor doctor) {
        DoctorDto doctorDto = new DoctorDto();
        doctorDto.setId(doctor.getId());
        doctorDto.setFirstName(doctor.getFirstName());
        doctorDto.setLastName(doctor.getLastName());
        doctorDto.setSpecialty(doctor.getSpecialty());
        doctorDto.setAddress(doctor.getAddress());
        doctorDto.setYearsExperience(doctor.getYearsExperience());
        doctorDto.setBiography(doctor.getBiography());
        doctorDto.setImage(doctor.getImage());
        return doctorDto;
    }
    
    public Optional<Doctor> findById(Integer id) {
        return doctorRepo.findById(id);
    }
}