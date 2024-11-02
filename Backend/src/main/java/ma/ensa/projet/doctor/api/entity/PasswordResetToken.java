package ma.ensa.projet.doctor.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private LocalDateTime expiryDate;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    
    private boolean used;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}