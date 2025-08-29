package com.wipro.hms.appointment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.wipro.hms.appointment.Entity.Appointment;
import com.wipro.hms.appointment.repository.AppointmentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public Appointment createAppointment(Appointment appointment) {
        // Check for conflicting appointments
        if (hasAppointmentConflict(appointment.getDoctorId(), appointment.getAppointmentDate(), appointment.getTimeSlot())) {
            throw new ConflictException("Doctor already has an appointment at this time");
        }
        
        appointment.setCreatedAt(LocalDateTime.now());
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        kafkaTemplate.send("appointment-events", "APPOINTMENT_CREATED", savedAppointment);
        return savedAppointment;
    }
    
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }
    
    public Page<Appointment> getAppointmentsByPatientId(Long patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }
    
    public Page<Appointment> getAppointmentsByDoctorId(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }
    
    public List<Appointment> getAppointmentsByDateAndDoctor(LocalDate date, Long doctorId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return appointmentRepository.findByDoctorIdAndAppointmentDateBetween(doctorId, startOfDay, endOfDay);
    }
    
    public Appointment updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(status);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        Appointment updatedAppointment = appointmentRepository.save(appointment);
        kafkaTemplate.send("appointment-events", "APPOINTMENT_UPDATED", updatedAppointment);
        
        return updatedAppointment;
    }
    
    public void cancelAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());
        
        appointmentRepository.save(appointment);
        kafkaTemplate.send("appointment-events", "APPOINTMENT_CANCELLED", appointment);
    }
    
    private boolean hasAppointmentConflict(Long doctorId, LocalDateTime appointmentDate, String timeSlot) {
        LocalDateTime startDateTime = appointmentDate;
        List<Appointment> existingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndTimeSlot(doctorId, appointmentDate.toLocalDate(), timeSlot);
        
        return !existingAppointments.isEmpty();
    }
}