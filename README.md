# Medical Appointment Management Mobile App

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
[![Docker](https://img.shields.io/badge/docker-supported-blue.svg)](https://www.docker.com/)
[![Android](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Coverage](https://img.shields.io/badge/coverage-87%25-green.svg)](https://sonarqube.org/)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/CHOUAY15/EnsaHealth)

A modern mobile application for managing medical appointments using REST architecture and Retrofit
</div>


## 📱 Project Overview

This project demonstrates a working implementation of REST architectural style using the Retrofit library. Through Retrofit, mobile applications can efficiently communicate with the management system, ensuring smooth operation and interaction.

The specified architecture is consistently translated into asynchronous request processing and automatic JSON data pulling, ensuring high efficiency and convenient information handling.

This solution is illustrated through the creation of a mobile application for appointment booking, aligning with the current trend of healthcare sector digitalization. Indeed, improving appointment management is a strategic challenge for healthcare facilities as it not only makes services more efficient but also enhances patient satisfaction.

## 🏗️ Architecture

![WhatsApp Image 2024-11-02 à 11 00 40_fbc5e0a5](https://github.com/user-attachments/assets/c2b0d9f4-c455-49fe-9076-0e5ddc139e1e)


## 🏗️ Architecture Details

### Backend Architecture (Spring Boot)

```
src/main/java/ma/ensa/doctor/
├── api
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── Exception/
│   ├── repository/
│   ├── service/
│   └── security/
│       ├── JWTAuthenticationFilter.java
│       ├── JwtAuthEntryPoint.java
│       ├── JWTGenerator.java
│       ├── SecurityConfig.java
│       └── SecurityConstants.java
└── DoctorApplication.java
```

### Frontend Architecture (Kotlin)

```
src/main/kotlin/ma/ensa/projet/
├── adapter/
├── api/
├── beans/
├── ui/
├── util/
├── AuthActivity.kt
├── DoctorDetailActivity.kt
├── IntroActivity.kt
├── ListDoctorActivity.kt
├── MainActivity.kt
├── RegisterActivity.kt
└── ResetPassActivity.kt
```
## 🛠️ Prerequisites

Before getting started, ensure you have the following installed:

### Development Tools
- **Android Studio**
- **JDK** (version 17 or later)
- **Android SDK** (API level 33 recommended)
- **Docker** 
- **Git**  
## 🛠️ Technical Components

### Spring Boot Components

#### Security Configuration
```java
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
```

### Kotlin Components
#### Core Dependencies
```gradle
dependencies {
    // Networking
    implementation "com.squareup.okhttp3:okhttp:4.9.0"
    implementation "com.squareup.retrofit2:retrofit:2.9.0"
    implementation "com.squareup.retrofit2:converter-gson:2.9.0"
    
    // Image Loading
    implementation "com.github.bumptech.glide:glide:4.12.0"
    annotationProcessor "com.github.bumptech.glide:compiler:4.12.0"
   
}
```

#### Retrofit Configuration
```kotlin
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:4000"
    private lateinit var tokenManager: TokenManager

    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: DoctorApiService by lazy {
        retrofit.create(DoctorApiService::class.java)
    }

    val patientApiService: PatientApiService by lazy {
        retrofit.create(PatientApiService::class.java)
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val appointmentApiService: AppointmentApiService by lazy {
        retrofit.create(AppointmentApiService::class.java)
    }
}
```

#### API Service Interface
```kotlin
interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}
```
## 🐳 Docker Configuration

### Full Docker Compose Configuration
```yaml
version: "3.8"

services:
  mysql-db:
    image: mysql:8.0
    container_name: mysql-doctor
    environment:
      - MYSQL_DATABASE=database
      - MYSQL_ROOT_PASSWORD=root
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 5

  backend:
    image: spring-boot-app 
    container_name: spring-boot-doctor
    depends_on:
      - mysql-db
    ports:
      - "8081:4000" 
    networks:
      - app-network
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/doctor_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root

  phpmyadmin:
    image: phpmyadmin/phpmyadmin
    container_name: phpmyadmin-doctor
    depends_on:
      - mysql-db
    ports:
      - "8082:80"
    networks:
      - app-network
    environment:
      PMA_HOST: mysql-db
      PMA_PORT: 3306

volumes:
  mysql_data:

networks:
  app-network:
    driver: bridge

```
## 🚀 Installation Guide

### Clone the Repository
```bash
# Clone the project
git clone https://github.com/CHOUAY15/EnsaHealth.git
```
### Launch with Docker
```bash
# Navigate to backend directory
cd Backend

# Build the image
docker-compose build

# Start services
docker-compose up -d

# Verify containers
docker ps
```
## 🔍 Testing and Quality (SonarQube)
To simplify the task, SonarQube can be set to run on port 9000 without requiring authentication. This is done by setting the environment variable as follows:

```yaml
SONAR_FORCEAUTHENTICATION=false
```

> **Warning**: This configuration is not recommended for production environments. It’s best to set up your own security configurations.

To adjust this setting, modify the environment variable in the following file:

- `src/main/docker/sonar.yml`
```
### Running Tests

# Navigate to backend directory
cd Backend

# Unit Tests
mvn test

# Integration Tests
mvn verify -P integration-test

# Generate JaCoCo Report
mvn clean test jacoco:report

# Start SonarQube
docker compose -f src/main/docker/sonar.yml up -d

# Run analysis
./mvnw clean verify sonar:sonar -Dsonar.login=admin -Dsonar.password=admin
```
## 🔐 Security

The application implements several security measures:
- JWT authentication
- Password encryption
- HTTPS communication
- Input validation





## 🎯 Conclusion

This application represents a modern solution for medical appointment management, combining mobile and backend development best practices. The use of technologies like Retrofit, Spring Boot, and Docker ensures a robust and easily deployable architecture.

## 👥 Contributors

- CHOUAY Walid ([GitHub](https://github.com/CHOUAY15))


---
<div align="center">
Made with ❤️ by CHOUAY Walid
</div>
