<template>
  <div class="dashboard">
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

    <div class="dashboard-content">
      <div class="welcome-section">
        <h2>Welcome back! 👋</h2>
        <p>Manage your veterinary clinic efficiently</p>
      </div>

      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon">🐕</div>
          <div class="stat-info">
            <h3>Total Pets</h3>
            <p class="stat-number">25</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-info">
            <h3>Pet Owners</h3>
            <p class="stat-number">18</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📅</div>
          <div class="stat-info">
            <h3>Appointments Today</h3>
            <p class="stat-number">7</p>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">⚕️</div>
          <div class="stat-info">
            <h3>Active Treatments</h3>
            <p class="stat-number">12</p>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <h3>Quick Actions</h3>
        <div class="actions-grid">
          <router-link to="/owners" class="action-btn">
            <span class="action-icon">👥</span>
            Manage Owners
          </router-link>
          <router-link to="/pets" class="action-btn">
            <span class="action-icon">🐕</span>
            Manage Pets
          </router-link>
          <router-link to="/appointments" class="action-btn">
            <span class="action-icon">📅</span>
            View Appointments
          </router-link>
          <router-link to="/treatments" class="action-btn">
            <span class="action-icon">⚕️</span>
            Treatments
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'DashboardView',
  methods: {
    logout() {
      console.log('🚪 Logout button clicked!')
      
      // Koristi auth store
      const authStore = useAuthStore()
      authStore.logout()
      
      console.log('✅ Auth store logout called')
      console.log('🏃 Redirecting to login...')
      
      // Preusmeri na login
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.dashboard {
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

.dashboard-content {
  padding: 2rem;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  margin-bottom: 3rem;
}

.welcome-section h2 {
  color: #1e293b;
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.welcome-section p {
  color: #64748b;
  font-size: 1.1rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-info h3 {
  color: #64748b;
  font-size: 0.9rem;
  margin: 0 0 0.5rem 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-number {
  color: #1e293b;
  font-size: 2rem;
  font-weight: bold;
  margin: 0;
}

.quick-actions h3 {
  color: #1e293b;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.action-btn {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  text-decoration: none;
  color: #1e293b;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-weight: 500;
}

.action-btn:hover {
  transform: translateY(-5px);
  color: #667eea;
}

.action-icon {
  font-size: 2rem;
}
</style>