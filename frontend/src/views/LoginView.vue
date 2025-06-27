<template>
  <div class="login-container">
    <div class="login-card">
      <h1>🏥 Veterinary Clinic</h1>
      <h2>Login</h2>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username:</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            required
            :disabled="isLoading"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password:</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            required
            :disabled="isLoading"
          />
        </div>
        
        <!-- Error Message -->
        <div v-if="errorMessage" class="error-message">
          ⚠️ {{ errorMessage }}
        </div>

        <!-- Loading State -->
        <div v-if="isLoading" class="loading-message">
          🔄 Logging in...
        </div>
        
        <button 
          type="submit" 
          class="login-btn" 
          :disabled="isLoading"
        >
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      
      <p>Don't have an account? <router-link to="/register">Register here</router-link></p>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../stores/auth.js'

export default {
  name: 'LoginView',
  data() {
    return {
      username: '',
      password: '',
      isLoading: false,
      errorMessage: ''
    }
  },
  setup() {
    const authStore = useAuthStore()
    return { authStore }
  },
  mounted() {
    // Inicijalizuj auth store
    this.authStore.initializeAuth()
    
    // Ako je korisnik već ulogovan, preusmeri na dashboard
    if (this.authStore.isAuthenticated) {
      this.$router.push('/dashboard')
    }
  },
  methods: {
    async handleLogin() {
      // Validacija
      if (!this.username || !this.password) {
        this.errorMessage = 'Please enter both username and password'
        return
      }

      this.isLoading = true
      this.errorMessage = ''

      try {
        // Pozovi API za login
        await this.authStore.login({
          username: this.username,
          password: this.password
        })

        // Uspešan login - preusmeri na dashboard
        console.log('Login successful! Redirecting to dashboard...')
        this.$router.push('/dashboard')
        
      } catch (error) {
        // Prikaži grešku korisniku
        this.errorMessage = error.message || 'Login failed. Please try again.'
        console.error('Login failed:', error)
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 15px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1.8rem;
}

h2 {
  text-align: center;
  color: #666;
  margin-bottom: 2rem;
  font-weight: 300;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e1e1e1;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #667eea;
}

input:disabled {
  background-color: #f9fafb;
  cursor: not-allowed;
}

.error-message {
  background: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
  border: 1px solid #fecaca;
}

.loading-message {
  background: #dbeafe;
  color: #1d4ed8;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
  border: 1px solid #bfdbfe;
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: transform 0.2s;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.login-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

p {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
}

a {
  color: #667eea;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>