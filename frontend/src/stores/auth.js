// Pinia store za autentifikaciju
import { defineStore } from 'pinia'
import apiService from '../services/api.js'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isLoading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.role || null,
    userName: (state) => state.user ? `${state.user.firstName} ${state.user.lastName}` : ''
  },

  actions: {
    // Inicijalizuj store sa podacima iz localStorage
    initializeAuth() {
      const token = localStorage.getItem('authToken');
      const user = localStorage.getItem('currentUser');
      
      if (token && user) {
        this.token = token;
        this.user = JSON.parse(user);
      }
    },

    // Login korisnika
    async login(credentials) {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await apiService.login(credentials);
        
        this.token = response.token;
        this.user = response.user;
        
        console.log('Login successful:', this.user);
        return response;
      } catch (error) {
        this.error = error.message;
        console.error('Login error:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // Register novog korisnika
    async register(userData) {
      this.isLoading = true;
      this.error = null;

      try {
        const response = await apiService.register(userData);
        
        this.token = response.token;
        this.user = response.user;
        
        console.log('Registration successful:', this.user);
        return response;
      } catch (error) {
        this.error = error.message;
        console.error('Registration error:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // Logout korisnika
    logout() {
      apiService.logout();
      this.user = null;
      this.token = null;
      this.error = null;
      
      console.log('User logged out');
    },

    // Očisti greške
    clearError() {
      this.error = null;
    }
  }
});