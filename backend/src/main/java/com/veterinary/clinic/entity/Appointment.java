package com.veterinary.clinic.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
    
    @ManyToOne
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private Veterinarian veterinarian;
    
    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;
    
    private String reason; // razlog dolaska
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL)
    private List<Treatment> treatments;
    
    // Enum za status
    public enum AppointmentStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }
    
    // Konstruktori
    public Appointment() {}
    
    public Appointment(Pet pet, Veterinarian veterinarian, LocalDateTime appointmentDateTime, String reason) {
        this.pet = pet;
        this.veterinarian = veterinarian;
        this.appointmentDateTime = appointmentDateTime;
        this.reason = reason;
    }
    
    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    
    public Veterinarian getVeterinarian() { return veterinarian; }
    public void setVeterinarian(Veterinarian veterinarian) { this.veterinarian = veterinarian; }
    
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    
    public List<Treatment> getTreatments() { return treatments; }
    public void setTreatments(List<Treatment> treatments) { this.treatments = treatments; }
}