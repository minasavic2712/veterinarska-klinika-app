package com.veterinary.clinic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinary.clinic.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Termini jednog ljubimca
    List<Appointment> findByPetId(Long petId);
    
    // Termini jednog veterinara
    List<Appointment> findByVeterinarianId(Long veterinarianId);
    
    // Termini po statusu
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    
    // Termini u određenom vremenskom opsegu
    List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    // Provera konflikta termina za veterinara
    List<Appointment> findByVeterinarianIdAndAppointmentDateTime(Long veterinarianId, LocalDateTime appointmentDateTime);
    
    // Termini veterinara za određeni dan
    List<Appointment> findByVeterinarianIdAndAppointmentDateTimeBetween(Long veterinarianId, LocalDateTime start, LocalDateTime end);
}