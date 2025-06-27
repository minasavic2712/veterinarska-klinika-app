package com.veterinary.clinic.controller;

import com.veterinary.clinic.entity.Pet;
import com.veterinary.clinic.entity.Owner;
import com.veterinary.clinic.repository.PetRepository;
import com.veterinary.clinic.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pets")
@CrossOrigin(origins = "*")
public class PetController {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    // GET /api/pets - svi ljubimci
    @GetMapping
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    // GET /api/pets/{id} - jedan ljubimac
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id) {
        Optional<Pet> pet = petRepository.findById(id);
        return pet.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/pets/owner/{ownerId} - ljubimci jednog vlasnika
    @GetMapping("/owner/{ownerId}")
    public List<Pet> getPetsByOwner(@PathVariable Long ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }

    // GET /api/pets/species/{species} - ljubimci po vrsti
    @GetMapping("/species/{species}")
    public List<Pet> getPetsBySpecies(@PathVariable String species) {
        return petRepository.findBySpecies(species);
    }

    // POST /api/pets - dodaj novog ljubimca
    @PostMapping
    public ResponseEntity<Pet> createPet(@RequestBody Pet pet) {
        // Proveri da li vlasnik postoji
        if (pet.getOwner() != null && pet.getOwner().getId() != null) {
            Optional<Owner> owner = ownerRepository.findById(pet.getOwner().getId());
            if (owner.isPresent()) {
                pet.setOwner(owner.get());
                Pet savedPet = petRepository.save(pet);
                return ResponseEntity.ok(savedPet);
            }
        }
        return ResponseEntity.badRequest().build();
    }

    // PUT /api/pets/{id} - ažuriraj ljubimca
    @PutMapping("/{id}")
    public ResponseEntity<Pet> updatePet(@PathVariable Long id, @RequestBody Pet petDetails) {
        Optional<Pet> optionalPet = petRepository.findById(id);
        
        if (optionalPet.isPresent()) {
            Pet pet = optionalPet.get();
            pet.setName(petDetails.getName());
            pet.setSpecies(petDetails.getSpecies());
            pet.setBreed(petDetails.getBreed());
            pet.setAge(petDetails.getAge());
            pet.setWeight(petDetails.getWeight());
            pet.setColor(petDetails.getColor());
            
            // Ažuriraj vlasnika ako je prosleđen
            if (petDetails.getOwner() != null && petDetails.getOwner().getId() != null) {
                Optional<Owner> owner = ownerRepository.findById(petDetails.getOwner().getId());
                if (owner.isPresent()) {
                    pet.setOwner(owner.get());
                }
            }
            
            Pet updatedPet = petRepository.save(pet);
            return ResponseEntity.ok(updatedPet);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/pets/{id} - obriši ljubimca
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        if (petRepository.existsById(id)) {
            petRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}