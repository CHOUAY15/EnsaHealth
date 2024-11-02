package ma.ensa.projet.doctor.api.service.impl;

import ma.ensa.projet.doctor.api.dto.RendezVousDTO;
import ma.ensa.projet.doctor.api.entity.Doctor;
import ma.ensa.projet.doctor.api.entity.Patient;
import ma.ensa.projet.doctor.api.entity.RendezVous;
import ma.ensa.projet.doctor.api.entity.RendezVousId;
import ma.ensa.projet.doctor.api.repository.DoctorRepo;
import ma.ensa.projet.doctor.api.repository.PatientRepo;
import ma.ensa.projet.doctor.api.repository.RendezVoRepos;
import ma.ensa.projet.doctor.api.service.interfaces.RendezVoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RendezVoServmpl implements RendezVoService {

    @Autowired
    private RendezVoRepos rendezVousRepository;

    @Autowired
    private PatientRepo patientRepository;

    @Autowired
    private DoctorRepo doctorRepository;

    @Override
    public RendezVousDTO createRendezVous(RendezVousDTO rendezVousDTO) {
        Patient patient = patientRepository.findById(rendezVousDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Doctor doctor = doctorRepository.findById(rendezVousDTO.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        boolean rendezVousExists = rendezVousRepository.existsByDoctorIdAndDate(
                rendezVousDTO.getDoctorId(),
                rendezVousDTO.getDate());

        if (rendezVousExists) {
            throw new IllegalArgumentException("Cette date de rendez-vous est déjà réservée.");
        }

        RendezVous rendezVous = new RendezVous();
        rendezVous.setDoctorId(doctor.getId()); 
        rendezVous.setDoctor(doctor);
        rendezVous.setPatient(patient);
        rendezVous.setDate(rendezVousDTO.getDate());
        rendezVous.setStatus(rendezVousDTO.isStatut());

        return convertToDto(rendezVousRepository.save(rendezVous));
    }

    @Override
    public void deleteRendezVous(RendezVousId rendezVousId) { 
        if (!rendezVousRepository.existsById(rendezVousId)) {
            throw new EntityNotFoundException("RendezVous not found");
        }
        rendezVousRepository.deleteById(rendezVousId);
    }

    @Transactional
    @Override
    public RendezVousDTO updateRendezVous(Integer doctorId, String date, RendezVousDTO rendezVousDTO) {
        RendezVous existingRendezVous = rendezVousRepository.findById(new RendezVousId(doctorId, date))
                .orElseThrow(() -> new EntityNotFoundException("RendezVous not found"));
    
        if (rendezVousDTO.isStatut() != existingRendezVous.isStatus()) {
            existingRendezVous.setStatus(rendezVousDTO.isStatut());
            return convertToDto(rendezVousRepository.save(existingRendezVous));
        }
    
        if (!date.equals(rendezVousDTO.getDate())) {
            boolean exists = rendezVousRepository.existsByDoctorIdAndDate(doctorId, rendezVousDTO.getDate());
            if (exists) {
                throw new IllegalArgumentException("Un rendez-vous existe déjà à cette date");
            }
    
            RendezVous newRendezVous = new RendezVous();
            newRendezVous.setDoctorId(doctorId);
            newRendezVous.setDate(rendezVousDTO.getDate());
            newRendezVous.setPatient(existingRendezVous.getPatient());
            newRendezVous.setStatus(existingRendezVous.isStatus());
            newRendezVous.setDoctor(existingRendezVous.getDoctor());
    
            rendezVousRepository.delete(existingRendezVous);
            
            return convertToDto(rendezVousRepository.save(newRendezVous));
        }
    
        return convertToDto(existingRendezVous);
    }

    @Override
    public List<RendezVousDTO> getAllRendezVous() {
        return rendezVousRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RendezVousDTO> getRendezVousByPatientId(Integer patId) {
        return rendezVousRepository.findByPatientId(patId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RendezVousDTO> getRendezVousByDoctorId(Integer docId) {
        return rendezVousRepository.findByDoctorId(docId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RendezVousDTO convertToDto(RendezVous rendezVous) {
        RendezVousDTO dto = new RendezVousDTO();
        dto.setDoctorName(rendezVous.getDoctor().getLastName() + " " + rendezVous.getDoctor().getFirstName());
        dto.setPtienName(rendezVous.getPatient().getLastName() + " " + rendezVous.getPatient().getFirstName());
        dto.setDate(rendezVous.getDate());
        dto.setPatientId(rendezVous.getPatient().getId());
        dto.setDoctorId(rendezVous.getDoctor().getId());
        dto.setStatut(rendezVous.isStatus());
        return dto;
    }
}
