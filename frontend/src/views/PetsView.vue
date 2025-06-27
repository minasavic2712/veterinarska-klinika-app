<template>
  <div class="pets">
    <nav class="navbar">
      <div class="nav-brand">
        <h1>🏥 Veterinary Clinic</h1>
      </div>
      <div class="nav-links">
        <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link to="/owners" class="nav-link">Owners</router-link>
        <router-link to="/pets" class="nav-link">Pets</router-link>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>
    </nav>

    <div class="content">
      <div class="header">
        <h2>🐕 Pets Management</h2>
        <button @click="showAddForm = true" class="add-btn">+ Add New Pet</button>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-message">
        ⚠️ {{ error }}
        <button @click="error = null" class="close-error">×</button>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="loading-message">
        🔄 Loading...
      </div>

      <!-- Add/Edit Form -->
      <div v-if="showAddForm || editingPet" class="form-overlay" @click="closeForm">
        <div class="form-modal" @click.stop>
          <h3>{{ editingPet ? 'Edit Pet' : 'Add New Pet' }}</h3>
          
          <form @submit.prevent="savePet">
            <div class="form-row">
              <div class="form-group">
                <label for="name">Pet Name:</label>
                <input 
                  type="text" 
                  id="name" 
                  v-model="petForm.name" 
                  required
                  placeholder="e.g. Buddy"
                />
              </div>
              
              <div class="form-group">
                <label for="species">Species:</label>
                <select id="species" v-model="petForm.species" required>
                  <option value="">Select species</option>
                  <option value="Dog">Dog (Pas)</option>
                  <option value="Cat">Cat (Mačka)</option>
                  <option value="Bird">Bird (Ptica)</option>
                  <option value="Rabbit">Rabbit (Zec)</option>
                  <option value="Hamster">Hamster</option>
                  <option value="Other">Other</option>
                </select>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="breed">Breed:</label>
                <input 
                  type="text" 
                  id="breed" 
                  v-model="petForm.breed"
                  placeholder="e.g. Golden Retriever"
                />
              </div>
              
              <div class="form-group">
                <label for="age">Age (years):</label>
                <input 
                  type="number" 
                  id="age" 
                  v-model.number="petForm.age"
                  min="0"
                  max="30"
                  placeholder="e.g. 3"
                />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="weight">Weight (kg):</label>
                <input 
                  type="number" 
                  id="weight" 
                  v-model.number="petForm.weight"
                  step="0.1"
                  min="0"
                  placeholder="e.g. 25.5"
                />
              </div>
              
              <div class="form-group">
                <label for="color">Color:</label>
                <input 
                  type="text" 
                  id="color" 
                  v-model="petForm.color"
                  placeholder="e.g. Brown, Black, White"
                />
              </div>
            </div>
            
            <div class="form-group">
              <label for="owner">Owner:</label>
              <select id="owner" v-model="petForm.ownerId" required>
                <option value="">Select owner</option>
                <option v-for="owner in owners" :key="owner.id" :value="owner.id">
                  {{ owner.name }} - {{ owner.email }}
                </option>
              </select>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeForm" class="cancel-btn">Cancel</button>
              <button type="submit" class="save-btn" :disabled="isLoading">
                {{ editingPet ? 'Update' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Filter Options -->
      <div class="filters">
        <div class="filter-group">
          <label for="speciesFilter">Filter by Species:</label>
          <select id="speciesFilter" v-model="speciesFilter">
            <option value="">All Species</option>
            <option value="Dog">Dogs</option>
            <option value="Cat">Cats</option>
            <option value="Bird">Birds</option>
            <option value="Rabbit">Rabbits</option>
            <option value="Other">Other</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="ownerFilter">Filter by Owner:</label>
          <select id="ownerFilter" v-model="ownerFilter">
            <option value="">All Owners</option>
            <option v-for="owner in owners" :key="owner.id" :value="owner.id">
              {{ owner.name }}
            </option>
          </select>
        </div>
      </div>

      <!-- Pets Grid -->
      <div class="pets-grid">
        <div v-for="pet in filteredPets" :key="pet.id" class="pet-card">
          <div class="pet-header">
            <h4>{{ pet.name }}</h4>
            <span class="species-badge" :class="pet.species.toLowerCase()">
              {{ getSpeciesEmoji(pet.species) }} {{ pet.species }}
            </span>
          </div>
          
          <div class="pet-info">
            <div class="info-row">
              <span class="label">Breed:</span>
              <span>{{ pet.breed || 'Not specified' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Age:</span>
              <span>{{ pet.age ? pet.age + ' years' : 'Unknown' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Weight:</span>
              <span>{{ pet.weight ? pet.weight + ' kg' : 'Not recorded' }}</span>
            </div>
            <div class="info-row">
              <span class="label">Color:</span>
              <span>{{ pet.color || 'Not specified' }}</span>
            </div>
            <div class="info-row owner-info">
              <span class="label">Owner:</span>
              <span>{{ getOwnerName(pet.owner?.id || pet.ownerId) }}</span>
            </div>
          </div>
          
          <div class="pet-actions">
            <button @click="editPet(pet)" class="edit-btn">✏️ Edit</button>
            <button @click="deletePet(pet.id)" class="delete-btn">🗑️ Delete</button>
            <button @click="viewAppointments(pet)" class="appointments-btn">📅 Appointments</button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!isLoading && filteredPets.length === 0" class="empty-state">
        <div class="empty-icon">🐕</div>
        <h3>{{ pets.length === 0 ? 'No pets yet' : 'No pets match your filters' }}</h3>
        <p>{{ pets.length === 0 ? 'Start by adding your first pet!' : 'Try adjusting your filter settings.' }}</p>
        <button v-if="pets.length === 0" @click="showAddForm = true" class="add-btn">+ Add First Pet</button>
      </div>
    </div>
  </div>
</template>

<script>
import apiService from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'PetsView',
  data() {
    return {
      pets: [],
      owners: [],
      showAddForm: false,
      editingPet: null,
      petForm: {
        name: '',
        species: '',
        breed: '',
        age: null,
        weight: null,
        color: '',
        ownerId: null
      },
      speciesFilter: '',
      ownerFilter: '',
      isLoading: false,
      error: null
    }
  },
  computed: {
    filteredPets() {
      let filtered = this.pets;
      
      if (this.speciesFilter) {
        filtered = filtered.filter(pet => pet.species === this.speciesFilter);
      }
      
      if (this.ownerFilter) {
        filtered = filtered.filter(pet => {
          const ownerId = pet.owner?.id || pet.ownerId;
          return ownerId === parseInt(this.ownerFilter);
        });
      }
      
      return filtered;
    }
  },
  async mounted() {
    await this.loadOwners();
    await this.loadPets();
  },
  methods: {
    // Učitaj sve pets iz API-ja
    async loadPets() {
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await apiService.getPets();
        this.pets = response;
        console.log('Loaded pets:', this.pets);
      } catch (error) {
        this.error = 'Failed to load pets: ' + error.message;
        console.error('Error loading pets:', error);
      } finally {
        this.isLoading = false;
      }
    },

    // Učitaj sve owners za dropdown
    async loadOwners() {
      try {
        const response = await apiService.getOwners();
        this.owners = response;
        console.log('Loaded owners for dropdown:', this.owners);
      } catch (error) {
        console.error('Error loading owners:', error);
      }
    },

    showAddPet() {
      this.showAddForm = true;
      this.resetForm();
    },
    
    editPet(pet) {
      this.editingPet = pet;
      this.petForm = {
        name: pet.name,
        species: pet.species,
        breed: pet.breed || '',
        age: pet.age,
        weight: pet.weight,
        color: pet.color || '',
        ownerId: pet.owner?.id || pet.ownerId
      };
    },
    
    closeForm() {
      this.showAddForm = false;
      this.editingPet = null;
      this.resetForm();
    },
    
    resetForm() {
      this.petForm = {
        name: '',
        species: '',
        breed: '',
        age: null,
        weight: null,
        color: '',
        ownerId: null
      };
    },
    
    async savePet() {
      this.isLoading = true;
      this.error = null;
      
      try {
        // Pripremi podatke za backend - treba owner objekat
        const petData = {
          ...this.petForm,
          owner: {
            id: this.petForm.ownerId
          }
        };
        
        if (this.editingPet) {
          // Update postojećeg pet-a
          const response = await apiService.updatePet(this.editingPet.id, petData);
          
          // Ažuriraj u listi
          const index = this.pets.findIndex(p => p.id === this.editingPet.id);
          if (index !== -1) {
            this.pets[index] = response;
          }
          
          console.log('Updated pet:', response);
        } else {
          // Dodaj novi pet
          const response = await apiService.createPet(petData);
          this.pets.push(response);
          console.log('Created new pet:', response);
        }
        
        this.closeForm();
      } catch (error) {
        this.error = 'Failed to save pet: ' + error.message;
        console.error('Error saving pet:', error);
      } finally {
        this.isLoading = false;
      }
    },
    
    async deletePet(petId) {
      if (confirm('Are you sure you want to delete this pet?')) {
        this.isLoading = true;
        this.error = null;
        
        try {
          await apiService.deletePet(petId);
          
          // Ukloni iz liste
          this.pets = this.pets.filter(p => p.id !== petId);
          console.log('Deleted pet:', petId);
        } catch (error) {
          this.error = 'Failed to delete pet: ' + error.message;
          console.error('Error deleting pet:', error);
        } finally {
          this.isLoading = false;
        }
      }
    },
    
    viewAppointments(pet) {
      console.log('View appointments for:', pet.name);
      // Ovde ćemo kasnije dodati navigaciju do appointments stranice
    },
    
    getOwnerName(ownerId) {
      const owner = this.owners.find(o => o.id === ownerId);
      return owner ? owner.name : 'Unknown Owner';
    },
    
    getSpeciesEmoji(species) {
      const emojis = {
        'Dog': '🐕',
        'Cat': '🐱',
        'Bird': '🐦',
        'Rabbit': '🐰',
        'Hamster': '🐹',
        'Other': '🐾'
      };
      return emojis[species] || '🐾';
    },
    
    logout() {
      const authStore = useAuthStore();
      authStore.logout();
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
.pets {
  min-height: 100vh;
  background-color: #f8fafc;
}

.navbar {
  background: white;
  padding: 1rem 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-brand h1 {
  color: #667eea;
  font-size: 1.5rem;
  margin: 0;
}

.nav-links {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-link:hover, .nav-link.router-link-active {
  color: #667eea;
}

.logout-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #dc2626;
}

.content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h2 {
  color: #1e293b;
  font-size: 2rem;
  margin: 0;
}

.add-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.add-btn:hover {
  background: #059669;
  transform: translateY(-2px);
}

.error-message {
  background: #fee2e2;
  color: #dc2626;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #fecaca;
}

.close-error {
  background: none;
  border: none;
  color: #dc2626;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0;
  margin-left: 1rem;
}

.loading-message {
  background: #dbeafe;
  color: #1d4ed8;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
  border: 1px solid #bfdbfe;
}

.form-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.form-modal {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.form-modal h3 {
  color: #1e293b;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #374151;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #667eea;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
}

.cancel-btn {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.cancel-btn:hover {
  background: #4b5563;
}

.save-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.3s;
}

.save-btn:hover:not(:disabled) {
  background: #5a67d8;
}

.save-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.filters {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  display: flex;
  gap: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #374151;
}

.filter-group select {
  padding: 0.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  min-width: 150px;
}

.pets-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.pet-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.pet-card:hover {
  transform: translateY(-5px);
}

.pet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.pet-header h4 {
  color: #1e293b;
  font-size: 1.25rem;
  margin: 0;
}

.species-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.species-badge.dog {
  background: #fef3c7;
  color: #d97706;
}

.species-badge.cat {
  background: #f3e8ff;
  color: #8b5cf6;
}

.species-badge.bird {
  background: #dbeafe;
  color: #3b82f6;
}

.species-badge.rabbit {
  background: #d1fae5;
  color: #10b981;
}

.species-badge.other {
  background: #f1f5f9;
  color: #64748b;
}

.pet-info {
  margin-bottom: 1.5rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.info-row .label {
  font-weight: 500;
  color: #64748b;
}

.owner-info {
  padding-top: 0.5rem;
  border-top: 1px solid #e5e7eb;
  color: #10b981;
  font-weight: 500;
}

.pet-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.edit-btn {
  background: #f59e0b;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
}

.edit-btn:hover {
  background: #d97706;
}

.delete-btn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
}

.delete-btn:hover {
  background: #dc2626;
}

.appointments-btn {
  background: #8b5cf6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
}

.appointments-btn:hover {
  background: #7c3aed;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #64748b;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #374151;
  margin-bottom: 0.5rem;
}

.empty-state p {
  margin-bottom: 2rem;
}

@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    gap: 1rem;
  }

  .nav-links {
    gap: 1rem;
  }

  .header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .filters {
    flex-direction: column;
    gap: 1rem;
  }

  .pets-grid {
    grid-template-columns: 1fr;
  }

  .pet-actions {
    justify-content: center;
  }
}
</style>