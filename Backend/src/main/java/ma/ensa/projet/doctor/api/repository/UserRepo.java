package ma.ensa.projet.doctor.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ma.ensa.projet.doctor.api.entity.UserEntity;

@Repository

public interface UserRepo  extends  JpaRepository<UserEntity,Integer>{

    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
    
}
