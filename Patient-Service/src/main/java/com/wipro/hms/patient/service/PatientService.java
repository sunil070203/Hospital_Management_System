package com.wipro.hms.patient.service;

import java.time.LocalDateTime;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.wipro.hms.patient.Entity.Patient;
import com.wipro.hms.patient.Repository.PatientRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // Create patient
    public Patient createPatient(Patient patient) {
        Patient savedPatient = patientRepository.save(patient);
        kafkaTemplate.send("patient-events", "PATIENT_CREATED", savedPatient);
        return savedPatient;
    }

    // Get patient by ID
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    // Get all patients
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    // Update patient
    public Patient updatePatient(Long id, Patient patientDetails) {
        Patient patient = getPatientById(id);

        patient.setFirstName(patientDetails.getFirstName());
        patient.setLastName(patientDetails.getLastName());
        patient.setEmail(patientDetails.getEmail());
        patient.setPhone(patientDetails.getPhone());
        patient.setDateOfBirth(patientDetails.getDateOfBirth());
        patient.setAddress(patientDetails.getAddress());
        patient.setEmergencyContact(patientDetails.getEmergencyContact());
        patient.setBloodType(patientDetails.getBloodType());
        patient.setAllergies(patientDetails.getAllergies());
        patient.setUpdatedAt(LocalDateTime.now());

        Patient updatedPatient = patientRepository.save(patient);
        kafkaTemplate.send("patient-events", "PATIENT_UPDATED", updatedPatient);

        return updatedPatient;
    }

    // Delete patient
    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
        kafkaTemplate.send("patient-events", "PATIENT_DELETED", id);
    }
}
