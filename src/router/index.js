// router/index.js
import { createRouter, createWebHistory } from "vue-router";

import sendMessage from "@/components/sendMessage.vue";
import showMessage from "@/components/showMessage.vue";
import auditMessage from "@/components/auditMessage.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [

    {
      path: '/index',
      name: 'index',
      component: sendMessage
    },
    {
      path: '/screen',
      name: 'screen',
      component: showMessage
    },
    {
      path: '/audit',
      name: 'audit',
      component: auditMessage
    },
    {
      path: '/',
      redirect: '/index'
    }
  ]
});



export default router;