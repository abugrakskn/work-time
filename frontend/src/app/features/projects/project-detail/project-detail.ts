import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

import { Project } from '../../../core/models/project';
import { ProjectService } from '../../../core/services/project';

@Component({
  selector: 'app-project-detail',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatIconModule
  ],
  templateUrl: './project-detail.html',
  styleUrl: './project-detail.scss'
})
export class ProjectDetail implements OnInit {

  private route = inject(ActivatedRoute);
  private projectService = inject(ProjectService);

  project = signal<Project | null>(null);

  ngOnInit() {
    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    this.projectService.getById(id).subscribe({
      next: (project) => {
        this.project.set(project);
      },
      error: (err) => {
        console.error('Project could not be loaded.', err);
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