<template>
  <div class="treatments">
    <nav class="navbar">
      <div class="nav-brand">
        <h1>🏥 Veterinary Clinic</h1>
      </div>
      <div class="nav-links">
        <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link to="/owners" class="nav-link">Owners</router-link>
        <router-link to="/pets" class="nav-link">Pets</router-link>
        <router-link to="/appointments" class="nav-link">Appointments</router-link>
        <router-link to="/treatments" class="nav-link">Treatments</router-link>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>
    </nav>

    <div class="content">
      <div class="header">
        <h2>⚕️ Medical Treatments</h2>
        <button @click="showAddForm = true" class="add-btn">+ Add Treatment</button>
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

      <!-- Filters -->
      <div class="filters">
        <div class="filter-group">
          <label for="petFilter">Filter by Pet:</label>
          <select id="petFilter" v-model="petFilter">
            <option value="">All Pets</option>
            <option v-for="pet in pets" :key="pet.id" :value="pet.id">
              {{ pet.name }} ({{ pet.species }})
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="diagnosisFilter">Filter by Diagnosis:</label>
          <input 
            type="text" 
            id="diagnosisFilter" 
            v-model="diagnosisFilter" 
            placeholder="Search diagnosis..."
          />
        </div>
      </div>

      <!-- Add/Edit Form -->
      <div v-if="showAddForm || editingTreatment" class="form-overlay" @click="closeForm">
        <div class="form-modal" @click.stop>
          <h3>{{ editingTreatment ? 'Edit Treatment' : 'Add New Treatment' }}</h3>
          
          <form @submit.prevent="saveTreatment">
            <div class="form-group">
              <label for="appointment">Related Appointment:</label>
              <select id="appointment" v-model="treatmentForm.appointmentId" required>
                <option value="">Select appointment</option>
                <option v-for="appointment in appointments" :key="appointment.id" :value="appointment.id">
                  {{ getPetName(appointment.pet?.id) }} - {{ formatDate(appointment.appointmentDateTime) }}
                </option>
              </select>
            </div>
            
            <div class="form-group">
              <label for="diagnosis">Diagnosis:</label>
              <input 
                type="text" 
                id="diagnosis" 
                v-model="treatmentForm.diagnosis" 
                required
                placeholder="e.g. Dental disease, Ear infection..."
              />
            </div>
            
            <div class="form-group">
              <label for="treatment">Treatment:</label>
              <textarea 
                id="treatment" 
                v-model="treatmentForm.treatment"
                required
                placeholder="Describe the treatment performed..."
                rows="4"
              ></textarea>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="cost">Cost (€):</label>
                <input 
                  type="number" 
                  id="cost" 
                  v-model.number="treatmentForm.cost"
                  step="0.01"
                  min="0"
                  placeholder="e.g. 45.50"
                />
              </div>
            </div>
            
            <div class="form-group">
              <label for="notes">Additional Notes:</label>
              <textarea 
                id="notes" 
                v-model="treatmentForm.notes"
                placeholder="Any additional observations or recommendations..."
                rows="3"
              ></textarea>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeForm" class="cancel-btn">Cancel</button>
              <button type="submit" class="save-btn" :disabled="isLoading">
                {{ editingTreatment ? 'Update' : 'Save' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Treatments List -->
      <div class="treatments-list">
        <div v-for="treatment in filteredTreatments" :key="treatment.id" class="treatment-card">
          <div class="treatment-header">
            <div class="patient-info">
              <h4>{{ getPetName(treatment.appointment?.pet?.id) }}</h4>
              <span class="appointment-date">📅 {{ formatDate(treatment.appointment?.appointmentDateTime) }}</span>
            </div>
            <div class="cost-badge" v-if="treatment.cost">
              💰 €{{ treatment.cost.toFixed(2) }}
            </div>
          </div>
          
          <div class="treatment-content">
            <div class="diagnosis-section">
              <h5>🔬 Diagnosis</h5>
              <p>{{ treatment.diagnosis }}</p>
            </div>
            
            <div class="treatment-section">
              <h5>⚕️ Treatment Performed</h5>
              <p>{{ treatment.treatment }}</p>
            </div>
            
            <div class="notes-section" v-if="treatment.notes">
              <h5>📝 Additional Notes</h5>
              <p>{{ treatment.notes }}</p>
            </div>
            
            <div class="metadata">
              <span class="vet-info">👨‍⚕️ {{ getVeterinarianName(treatment.appointment?.veterinarian?.id) }}</span>
              <span class="owner-info">👤 {{ getPetOwner(treatment.appointment?.pet?.id) }}</span>
              <span class="created-date">🕐 {{ formatDate(treatment.createdAt) }}</span>
            </div>
          </div>
          
          <div class="treatment-actions">
            <button @click="editTreatment(treatment)" class="edit-btn">✏️ Edit</button>
            <button @click="deleteTreatment(treatment.id)" class="delete-btn">🗑️ Delete</button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!isLoading && filteredTreatments.length === 0" class="empty-state">
        <div class="empty-icon">⚕️</div>
        <h3>{{ treatments.length === 0 ? 'No treatments recorded' : 'No treatments match your filters' }}</h3>
        <p>{{ treatments.length === 0 ? 'Start recording medical treatments!' : 'Try adjusting your filter settings.' }}</p>
        <button v-if="treatments.length === 0" @click="showAddForm = true" class="add-btn">+ Add First Treatment</button>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'TreatmentsView',
  data() {
    return {
      treatments: [
        {
          id: 1,
          appointment: {
            id: 1,
            pet: { id: 1, name: 'Rex', species: 'Dog' },
            veterinarian: { id: 1, name: 'Dr. Marić' },
            appointmentDateTime: '2025-06-25T14:00:00'
          },
          diagnosis: 'Dental disease and tartar buildup',
          treatment: 'Professional dental cleaning and scaling. Removed infected tooth. Applied fluoride treatment. Prescribed antibiotics for 7 days.',
          cost: 125.50,
          notes: 'Schedule follow-up in 2 weeks. Recommend dental chews for prevention.',
          createdAt: '2025-06-25T15:30:00'
        },
        {
          id: 2,
          appointment: {
            id: 2,
            pet: { id: 2, name: 'Maca', species: 'Cat' },
            veterinarian: { id: 1, name: 'Dr. Marić' },
            appointmentDateTime: '2025-06-24T10:30:00'
          },
          diagnosis: 'Ear infection (Otitis externa)',
          treatment: 'Cleaned ears thoroughly. Applied topical antibiotic medication. Prescribed ear drops for 10 days.',
          cost: 65.00,
          notes: 'Keep ears dry. Return if symptoms persist after treatment.',
          createdAt: '2025-06-24T11:00:00'
        },
        {
          id: 3,
          appointment: {
            id: 3,
            pet: { id: 3, name: 'Pera', species: 'Bird' },
            veterinarian: { id: 2, name: 'Dr. Popović' },
            appointmentDateTime: '2025-06-23T16:15:00'
          },
          diagnosis: 'Vitamin A deficiency and respiratory infection',
          treatment: 'Administered vitamin A injection. Prescribed antibiotic course. Dietary recommendations provided.',
          cost: 85.00,
          notes: 'Increase fresh vegetables in diet. Monitor breathing closely.',
          createdAt: '2025-06-23T16:45:00'
        },
        {
          id: 4,
          appointment: {
            id: 4,
            pet: { id: 1, name: 'Rex', species: 'Dog' },
            veterinarian: { id: 1, name: 'Dr. Marić' },
            appointmentDateTime: '2025-06-20T09:00:00'
          },
          diagnosis: 'Annual vaccination and health check',
          treatment: 'Administered DHPP and rabies vaccines. Complete physical examination. Blood work normal.',
          cost: 95.00,
          notes: 'All vitals normal. Next vaccination due in 12 months.',
          createdAt: '2025-06-20T09:30:00'
        },
        {
          id: 5,
          appointment: {
            id: 5,
            pet: { id: 4, name: 'Buddy', species: 'Dog' },
            veterinarian: { id: 2, name: 'Dr. Popović' },
            appointmentDateTime: '2025-06-18T11:00:00'
          },
          diagnosis: 'Skin allergies and dermatitis',
          treatment: 'Allergy testing performed. Prescribed antihistamines and topical cream. Dietary changes recommended.',
          cost: 150.00,
          notes: 'Avoid chicken-based foods. Use hypoallergenic shampoo. Follow-up in 3 weeks.',
          createdAt: '2025-06-18T12:15:00'
        }
      ],
      appointments: [
        { id: 1, pet: { id: 1, name: 'Rex' }, appointmentDateTime: '2025-06-25T14:00:00', veterinarian: { id: 1, name: 'Dr. Marić' } },
        { id: 2, pet: { id: 2, name: 'Maca' }, appointmentDateTime: '2025-06-24T10:30:00', veterinarian: { id: 1, name: 'Dr. Marić' } },
        { id: 3, pet: { id: 3, name: 'Pera' }, appointmentDateTime: '2025-06-23T16:15:00', veterinarian: { id: 2, name: 'Dr. Popović' } }
      ],
      pets: [
        { id: 1, name: 'Rex', species: 'Dog', owner: { id: 1, name: 'Marko Petrović' } },
        { id: 2, name: 'Maca', species: 'Cat', owner: { id: 2, name: 'Ana Nikolić' } },
        { id: 3, name: 'Pera', species: 'Bird', owner: { id: 1, name: 'Marko Petrović' } },
        { id: 4, name: 'Buddy', species: 'Dog', owner: { id: 3, name: 'Petar Milenković' } }
      ],
      showAddForm: false,
      editingTreatment: null,
      treatmentForm: {
        appointmentId: null,
        diagnosis: '',
        treatment: '',
        cost: null,
        notes: ''
      },
      petFilter: '',
      diagnosisFilter: '',
      isLoading: false,
      error: null
    }
  },
  computed: {
    filteredTreatments() {
      let filtered = this.treatments;
      
      if (this.petFilter) {
        filtered = filtered.filter(t => t.appointment?.pet?.id === parseInt(this.petFilter));
      }
      
      if (this.diagnosisFilter) {
        const searchTerm = this.diagnosisFilter.toLowerCase();
        filtered = filtered.filter(t => 
          t.diagnosis.toLowerCase().includes(searchTerm) ||
          t.treatment.toLowerCase().includes(searchTerm)
        );
      }
      
      return filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
  },
  methods: {
    editTreatment(treatment) {
      this.editingTreatment = treatment;
      this.treatmentForm = {
        appointmentId: treatment.appointment?.id,
        diagnosis: treatment.diagnosis,
        treatment: treatment.treatment,
        cost: treatment.cost,
        notes: treatment.notes || ''
      };
    },

    closeForm() {
      this.showAddForm = false;
      this.editingTreatment = null;
      this.resetForm();
    },

    resetForm() {
      this.treatmentForm = {
        appointmentId: null,
        diagnosis: '',
        treatment: '',
        cost: null,
        notes: ''
      };
    },

    saveTreatment() {
      if (this.editingTreatment) {
        const index = this.treatments.findIndex(t => t.id === this.editingTreatment.id);
        if (index !== -1) {
          this.treatments[index] = {
            ...this.treatments[index],
            diagnosis: this.treatmentForm.diagnosis,
            treatment: this.treatmentForm.treatment,
            cost: this.treatmentForm.cost,
            notes: this.treatmentForm.notes
          };
        }
      } else {
        const selectedAppointment = this.appointments.find(a => a.id === this.treatmentForm.appointmentId);
        const newTreatment = {
          id: Date.now(),
          appointment: selectedAppointment,
          diagnosis: this.treatmentForm.diagnosis,
          treatment: this.treatmentForm.treatment,
          cost: this.treatmentForm.cost,
          notes: this.treatmentForm.notes,
          createdAt: new Date().toISOString()
        };
        this.treatments.push(newTreatment);
      }
      
      this.closeForm();
    },

    deleteTreatment(treatmentId) {
      if (confirm('Are you sure you want to delete this treatment record?')) {
        this.treatments = this.treatments.filter(t => t.id !== treatmentId);
      }
    },

    formatDate(dateTimeString) {
      const date = new Date(dateTimeString);
      return date.toLocaleDateString('sr-RS', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric'
      });
    },

    getPetName(petId) {
      const pet = this.pets.find(p => p.id === petId);
      return pet ? `${pet.name} (${pet.species})` : 'Unknown Pet';
    },

    getVeterinarianName(vetId) {
      return vetId === 1 ? 'Dr. Marić' : 'Dr. Popović';
    },

    getPetOwner(petId) {
      const pet = this.pets.find(p => p.id === petId);
      return pet?.owner?.name || 'Unknown Owner';
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
.treatments {
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
  padding: 2rem 4rem;
  width: 100%;
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

.filter-group select,
.filter-group input {
  padding: 0.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 6px;
  min-width: 150px;
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
  max-width: 700px;
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
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
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

.treatments-list {
  display: grid;
  gap: 1.5rem;
}

.treatment-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.treatment-card:hover {
  transform: translateY(-3px);
}

.treatment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e5e7eb;
}

.patient-info h4 {
  color: #1e293b;
  font-size: 1.5rem;
  margin: 0 0 0.5rem 0;
}

.appointment-date {
  color: #64748b;
  font-size: 0.95rem;
}

.cost-badge {
  background: #10b981;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 600;
  font-size: 1.1rem;
}

.treatment-content {
  margin-bottom: 1.5rem;
}

.diagnosis-section,
.treatment-section,
.notes-section {
  margin-bottom: 1.5rem;
}

.diagnosis-section h5,
.treatment-section h5,
.notes-section h5 {
  color: #374151;
  font-size: 1rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.diagnosis-section p,
.treatment-section p,
.notes-section p {
  color: #1e293b;
  line-height: 1.6;
  margin: 0;
}

.metadata {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  font-size: 0.9rem;
  color: #64748b;
}

.treatment-actions {
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
    flex-wrap: wrap;
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

  .treatment-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .metadata {
    flex-direction: column;
    gap: 0.5rem;
  }

  .treatment-actions {
    justify-content: center;
  }
}
</style>