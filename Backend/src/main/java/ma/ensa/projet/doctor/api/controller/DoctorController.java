package ma.ensa.projet.doctor.api.controller;

import ma.ensa.projet.doctor.api.dto.DoctorDto;
import ma.ensa.projet.doctor.api.entity.Doctor;
import ma.ensa.projet.doctor.api.service.impl.FileStorageService;
import ma.ensa.projet.doctor.api.service.interfaces.DoctorService;
import ma.ensa.projet.doctor.api.service.interfaces.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/doctors")


public class DoctorController {



    @Autowired
    private  DoctorService doctorService;
    
    @Autowired
     private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        if (doctors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorDto>> getDoctorsBySpecialty(
            @PathVariable String specialty) {
        List<DoctorDto> doctors = doctorService.getAllDoctorsBySpeciality(specialty);
        if (doctors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(doctors);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DoctorDto> update(
            @PathVariable Integer id,
            
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("specialty") String specialty,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("address") String address,
            @RequestParam("yearsExperience") int yearsExperience,
            @RequestParam("biography") String biography) {

        try {
            Doctor existingDoctor = doctorService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

            DoctorDto doctorDto = new DoctorDto();
            doctorDto.setId(id); 
            doctorDto.setFirstName(firstName);
            doctorDto.setLastName(lastName);
            doctorDto.setSpecialty(specialty);
            doctorDto.setAddress(address);
            doctorDto.setYearsExperience(yearsExperience);
            doctorDto.setBiography(biography);
         
      

            if (image != null && !image.isEmpty()) {
                if (existingDoctor.getImage() != null) {
                    fileStorageService.deleteFile(existingDoctor.getImage());
                }
                String fileName = fileStorageService.storeFile(image);
                doctorDto.setImage(fileName);
            }

            DoctorDto updatedDoctorDto = userService.updateDoctor(id, doctorDto);

            return new ResponseEntity<>(updatedDoctorDto, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}