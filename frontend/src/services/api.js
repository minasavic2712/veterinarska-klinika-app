// API Service za komunikaciju sa Spring Boot backend-om
const API_BASE_URL = 'http://localhost:8081/api';

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  // Helper metoda za HTTP zahteve
  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    
    // Defaultne opcije
    const config = {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    // Dodaj JWT token ako postoji
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    console.log('🚀 Making request to:', url);
    console.log('📝 Request method:', config.method || 'GET');
    console.log('📋 Request headers:', config.headers);
    if (config.body) {
      console.log('📦 Request body:', config.body);
    }

    try {
      const response = await fetch(url, config);
      
      console.log('📡 Response status:', response.status);
      console.log('📄 Response headers:', Object.fromEntries(response.headers.entries()));

      // Proveri da li response ima content
      const contentType = response.headers.get('content-type');
      let data;
      
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
        console.log('✅ Response data (JSON):', data);
      } else {
        data = await response.text();
        console.log('📝 Response data (Text):', data);
      }

      if (!response.ok) {
        console.error('❌ Response not OK:', response.status, data);
        throw new Error(data.error || data.message || `HTTP ${response.status}: ${data}`);
      }

      console.log('✅ Request successful!');
      return data;
    } catch (error) {
      console.error('💥 API Error:', error);
      throw error;
    }
  }

  // GET zahtev
  get(endpoint) {
    return this.request(endpoint, {
      method: 'GET',
    });
  }

  // POST zahtev
  post(endpoint, data) {
    console.log('📤 POST request to:', endpoint);
    console.log('📦 POST data:', data);
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // PUT zahtev
  put(endpoint, data) {
    console.log('🔄 PUT request to:', endpoint);
    console.log('📦 PUT data:', data);
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  // DELETE zahtev
  delete(endpoint) {
    console.log('🗑️ DELETE request to:', endpoint);
    return this.request(endpoint, {
      method: 'DELETE',
    });
  }

  // AUTH ENDPOINTS
  
  // Login korisnika
  async login(credentials) {
    const response = await this.post('/auth/login', credentials);
    
    // Sačuvaj token u localStorage
    if (response.token) {
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('currentUser', JSON.stringify(response.user));
    }
    
    return response;
  }

  // Register novog korisnika
  async register(userData) {
    const response = await this.post('/auth/register', userData);
    
    // Sačuvaj token u localStorage
    if (response.token) {
      localStorage.setItem('authToken', response.token);
      localStorage.setItem('currentUser', JSON.stringify(response.user));
    }
    
    return response;
  }

  // Logout korisnika
  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  }

  // Proveri da li je korisnik ulogovan
  isAuthenticated() {
    return !!localStorage.getItem('authToken');
  }

  // Uzmi trenutnog korisnika
  getCurrentUser() {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  // OWNERS ENDPOINTS
  
  // Uzmi sve vlasnike
  getOwners() {
    return this.get('/owners');
  }

  // Uzmi jednog vlasnika po ID
  getOwner(id) {
    return this.get(`/owners/${id}`);
  }

  // Kreiraj novog vlasnika
  createOwner(ownerData) {
    return this.post('/owners', ownerData);
  }

  // Ažuriraj vlasnika
  updateOwner(id, ownerData) {
    return this.put(`/owners/${id}`, ownerData);
  }

  // Obriši vlasnika
  deleteOwner(id) {
    return this.delete(`/owners/${id}`);
  }

  // PETS ENDPOINTS
  
  // Uzmi sve ljubimce
  getPets() {
    return this.get('/pets');
  }

  // Uzmi jednog ljubimca po ID
  getPet(id) {
    return this.get(`/pets/${id}`);
  }

  // Uzmi ljubimce jednog vlasnika
  getPetsByOwner(ownerId) {
    return this.get(`/pets/owner/${ownerId}`);
  }

  // Uzmi ljubimce po vrsti
  getPetsBySpecies(species) {
    return this.get(`/pets/species/${species}`);
  }

  // Kreiraj novog ljubimca
  createPet(petData) {
    return this.post('/pets', petData);
  }

  // Ažuriraj ljubimca
  updatePet(id, petData) {
    return this.put(`/pets/${id}`, petData);
  }

  // Obriši ljubimca
  deletePet(id) {
    return this.delete(`/pets/${id}`);
  }

  // APPOINTMENTS ENDPOINTS (za buduće proširenje)
  
  getAppointments() {
    return this.get('/appointments');
  }

  createAppointment(appointmentData) {
    return this.post('/appointments', appointmentData);
  }

  updateAppointment(id, appointmentData) {
    return this.put(`/appointments/${id}`, appointmentData);
  }

  deleteAppointment(id) {
    return this.delete(`/appointments/${id}`);
  }
}

// Izvozi singleton instancu
export default new ApiService();