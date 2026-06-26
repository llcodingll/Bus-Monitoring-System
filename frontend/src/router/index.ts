import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import EventListView from '@/views/EventListView.vue'
import BusDetailView from '@/views/BusDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: DashboardView },
    { path: '/events', component: EventListView },
    { path: '/buses/:id', component: BusDetailView },
  ],
})

export default router
