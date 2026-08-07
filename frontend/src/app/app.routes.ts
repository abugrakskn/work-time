import { Routes } from '@angular/router';

import { authChildGuard } from './core/guards/auth-child-guard';
import { Login } from './features/auth/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { Projects } from './features/projects/projects';
import { Tasks } from './features/tasks/tasks';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [
  {
    path: '',
    component: Login
  },

  {
    path: '',
    component: MainLayout,
    canActivateChild: [authChildGuard],
    children: [
      {
        path: 'dashboard',
        component: Dashboard
      },
      {
        path: 'tasks',
        component: Tasks
      },
      {
        path: 'projects',
        component: Projects
      }
    ]
  },

  {
    path: '**',
    redirectTo: ''
  }
];