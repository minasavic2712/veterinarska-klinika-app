package com.veterinary.clinic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinary.clinic.entity.Veterinarian;

@Repository
public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
    
    // Pronađi po email-u
    Optional<Veterinarian> findByEmail(String email);
    
    // Proveri da li email postoji
    boolean existsByEmail(String email);
    
    // Pronađi po specijalizaciji
    List<Veterinarian> findBySpecialization(String specialization);
}