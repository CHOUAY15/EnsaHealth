package ma.ensa.projet.doctor.api.service.interfaces;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import ma.ensa.projet.doctor.api.dto.AuthResponseDTO;
import ma.ensa.projet.doctor.api.dto.DoctorDto;
import ma.ensa.projet.doctor.api.dto.LoginDto;
import ma.ensa.projet.doctor.api.dto.PatientDto;
import ma.ensa.projet.doctor.api.dto.PatientRegistrationDto;
import ma.ensa.projet.doctor.api.dto.PersonDto;
import ma.ensa.projet.doctor.api.entity.UserEntity;

public interface UserService {
    AuthResponseDTO authenticateUser(LoginDto loginDto);

    Authentication authenticate(LoginDto loginDto);

    UserEntity getUserByEmail(String email);

    PersonDto getPersonDto(UserEntity user);

    AuthResponseDTO buildAuthResponse(String token, PersonDto personDto, Integer role);

    PatientDto updatePatient(Integer id, PatientDto patientDto);

    DoctorDto updateDoctor(Integer id, DoctorDto patientDto);

    void initiatePasswordReset(String email);

    void confirmPasswordReset(String email, String token, String newPassword);

    UserEntity registerDoctor(String email, String password, String firstName, String lastName,
            String specialty, MultipartFile image, String address,
            int yearsExperience, String biography);

    UserEntity registerPatient(PatientRegistrationDto patientDto);
}