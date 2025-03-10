import { createRouter, createWebHistory } from 'vue-router'

const routes = [{
  path: '/',
  component: () => import('../views/main.vue'),
  children: [{
    path: 'welcome',
    component: () => import('../views/main/welcome.vue'),
  }, {
    path: 'about',
    component: () => import('../views/main/about.vue'),
  }, {
    path: 'base',
    children: [
      {
        path: 'station',
        component: () => import('../views/main/base/station.vue')
      }, {
        path: 'train-station',
        component: () => import('../views/main/base/train-station.vue')
      },
      {
        path: 'train',
        component: () => import('../views/main/base/train.vue')
      },
      {
        path: 'train-carriage',
        component: () => import('../views/main/base/train-carriage.vue')
      },
      {
        path: 'train-seat',
        component: () => import('../views/main/base/train-seat.vue')
      }
    ]
  }, {
    path: 'business',
    children: [
      {
        path: 'sk-token',
        name: 'sk-token',
        component: () => import('../views/main/business/sk-token.vue'),
      },
      {
        path: 'daily-train',
        name: 'daily-train',
        component: () => import('../views/main/business/daily-train.vue'),
      },
      {
        path: 'daily-train-station',
        name: 'daily-train-station',
        component: () => import('../views/main/business/daily-train-station.vue')
      },
      {
        path: 'daily-train-carriage',
        name: 'daily-train-carriage',
        component: () => import('../views/main/business/daily-train-carriage.vue')
      },
      {
        path: 'daily-train-seat',
        name: 'daily-train-seat',
        component: () => import('../views/main/business/daily-train-seat.vue')
      },
      {
        path: 'daily-train-ticket',
        name: 'daily-train-ticket',
        component: () => import('../views/main/business/daily-train-ticket.vue')
      },
      {
        path: 'confirm-order',
        name: 'confirm-order',
        component: () => import('../views/main/business/confirm-order.vue')
      }


    ]
  }, {
    path: 'batch',
    children: [
      {
        path: 'job',
        name: 'job',
        component: () => import('../views/main/batch/job.vue'),
      }
    ]
  }, {
    path: 'member',
    children: [
      {
        path: 'ticket',
        name: 'ticket',
        component: () => import('../views/main/member/ticket.vue'),
      }
    ]
  }
  ]
}, {
    path: '',
    redirect: '/welcome'
  }];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
