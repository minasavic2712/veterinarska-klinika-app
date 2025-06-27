<template>
  <div class="owners">
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
        <h2>👥 Pet Owners Management</h2>
        <button @click="showAddForm = true" class="add-btn">+ Add New Owner</button>
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
      <div v-if="showAddForm || editingOwner" class="form-overlay" @click="closeForm">
        <div class="form-modal" @click.stop>
          <h3>{{ editingOwner ? 'Edit Owner' : 'Add New Owner' }}</h3>
          
          <form @submit.prevent="saveOwner">
            <div class="form-group">
              <label for="name">Full Name:</label>
              <input 
                type="text" 
                id="name" 
                v-model="ownerForm.name" 
                required
                placeholder="e.g. John Smith"
              />
            </div>
            
            <div class="form-group">
              <label for="email">Email:</label>
              <input 
                type="email" 
                id="email" 
                v-model="ownerForm.email" 
                required
                placeholder="e.g. john.smith@email.com"
              />
            </div>
            
            <div class="form-group">
              <label for="phone">Phone:</label>
              <input 
                type="tel" 
                id="phone" 
                v-model="ownerForm.phone"
                placeholder="e.g. +381 60 123 4567"
              />
            </div>
            
            <div class="form-group">
              <label for="address">Address:</label>
              <textarea 
                id="address" 
                v-model="ownerForm.address"
                placeholder="e.g. Knez Mihailova 15, Belgrade"
                rows="3"
              ></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeForm" class="cancel-btn">Cancel</button>
              <button type="submit" class="save-btn" :disabled="isLoading">
                {{ editingOwner ? 'Update' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Owners List -->
      <div class="owners-grid">
        <div v-for="owner in owners" :key="owner.id" class="owner-card">
          <div class="owner-info">
            <h4>{{ owner.name }}</h4>
            <p class="email">📧 {{ owner.email }}</p>
            <p v-if="owner.phone" class="phone">📞 {{ owner.phone }}</p>
            <p v-if="owner.address" class="address">📍 {{ owner.address }}</p>
            <p class="pets-count">🐕 {{ owner.pets ? owner.pets.length : 0 }} pets</p>
          </div>
          
          <div class="owner-actions">
            <button @click="editOwner(owner)" class="edit-btn">✏️ Edit</button>
            <button @click="deleteOwner(owner.id)" class="delete-btn">🗑️ Delete</button>
            <button @click="viewPets(owner)" class="pets-btn">🐕 View Pets</button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!isLoading && owners.length === 0" class="empty-state">
        <div class="empty-icon">👥</div>
        <h3>No owners yet</h3>
        <p>Start by adding your first pet owner!</p>
        <button @click="showAddForm = true" class="add-btn">+ Add First Owner</button>
      </div>
    </div>
  </div>
</template>

<script>
import apiService from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'OwnersView',
  data() {
    return {
      owners: [],
      showAddForm: false,
      editingOwner: null,
      ownerForm: {
        name: '',
        email: '',
        phone: '',
        address: ''
      },
      isLoading: false,
      error: null
    }
  },
  async mounted() {
    await this.loadOwners()
  },
  methods: {
    // Učitaj sve vlasnike iz API-ja
    async loadOwners() {
      this.isLoading = true
      this.error = null
      
      try {
        const response = await apiService.getOwners()
        this.owners = response
        console.log('Loaded owners:', this.owners)
      } catch (error) {
        this.error = 'Failed to load owners: ' + error.message
        console.error('Error loading owners:', error)
      } finally {
        this.isLoading = false
      }
    },

    showAddOwner() {
      this.showAddForm = true
      this.resetForm()
    },
    
    editOwner(owner) {
      this.editingOwner = owner
      this.ownerForm = {
        name: owner.name,
        email: owner.email,
        phone: owner.phone || '',
        address: owner.address || ''
      }
    },
    
    closeForm() {
      this.showAddForm = false
      this.editingOwner = null
      this.resetForm()
    },
    
    resetForm() {
      this.ownerForm = {
        name: '',
        email: '',
        phone: '',
        address: ''
      }
    },
    
    async saveOwner() {
      this.isLoading = true
      this.error = null
      
      try {
        if (this.editingOwner) {
          // Update postojećeg vlasnika
          const response = await apiService.updateOwner(this.editingOwner.id, this.ownerForm)
          
          // Ažuriraj u listi
          const index = this.owners.findIndex(o => o.id === this.editingOwner.id)
          if (index !== -1) {
            this.owners[index] = response
          }
          
          console.log('Updated owner:', response)
        } else {
          // Dodaj novog vlasnika
          const response = await apiService.createOwner(this.ownerForm)
          this.owners.push(response)
          console.log('Created new owner:', response)
        }
        
        this.closeForm()
      } catch (error) {
        this.error = 'Failed to save owner: ' + error.message
        console.error('Error saving owner:', error)
      } finally {
        this.isLoading = false
      }
    },
    
    async deleteOwner(ownerId) {
      if (confirm('Are you sure you want to delete this owner?')) {
        this.isLoading = true
        this.error = null
        
        try {
          await apiService.deleteOwner(ownerId)
          
          // Ukloni iz liste
          this.owners = this.owners.filter(o => o.id !== ownerId)
          console.log('Deleted owner:', ownerId)
        } catch (error) {
          this.error = 'Failed to delete owner: ' + error.message
          console.error('Error deleting owner:', error)
        } finally {
          this.isLoading = false
        }
      }
    },
    
    viewPets(owner) {
      console.log('View pets for:', owner.name)
      // Navigiraj do pets stranice sa filterom
      this.$router.push(`/pets?owner=${owner.id}`)
    },
    
    logout() {
      // Pozovi logout iz auth store
      const authStore = useAuthStore()
      authStore.logout()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.owners {
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
  max-width: 1200px;
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
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.form-modal h3 {
  color: #1e293b;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
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
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
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

.owners-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.owner-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.owner-card:hover {
  transform: translateY(-5px);
}

.owner-info h4 {
  color: #1e293b;
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

.owner-info p {
  color: #64748b;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.pets-count {
  color: #10b981 !important;
  font-weight: 600;
}

.owner-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1.5rem;
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

.pets-btn {
  background: #8b5cf6;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
}

.pets-btn:hover {
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

  .owners-grid {
    grid-template-columns: 1fr;
  }

  .owner-actions {
    justify-content: center;
  }
}
</style>