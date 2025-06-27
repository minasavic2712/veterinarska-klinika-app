package com.veterinary.clinic.controller;

import com.veterinary.clinic.entity.Appointment;
import com.veterinary.clinic.entity.Pet;
import com.veterinary.clinic.entity.Veterinarian;
import com.veterinary.clinic.repository.AppointmentRepository;
import com.veterinary.clinic.repository.PetRepository;
import com.veterinary.clinic.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    // GET /api/appointments - svi termini
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // GET /api/appointments/{id} - jedan termin
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        return appointment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/appointments/pet/{petId} - termini jednog ljubimca
    @GetMapping("/pet/{petId}")
    public List<Appointment> getAppointmentsByPet(@PathVariable Long petId) {
        return appointmentRepository.findByPetId(petId);
    }

    // GET /api/appointments/veterinarian/{vetId} - termini jednog veterinara
    @GetMapping("/veterinarian/{vetId}")
    public List<Appointment> getAppointmentsByVeterinarian(@PathVariable Long vetId) {
        return appointmentRepository.findByVeterinarianId(vetId);
    }

    // GET /api/appointments/status/{status} - termini po statusu
    @GetMapping("/status/{status}")
    public List<Appointment> getAppointmentsByStatus(@PathVariable String status) {
        try {
            Appointment.AppointmentStatus appointmentStatus = Appointment.AppointmentStatus.valueOf(status.toUpperCase());
            return appointmentRepository.findByStatus(appointmentStatus);
        } catch (IllegalArgumentException e) {
            return List.of(); // Vraća praznu listu za nevaljan status
        }
    }

    // GET /api/appointments/today - današnji termini
    @GetMapping("/today")
    public List<Appointment> getTodayAppointments() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return appointmentRepository.findByAppointmentDateTimeBetween(startOfDay, endOfDay);
    }

    // POST /api/appointments - zakaži novi termin
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> requestData) {
        try {
            // Ekstraktuj podatke iz request-a
            Long petId = Long.valueOf(requestData.get("petId").toString());
            Long veterinarianId = Long.valueOf(requestData.get("veterinarianId").toString());
            String appointmentDateTimeStr = requestData.get("appointmentDateTime").toString();
            String reason = (String) requestData.get("reason");
            
            // Validiraj da pet i veterinarian postoje
            Optional<Pet> pet = petRepository.findById(petId);
            Optional<Veterinarian> veterinarian = veterinarianRepository.findById(veterinarianId);
            
            if (pet.isEmpty() || veterinarian.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pet ili Veterinarian ne postoji"));
            }
            
            // Parsiraj datum i vreme
            LocalDateTime appointmentDateTime = LocalDateTime.parse(appointmentDateTimeStr);
            
            // Proveri da li veterinarian ima slobodan termin
            List<Appointment> conflictingAppointments = appointmentRepository
                .findByVeterinarianIdAndAppointmentDateTime(veterinarianId, appointmentDateTime);
            
            if (!conflictingAppointments.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Veterinarian nije dostupan u to vreme"));
            }
            
            // Kreiraj novi termin
            Appointment appointment = new Appointment();
            appointment.setPet(pet.get());
            appointment.setVeterinarian(veterinarian.get());
            appointment.setAppointmentDateTime(appointmentDateTime);
            appointment.setReason(reason);
            appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.ok(savedAppointment);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Greška pri kreiranju termina: " + e.getMessage()));
        }
    }

    // PUT /api/appointments/{id}/status - promeni status termina
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody Map<String, String> requestData) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            
            try {
                String statusStr = requestData.get("status");
                Appointment.AppointmentStatus newStatus = Appointment.AppointmentStatus.valueOf(statusStr.toUpperCase());
                appointment.setStatus(newStatus);
                
                Appointment updatedAppointment = appointmentRepository.save(appointment);
                return ResponseEntity.ok(updatedAppointment);
                
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Nevaljan status"));
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/appointments/{id}/cancel - otkaži termin
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        
        if (optionalAppointment.isPresent()) {
            Appointment appointment = optionalAppointment.get();
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            
            Appointment updatedAppointment = appointmentRepository.save(appointment);
            return ResponseEntity.ok(updatedAppointment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/appointments/{id} - obriši termin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}