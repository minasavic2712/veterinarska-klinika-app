package com.veterinary.clinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinary.clinic.entity.Owner;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    
    // Dodatne metode za pretragu
    Optional<Owner> findByEmail(String email);
    boolean existsByEmail(String email);
}