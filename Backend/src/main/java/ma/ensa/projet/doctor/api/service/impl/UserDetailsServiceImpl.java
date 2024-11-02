package ma.ensa.projet.doctor.api.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ma.ensa.projet.doctor.api.entity.UserEntity;
import ma.ensa.projet.doctor.api.repository.UserRepo;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("user n'est pas exist"));
        
        String email = user.getEmail();
        String password = user.getPassword();
        Integer role = user.getRole();

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role == 0) {
            authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
        } else if (role == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR")); 
        }

        return new User(email, password, authorities);
    }
}