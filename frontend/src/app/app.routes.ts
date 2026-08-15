import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin-guard';
import { authChildGuard } from './core/guards/auth-child-guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then((component) => component.Login)
  },
  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout/main-layout')
        .then((component) => component.MainLayout),
    canActivateChild: [authChildGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard')
            .then((component) => component.Dashboard)
      },
      {
        path: 'time-entries',
        loadComponent: () =>
          import('./features/time-entries/time-entries')
            .then((component) => component.TimeEntries)
      },
      {
        path: 'tasks',
        loadComponent: () =>
          import('./features/tasks/tasks')
            .then((component) => component.Tasks)
      },
      {
        path: 'tasks/create',
        loadComponent: () =>
          import('./features/tasks/task-form/task-form')
            .then((component) => component.TaskForm),
        canActivate: [adminGuard]
      },
      {
        path: 'tasks/:id/edit',
        loadComponent: () =>
          import('./features/tasks/task-form/task-form')
            .then((component) => component.TaskForm)
      },
      {
        path: 'tasks/:id',
        loadComponent: () =>
          import('./features/tasks/task-detail/task-detail')
            .then((component) => component.TaskDetail)
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/users/users')
            .then((component) => component.Users),
        canActivate: [adminGuard]
      },
      {
        path: 'users/create',
        loadComponent: () =>
          import('./features/users/user-form/user-form')
            .then((component) => component.UserForm),
        canActivate: [adminGuard]
      },
      {
        path: 'users/:id/edit',
        loadComponent: () =>
          import('./features/users/user-form/user-form')
            .then((component) => component.UserForm),
        canActivate: [adminGuard]
      },
      {
        path: 'projects',
        loadComponent: () =>
          import('./features/projects/projects')
            .then((component) => component.Projects)
      },
      {
        path: 'projects/create',
        loadComponent: () =>
          import(
            './features/projects/create-project/create-project'
          ).then((component) => component.CreateProject),
        canActivate: [adminGuard]
      },
      {
        path: 'projects/:id/edit',
        loadComponent: () =>
          import(
            './features/projects/create-project/create-project'
          ).then((component) => component.CreateProject),
        canActivate: [adminGuard]
      },
      {
        path: 'projects/:id',
        loadComponent: () =>
          import(
            './features/projects/project-detail/project-detail'
          ).then((component) => component.ProjectDetail)
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];