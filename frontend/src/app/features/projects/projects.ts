import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { Project } from '../../core/models/project';
import { ProjectService } from '../../core/services/project';

import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-projects',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './projects.html',
  styleUrl: './projects.scss'
})
export class Projects implements OnInit {

  private projectService = inject(ProjectService);

  projects = signal<Project[]>([]);

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getAll().subscribe({
      next: (projects) => {
        this.projects.set(projects);
      },
      error: (err) => {
        console.error('Projects could not be loaded.', err);
      }
    });
  }

  getStatusClass(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'status-active';

    case 'COMPLETED':
      return 'status-completed';

    case 'CANCELLED':
      return 'status-cancelled';

    case 'PLANNED':
      return 'status-planned';

    default:
      return 'status-default';
  }
  } 
}