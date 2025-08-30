package com.wipro.hms.doctor.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.wipro.hms.doctor.entity.Doctor;
import com.wipro.hms.doctor.repository.DoctorRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public Doctor createDoctor(Doctor doctor) {
        Doctor savedDoctor = doctorRepository.save(doctor);
        kafkaTemplate.send("doctor-events", savedDoctor.getId().toString(), savedDoctor);
        return savedDoctor;
    }

    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found with id: " + id));
    }

    public Page<Doctor> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        return doctorRepository.findByDepartment(department);
    }

    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = getDoctorById(id);

        doctor.setFirstName(doctorDetails.getFirstName());
        doctor.setLastName(doctorDetails.getLastName());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setPhone(doctorDetails.getPhone());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setQualifications(doctorDetails.getQualifications());
        doctor.setExperience(doctorDetails.getExperience());
        doctor.setDepartment(doctorDetails.getDepartment());
        doctor.setConsultationFee(doctorDetails.getConsultationFee());
        doctor.setAvailability(doctorDetails.getAvailability());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        kafkaTemplate.send("doctor-events", updatedDoctor.getId().toString(), updatedDoctor);

        return updatedDoctor;
    }

    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorById(id);
        doctorRepository.delete(doctor);
        kafkaTemplate.send("doctor-events", id.toString(), "DOCTOR_DELETED");
    }
}
