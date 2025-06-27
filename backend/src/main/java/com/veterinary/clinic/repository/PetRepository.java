package com.veterinary.clinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veterinary.clinic.entity.Pet;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    // Pronađi sve ljubimce jednog vlasnika
    List<Pet> findByOwnerId(Long ownerId);
    
    // Pronađi po vrsti
    List<Pet> findBySpecies(String species);
}