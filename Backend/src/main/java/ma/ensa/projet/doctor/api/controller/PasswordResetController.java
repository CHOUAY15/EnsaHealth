package ma.ensa.projet.doctor.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ma.ensa.projet.doctor.api.dto.PasswordResetRequestDto;
import ma.ensa.projet.doctor.api.dto.PasswordResetConfirmDto;
import ma.ensa.projet.doctor.api.service.interfaces.UserService;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @PostMapping("/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequestDto request) {
        try {
            userService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok().body("Password reset code has been sent to your email");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetConfirmDto request) {
        try {
            userService.confirmPasswordReset(
                request.getEmail(),
                request.getToken(),
                request.getNewPassword()
            );
            return ResponseEntity.ok().body("Password has been reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}