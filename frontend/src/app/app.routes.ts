import { Routes } from '@angular/router';

import { authChildGuard } from './core/guards/auth-child-guard';
import { adminGuard } from './core/guards/admin-guard';
import { Login } from './features/auth/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { Projects } from './features/projects/projects';
import { CreateProject } from './features/projects/create-project/create-project';
import { ProjectDetail } from './features/projects/project-detail/project-detail';
import { Tasks } from './features/tasks/tasks';
import { MainLayout } from './layout/main-layout/main-layout';
import { TaskDetail } from './features/tasks/task-detail/task-detail';
import { TaskForm } from './features/tasks/task-form/task-form';

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
        path: 'tasks/:id/edit',
        component: TaskForm
      },
      {
        path: 'tasks/:id/edit',
        component: TaskForm,
        canActivate: [adminGuard]
      },
      {
        path: 'tasks/:id',
        component: TaskDetail
      },
      {
        path: 'projects',
        component: Projects
      },
      {
        path: 'projects/:id/edit',
        component: CreateProject,
        canActivate: [adminGuard]
      },
      {
        path: 'projects/:id/edit',
        component: CreateProject
      },
{
  path: 'projects/:id',
  component: ProjectDetail
}
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];