package ma.ensa.projet.doctor.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ma.ensa.projet.doctor.api.service.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    private JwtAuthEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/password").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/doctors/**").hasRole("PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasRole("DOCTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/patient/**").hasRole("PATIENT")

                        .requestMatchers(HttpMethod.GET, "/api/images").hasAnyRole("PATIENT", "DOCTOR")
                        .requestMatchers(HttpMethod.GET, "/api/rendv/**").hasAnyRole("PATIENT", "DOCTOR")

                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

}