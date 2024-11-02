package ma.ensa.projet.doctor.api.controller;

import ma.ensa.projet.doctor.api.entity.UserEntity;

import ma.ensa.projet.doctor.api.service.interfaces.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth/doctor")
@CrossOrigin(origins = "*")
public class DoctorAuthController {

 
    

      @Autowired
    private UserService userService;


    

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("specialty") String specialty,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("address") String address,
            @RequestParam("yearsExperience") int yearsExperience,
            @RequestParam("biography") String biography) {
        
        try {
            UserEntity savedUser = userService.registerDoctor(email, password, firstName, lastName,
                    specialty, image, address, yearsExperience, biography);
            return new ResponseEntity<>("Doctor has been registered", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}