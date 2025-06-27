import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import DashboardView from '../views/DashboardView.vue'
import OwnersView from '../views/OwnersView.vue'
import PetsView from '../views/PetsView.vue'
import AppointmentsView from '../views/AppointmentsView.vue'
import TreatmentsView from '@/views/TreatmentsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView
    },
    {
      path: '/owners',
      name: 'owners',
      component: OwnersView
    },
    {
      path: '/pets',
      name: 'pets',
      component: PetsView
    },
    {
      path: '/appointments',
      name: 'appointments',
      component: AppointmentsView
    },
    {
      path: '/treatments',
      name: 'treatments',
      component: TreatmentsView
    }
  ]
})

export default router