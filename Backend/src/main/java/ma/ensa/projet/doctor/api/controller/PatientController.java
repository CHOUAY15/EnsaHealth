package ma.ensa.projet.doctor.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.ensa.projet.doctor.api.dto.PatientDto;
import ma.ensa.projet.doctor.api.service.interfaces.UserService;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

  

    @Autowired
     private UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Integer id, @RequestBody PatientDto patientDto) {
     
        
        PatientDto updatedPatient = userService.updatePatient(id,patientDto);
        
        return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
    }
}