<template>
  <div class="appointments">
    <nav class="navbar">
      <div class="nav-brand">
        <h1>🏥 Veterinary Clinic</h1>
      </div>
      <div class="nav-links">
        <router-link to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link to="/owners" class="nav-link">Owners</router-link>
        <router-link to="/pets" class="nav-link">Pets</router-link>
        <router-link to="/appointments" class="nav-link">Appointments</router-link>
        <button @click="logout" class="logout-btn">Logout</button>
      </div>
    </nav>

    <div class="content">
      <div class="header">
        <h2>📅 Appointments Management</h2>
        <button @click="showAddForm = true" class="add-btn">+ Schedule Appointment</button>
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

      <!-- Status Filters -->
      <div class="filters">
        <div class="filter-group">
          <label>Filter by Status:</label>
          <div class="status-buttons">
            <button 
              @click="statusFilter = ''" 
              :class="['status-btn', { active: statusFilter === '' }]"
            >
              All
            </button>
            <button 
              @click="statusFilter = 'SCHEDULED'" 
              :class="['status-btn', 'scheduled', { active: statusFilter === 'SCHEDULED' }]"
            >
              Scheduled
            </button>
            <button 
              @click="statusFilter = 'COMPLETED'" 
              :class="['status-btn', 'completed', { active: statusFilter === 'COMPLETED' }]"
            >
              Completed
            </button>
            <button 
              @click="statusFilter = 'CANCELLED'" 
              :class="['status-btn', 'cancelled', { active: statusFilter === 'CANCELLED' }]"
            >
              Cancelled
            </button>
          </div>
        </div>

        <div class="filter-group">
          <label for="dateFilter">Filter by Date:</label>
          <select id="dateFilter" v-model="dateFilter">
            <option value="">All Dates</option>
            <option value="today">Today</option>
            <option value="week">This Week</option>
            <option value="month">This Month</option>
          </select>
        </div>
      </div>

      <!-- Add/Edit Form -->
      <div v-if="showAddForm || editingAppointment" class="form-overlay" @click="closeForm">
        <div class="form-modal" @click.stop>
          <h3>{{ editingAppointment ? 'Edit Appointment' : 'Schedule New Appointment' }}</h3>
          
          <form @submit.prevent="saveAppointment">
            <div class="form-row">
              <div class="form-group">
                <label for="pet">Pet:</label>
                <select id="pet" v-model="appointmentForm.petId" required>
                  <option value="">Select pet</option>
                  <option v-for="pet in pets" :key="pet.id" :value="pet.id">
                    {{ pet.name }} ({{ pet.species }}) - {{ getOwnerName(pet.owner?.id || pet.ownerId) }}
                  </option>
                </select>
              </div>
              
              <div class="form-group">
                <label for="veterinarian">Veterinarian:</label>
                <select id="veterinarian" v-model="appointmentForm.veterinarianId" required>
                  <option value="">Select veterinarian</option>
                  <option v-for="vet in veterinarians" :key="vet.id" :value="vet.id">
                    {{ vet.name }} - {{ vet.specialization || 'General' }}
                  </option>
                </select>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="date">Date:</label>
                <input 
                  type="date" 
                  id="date" 
                  v-model="appointmentForm.date" 
                  required
                  :min="today"
                />
              </div>
              
              <div class="form-group">
                <label for="time">Time:</label>
                <input 
                  type="time" 
                  id="time" 
                  v-model="appointmentForm.time" 
                  required
                />
              </div>
            </div>
            
            <div class="form-group">
              <label for="reason">Reason for visit:</label>
              <textarea 
                id="reason" 
                v-model="appointmentForm.reason"
                placeholder="e.g. Vaccination, Regular checkup, Dental cleaning..."
                rows="3"
                required
              ></textarea>
            </div>
            
            <div class="form-group" v-if="editingAppointment">
              <label for="status">Status:</label>
              <select id="status" v-model="appointmentForm.status">
                <option value="SCHEDULED">Scheduled</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>
            
            <div class="form-actions">
              <button type="button" @click="closeForm" class="cancel-btn">Cancel</button>
              <button type="submit" class="save-btn" :disabled="isLoading">
                {{ editingAppointment ? 'Update' : 'Schedule' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Appointments List -->
      <div class="appointments-list">
        <div v-for="appointment in filteredAppointments" :key="appointment.id" class="appointment-card">
          <div class="appointment-header">
            <div class="datetime">
              <div class="date">📅 {{ formatDate(appointment.appointmentDateTime) }}</div>
              <div class="time">🕐 {{ formatTime(appointment.appointmentDateTime) }}</div>
            </div>
            <div class="status-badge" :class="appointment.status.toLowerCase()">
              {{ appointment.status }}
            </div>
          </div>
          
          <div class="appointment-details">
            <div class="detail-row">
              <span class="label">Pet:</span>
              <span class="value">{{ getPetName(appointment.pet?.id || appointment.petId) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Veterinarian:</span>
              <span class="value">{{ getVeterinarianName(appointment.veterinarian?.id || appointment.veterinarianId) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Reason:</span>
              <span class="value">{{ appointment.reason }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Owner:</span>
              <span class="value">{{ getPetOwner(appointment.pet?.id || appointment.petId) }}</span>
            </div>
          </div>
          
          <div class="appointment-actions">
            <button @click="editAppointment(appointment)" class="edit-btn">✏️ Edit</button>
            <button @click="deleteAppointment(appointment.id)" class="delete-btn">🗑️ Delete</button>
            <button 
              v-if="appointment.status === 'SCHEDULED'" 
              @click="markCompleted(appointment)" 
              class="complete-btn"
            >
              ✅ Mark Complete
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!isLoading && filteredAppointments.length === 0" class="empty-state">
        <div class="empty-icon">📅</div>
        <h3>{{ appointments.length === 0 ? 'No appointments yet' : 'No appointments match your filters' }}</h3>
        <p>{{ appointments.length === 0 ? 'Schedule your first appointment!' : 'Try adjusting your filter settings.' }}</p>
        <button v-if="appointments.length === 0" @click="showAddForm = true" class="add-btn">+ Schedule First Appointment</button>
      </div>
    </div>
  </div>
</template>

<script>
import apiService from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'AppointmentsView',
  data() {
    return {
      appointments: [
        // Demo data
        {
          id: 1,
          pet: { id: 1, name: 'Rex' },
          veterinarian: { id: 1, name: 'Dr. Marić' },
          appointmentDateTime: '2025-06-28T14:00:00',
          reason: 'Regular checkup and vaccination',
          status: 'SCHEDULED'
        },
        {
          id: 2,
          pet: { id: 2, name: 'Maca' },
          veterinarian: { id: 1, name: 'Dr. Marić' },
          appointmentDateTime: '2025-06-27T10:30:00',
          reason: 'Dental cleaning',
          status: 'COMPLETED'
        }
      ],
      pets: [],
      veterinarians: [
        // Demo data
        { id: 1, name: 'Dr. Marić', specialization: 'General Practice' },
        { id: 2, name: 'Dr. Popović', specialization: 'Surgery' }
      ],
      owners: [],
      showAddForm: false,
      editingAppointment: null,
      appointmentForm: {
        petId: null,
        veterinarianId: null,
        date: '',
        time: '',
        reason: '',
        status: 'SCHEDULED'
      },
      statusFilter: '',
      dateFilter: '',
      isLoading: false,
      error: null
    }
  },
  computed: {
    today() {
      return new Date().toISOString().split('T')[0];
    },
    
    filteredAppointments() {
      let filtered = this.appointments;
      
      if (this.statusFilter) {
        filtered = filtered.filter(app => app.status === this.statusFilter);
      }
      
      if (this.dateFilter) {
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        
        filtered = filtered.filter(app => {
          const appDate = new Date(app.appointmentDateTime);
          const appDay = new Date(appDate.getFullYear(), appDate.getMonth(), appDate.getDate());
          
          switch (this.dateFilter) {
            case 'today':
              return appDay.getTime() === today.getTime();
            case 'week':
              const weekStart = new Date(today);
              weekStart.setDate(today.getDate() - today.getDay());
              const weekEnd = new Date(weekStart);
              weekEnd.setDate(weekStart.getDate() + 6);
              return appDay >= weekStart && appDay <= weekEnd;
            case 'month':
              return appDate.getMonth() === now.getMonth() && appDate.getFullYear() === now.getFullYear();
            default:
              return true;
          }
        });
      }
      
      return filtered.sort((a, b) => new Date(a.appointmentDateTime) - new Date(b.appointmentDateTime));
    }
  },
  async mounted() {
    await this.loadPets();
    await this.loadOwners();
    // await this.loadAppointments(); // Za kasnije kada dodamo API
  },
  methods: {
    async loadPets() {
      try {
        const response = await apiService.getPets();
        this.pets = response;
      } catch (error) {
        console.error('Error loading pets:', error);
      }
    },

    async loadOwners() {
      try {
        const response = await apiService.getOwners();
        this.owners = response;
      } catch (error) {
        console.error('Error loading owners:', error);
      }
    },

    editAppointment(appointment) {
      this.editingAppointment = appointment;
      const dateTime = new Date(appointment.appointmentDateTime);
      this.appointmentForm = {
        petId: appointment.pet?.id || appointment.petId,
        veterinarianId: appointment.veterinarian?.id || appointment.veterinarianId,
        date: dateTime.toISOString().split('T')[0],
        time: dateTime.toTimeString().slice(0, 5),
        reason: appointment.reason,
        status: appointment.status
      };
    },

    closeForm() {
      this.showAddForm = false;
      this.editingAppointment = null;
      this.resetForm();
    },

    resetForm() {
      this.appointmentForm = {
        petId: null,
        veterinarianId: null,
        date: '',
        time: '',
        reason: '',
        status: 'SCHEDULED'
      };
    },

    saveAppointment() {
      // Demo - dodaj lokalno
      const appointmentDateTime = `${this.appointmentForm.date}T${this.appointmentForm.time}:00`;
      
      if (this.editingAppointment) {
        const index = this.appointments.findIndex(a => a.id === this.editingAppointment.id);
        if (index !== -1) {
          this.appointments[index] = {
            ...this.appointments[index],
            pet: this.pets.find(p => p.id === this.appointmentForm.petId),
            veterinarian: this.veterinarians.find(v => v.id === this.appointmentForm.veterinarianId),
            appointmentDateTime,
            reason: this.appointmentForm.reason,
            status: this.appointmentForm.status
          };
        }
      } else {
        const newAppointment = {
          id: Date.now(),
          pet: this.pets.find(p => p.id === this.appointmentForm.petId),
          veterinarian: this.veterinarians.find(v => v.id === this.appointmentForm.veterinarianId),
          appointmentDateTime,
          reason: this.appointmentForm.reason,
          status: 'SCHEDULED'
        };
        this.appointments.push(newAppointment);
      }
      
      this.closeForm();
    },

    deleteAppointment(appointmentId) {
      if (confirm('Are you sure you want to delete this appointment?')) {
        this.appointments = this.appointments.filter(a => a.id !== appointmentId);
      }
    },

    markCompleted(appointment) {
      const index = this.appointments.findIndex(a => a.id === appointment.id);
      if (index !== -1) {
        this.appointments[index].status = 'COMPLETED';
      }
    },

    formatDate(dateTimeString) {
      const date = new Date(dateTimeString);
      return date.toLocaleDateString('sr-RS', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric',
        weekday: 'long'
      });
    },

    formatTime(dateTimeString) {
      const date = new Date(dateTimeString);
      return date.toLocaleTimeString('sr-RS', { 
        hour: '2-digit', 
        minute: '2-digit'
      });
    },

    getPetName(petId) {
      const pet = this.pets.find(p => p.id === petId);
      return pet ? `${pet.name} (${pet.species})` : 'Unknown Pet';
    },

    getVeterinarianName(vetId) {
      const vet = this.veterinarians.find(v => v.id === vetId);
      return vet ? vet.name : 'Unknown Veterinarian';
    },

    getPetOwner(petId) {
      const pet = this.pets.find(p => p.id === petId);
      if (pet) {
        const ownerId = pet.owner?.id || pet.ownerId;
        const owner = this.owners.find(o => o.id === ownerId);
        return owner ? owner.name : 'Unknown Owner';
      }
      return 'Unknown Owner';
    },

    getOwnerName(ownerId) {
      const owner = this.owners.find(o => o.id === ownerId);
      return owner ? owner.name : 'Unknown Owner';
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
.appointments {
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
  gap: 3rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.filter-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.status-buttons {
  display: flex;
  gap: 0.5rem;
}

.status-btn {
  padding: 0.5rem 1rem;
  border: 2px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.status-btn.active {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.status-btn.scheduled.active {
  border-color: #3b82f6;
  background: #3b82f6;
}

.status-btn.completed.active {
  border-color: #10b981;
  background: #10b981;
}

.status-btn.cancelled.active {
  border-color: #ef4444;
  background: #ef4444;
}

.filter-group select {
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

.appointments-list {
  display: grid;
  gap: 1.5rem;
}

.appointment-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.appointment-card:hover {
  transform: translateY(-3px);
}

.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.datetime {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.date {
  font-weight: 600;
  color: #1e293b;
  font-size: 1.1rem;
}

.time {
  color: #64748b;
  font-size: 0.95rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.scheduled {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-badge.completed {
  background: #d1fae5;
  color: #059669;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #dc2626;
}

.appointment-details {
  margin-bottom: 1.5rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.detail-row .label {
  font-weight: 500;
  color: #64748b;
  min-width: 100px;
}

.detail-row .value {
  color: #1e293b;
  text-align: right;
}

.appointment-actions {
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

.complete-btn {
  background: #10b981;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background 0.3s;
}

.complete-btn:hover {
  background: #059669;
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

  .status-buttons {
    flex-wrap: wrap;
  }

  .appointment-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .detail-row {
    flex-direction: column;
    gap: 0.25rem;
  }

  .detail-row .value {
    text-align: left;
  }

  .appointment-actions {
    justify-content: center;
  }
}
</style>