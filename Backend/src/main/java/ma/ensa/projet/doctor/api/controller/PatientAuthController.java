package ma.ensa.projet.doctor.api.controller;

import ma.ensa.projet.doctor.api.dto.PatientRegistrationDto;
import ma.ensa.projet.doctor.api.entity.UserEntity;
import ma.ensa.projet.doctor.api.service.interfaces.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/patient")
public class PatientAuthController {

    @Autowired
    private UserService patientService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody PatientRegistrationDto patientDto) {
        try {
            UserEntity savedUser = patientService.registerPatient(patientDto);
            return new ResponseEntity<>("Patient has been registered", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
