package com.veterinary.clinic.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veterinary.clinic.entity.Owner;
import com.veterinary.clinic.repository.OwnerRepository;

@RestController
@RequestMapping(value = "/api/owners", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class OwnerController {

    @Autowired
    private OwnerRepository ownerRepository;

    // GET /api/owners - svi vlasnici
    @GetMapping
    public List<Owner> getAllOwners() {
        System.out.println("Getting all owners...");
        List<Owner> owners = ownerRepository.findAll();
        System.out.println("Found " + owners.size() + " owners");
        return owners;
    }

    // GET /api/owners/{id} - jedan vlasnik
    @GetMapping("/{id}")
    public ResponseEntity<Owner> getOwnerById(@PathVariable Long id) {
        System.out.println("Getting owner with ID: " + id);
        Optional<Owner> owner = ownerRepository.findById(id);
        if (owner.isPresent()) {
            return ResponseEntity.ok(owner.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/owners - dodaj novog vlasnika
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Owner> createOwner(@RequestBody Owner owner) {
        try {
            System.out.println("Creating new owner:");
            System.out.println("Name: " + owner.getName());
            System.out.println("Email: " + owner.getEmail());
            System.out.println("Phone: " + owner.getPhone());
            System.out.println("Address: " + owner.getAddress());
            
            // Proveri da li email već postoji
            if (ownerRepository.existsByEmail(owner.getEmail())) {
                System.out.println("Email already exists: " + owner.getEmail());
                return ResponseEntity.badRequest().build();
            }
            
            Owner savedOwner = ownerRepository.save(owner);
            System.out.println("Successfully saved owner with ID: " + savedOwner.getId());
            
            return ResponseEntity.ok(savedOwner);
            
        } catch (Exception e) {
            System.err.println("Error creating owner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /api/owners/{id} - ažuriraj vlasnika
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Owner> updateOwner(@PathVariable Long id, @RequestBody Owner ownerDetails) {
        try {
            System.out.println("Updating owner with ID: " + id);
            Optional<Owner> optionalOwner = ownerRepository.findById(id);
            
            if (optionalOwner.isPresent()) {
                Owner owner = optionalOwner.get();
                
                // Proveri da li novi email već postoji (ali ne kod istog korisnika)
                if (!owner.getEmail().equals(ownerDetails.getEmail()) && 
                    ownerRepository.existsByEmail(ownerDetails.getEmail())) {
                    System.out.println("Email already exists: " + ownerDetails.getEmail());
                    return ResponseEntity.badRequest().build();
                }
                
                owner.setName(ownerDetails.getName());
                owner.setEmail(ownerDetails.getEmail());
                owner.setPhone(ownerDetails.getPhone());
                owner.setAddress(ownerDetails.getAddress());
                
                Owner updatedOwner = ownerRepository.save(owner);
                System.out.println("Successfully updated owner");
                return ResponseEntity.ok(updatedOwner);
            } else {
                System.out.println("Owner not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error updating owner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/owners/{id} - obriši vlasnika
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOwner(@PathVariable Long id) {
        try {
            System.out.println("Deleting owner with ID: " + id);
            if (ownerRepository.existsById(id)) {
                ownerRepository.deleteById(id);
                System.out.println("Successfully deleted owner");
                return ResponseEntity.ok().build();
            } else {
                System.out.println("Owner not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error deleting owner: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}