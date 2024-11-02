package ma.ensa.projet.doctor.api.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import ma.ensa.projet.doctor.api.dto.AuthResponseDTO;
import ma.ensa.projet.doctor.api.dto.DoctorDto;
import ma.ensa.projet.doctor.api.dto.LoginDto;
import ma.ensa.projet.doctor.api.dto.PatientDto;
import ma.ensa.projet.doctor.api.dto.PatientRegistrationDto;
import ma.ensa.projet.doctor.api.dto.PersonDto;
import ma.ensa.projet.doctor.api.entity.Doctor;
import ma.ensa.projet.doctor.api.entity.PasswordResetToken;
import ma.ensa.projet.doctor.api.entity.Patient;
import ma.ensa.projet.doctor.api.entity.UserEntity;
import ma.ensa.projet.doctor.api.repository.DoctorRepo;
import ma.ensa.projet.doctor.api.repository.PasswordResetTokenRepository;
import ma.ensa.projet.doctor.api.repository.PatientRepo;
import ma.ensa.projet.doctor.api.repository.UserRepo;
import ma.ensa.projet.doctor.api.service.interfaces.UserService;
import ma.ensa.projet.doctor.security.JWTGenerator;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private JWTGenerator jwtGenerator;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DoctorRepo doctorRepository;
    @Autowired
    private PatientRepo patientRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Override
    @Transactional
    public AuthResponseDTO authenticateUser(LoginDto loginDto) {
        try {
            Authentication authentication = authenticate(loginDto);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserEntity user = getUserByEmail(loginDto.getEmail());
            String token = jwtGenerator.generateToken(authentication);

            PersonDto personDto = getPersonDto(user);

            return buildAuthResponse(token, personDto, user.getRole());
        } catch (UsernameNotFoundException e) {
            throw new AuthenticationException("User not found") {
            };
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed") {
            };
        }
    }

    @Override
    public Authentication authenticate(LoginDto loginDto) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public PersonDto getPersonDto(UserEntity user) {
        if (user.getRole() == 0) { 
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            PatientDto patientDto = new PatientDto();
            patientDto.setId(patient.getId());
            patientDto.setFirstName(patient.getFirstName());
            patientDto.setLastName(patient.getLastName());
            patientDto.setPhoneNumber(patient.getPhoneNumber());

            return patientDto;

        } else if (user.getRole() == 1) { 
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            DoctorDto doctorDto = new DoctorDto();
            doctorDto.setImage(doctor.getImage());
            doctorDto.setId(doctor.getId());
            doctorDto.setFirstName(doctor.getFirstName());
            doctorDto.setLastName(doctor.getLastName());
            doctorDto.setAddress(doctor.getAddress());
            doctorDto.setBiography(doctor.getBiography());
            doctorDto.setSpecialty(doctor.getSpecialty());
            doctorDto.setYearsExperience(doctor.getYearsExperience());

            return doctorDto;
        }

        throw new IllegalStateException("Invalid user role");
    }

    @Override
    public AuthResponseDTO buildAuthResponse(String token, PersonDto personDto, Integer role) {
        return new AuthResponseDTO(
                token,
                "Bearer ",
                personDto, role);
    }

    @Override
    public PatientDto updatePatient(Integer id, PatientDto patientDto) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));

        existingPatient.setFirstName(patientDto.getFirstName());
        existingPatient.setLastName(patientDto.getLastName());
        existingPatient.setPhoneNumber(patientDto.getPhoneNumber());

        Patient updatedPatient = patientRepository.save(existingPatient);
        PatientDto patientDto2 = new PatientDto();
        patientDto2.setFirstName(updatedPatient.getFirstName());
        patientDto2.setLastName(updatedPatient.getLastName());
        patientDto2.setPhoneNumber(updatedPatient.getPhoneNumber());
        patientDto2.setId(updatedPatient.getId());

        return patientDto2;
    }

    @Override
    public DoctorDto updateDoctor(Integer id, DoctorDto doctorDto) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        existingDoctor.setSpecialty(doctorDto.getSpecialty());
        existingDoctor.setAddress(doctorDto.getAddress());
        existingDoctor.setYearsExperience(doctorDto.getYearsExperience());
        existingDoctor.setBiography(doctorDto.getBiography());
        existingDoctor.setImage(doctorDto.getImage());
        existingDoctor.setFirstName(doctorDto.getFirstName());
        existingDoctor.setLastName(doctorDto.getLastName());

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);

        DoctorDto doctorDtoResponse = new DoctorDto();

        doctorDtoResponse.setId(updatedDoctor.getId()); 
        doctorDtoResponse.setFirstName(updatedDoctor.getFirstName());
        doctorDtoResponse.setLastName(updatedDoctor.getLastName());
        doctorDtoResponse.setSpecialty(updatedDoctor.getSpecialty());
        doctorDtoResponse.setAddress(updatedDoctor.getAddress());
        doctorDtoResponse.setYearsExperience(updatedDoctor.getYearsExperience());
        doctorDtoResponse.setBiography(updatedDoctor.getBiography());
        doctorDtoResponse.setImage(updatedDoctor.getImage());

        return doctorDtoResponse;
    }

     @Override
    public void initiatePasswordReset(String email) {
        UserEntity user = getUserByEmail(email);
        
        String token = String.format("%06d", new Random().nextInt(999999));
        
        PasswordResetToken resetToken = tokenRepository.findByUserEmail(email)
                .orElse(new PasswordResetToken());
        
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        String emailBody = "Votre code de réinitialisation de mot de passe est: " + token + 
                         "\nCe code expirera dans 15 minutes.";
        emailService.sendEmail(email, "Code de réinitialisation de mot de passe", emailBody);
    }

    @Override
    public void confirmPasswordReset(String email, String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("No reset request found"));
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Reset token has expired");
        }
        
        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }
        
        if (!resetToken.getToken().equals(token)) {
            throw new RuntimeException("Invalid reset token");
        }
        
        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    @Transactional
    @Override
    public UserEntity registerDoctor(String email, String password, String firstName, String lastName,
                                     String specialty, MultipartFile image, String address,
                                     int yearsExperience, String biography) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already taken!");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(1); // 1 for Doctor role

        UserEntity savedUser = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setFirstName(firstName);
        doctor.setLastName(lastName);
        doctor.setSpecialty(specialty);
        doctor.setAddress(address);
        doctor.setYearsExperience(yearsExperience);
        doctor.setBiography(biography);
        doctor.setUser(savedUser);

        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            doctor.setImage(fileName);
        }

        doctorRepository.save(doctor);

        return savedUser;
    }

    @Override
    public UserEntity registerPatient(PatientRegistrationDto patientDto) {
        if (userRepository.existsByEmail(patientDto.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        UserEntity user = new UserEntity();
        user.setEmail(patientDto.getEmail());
        user.setPassword(passwordEncoder.encode(patientDto.getPassword()));
        user.setRole(0); // 0 for Patient role

        UserEntity savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setFirstName(patientDto.getFirstName());
        patient.setLastName(patientDto.getLastName());
        patient.setPhoneNumber(patientDto.getPhoneNumber());
        patient.setUser(savedUser);

        patientRepository.save(patient);

        return savedUser;
    }
    
}
