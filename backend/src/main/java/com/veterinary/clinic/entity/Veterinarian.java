package com.veterinary.clinic.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "veterinarians")
public class Veterinarian {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String specialization; // hirurg, dermatolog, opšta praksa...
    
    @Column(nullable = false, unique = true)
    private String email;
    
    private String phone;
    
    @OneToMany(mappedBy = "veterinarian", cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    
    // Konstruktori
    public Veterinarian() {}
    
    public Veterinarian(String name, String specialization, String email, String phone) {
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
    }
    
    // Getteri i Setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}