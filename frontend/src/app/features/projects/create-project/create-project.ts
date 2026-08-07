import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

import { CreateProjectRequest } from '../../../core/models/create-project-request';
import { UpdateProjectRequest } from '../../../core/models/update-project-request';
import { ProjectService } from '../../../core/services/project';

@Component({
  selector: 'app-create-project',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule
  ],
  templateUrl: './create-project.html',
  styleUrl: './create-project.scss'
})
export class CreateProject implements OnInit {

  private projectService = inject(ProjectService);
  private route = inject(ActivatedRoute);
  protected router = inject(Router);

  isEditMode = signal(false);
  projectId: number | null = null;

  name = '';
  description = '';
  startDate: Date | null = null;
  endDate: Date | null = null;
  status = 'PLANNED';

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.isEditMode.set(true);
      this.projectId = Number(idParam);

      this.loadProject(this.projectId);
    }
  }

  loadProject(id: number) {
    this.projectService.getById(id).subscribe({
      next: (project) => {
        this.name = project.name;
        this.description = project.description;

        this.startDate = project.startDate
          ? new Date(project.startDate)
          : null;

        this.endDate = project.endDate
          ? new Date(project.endDate)
          : null;

        this.status = project.status;
      },
      error: (err) => {
        console.error('Project could not be loaded.', err);
      }
    });
  }

  saveProject() {
    if (this.isEditMode() && this.projectId) {
      this.updateProject();
    } else {
      this.createProject();
    }
  }

  createProject() {
    if (!this.startDate || !this.endDate) {
      return;
    }

    const request: CreateProjectRequest = {
      name: this.name,
      description: this.description,
      startDate: this.toDateString(this.startDate),
      endDate: this.toDateString(this.endDate),
      status: this.status
    };

    this.projectService.create(request).subscribe({
      next: () => {
        this.router.navigate(['/projects']);
      },
      error: (err) => {
        console.error('Project could not be created.', err);
      }
    });
  }

  updateProject() {
    const request: UpdateProjectRequest = {
      name: this.name,
      description: this.description,
      startDate: this.startDate
        ? this.toDateString(this.startDate)
        : null,
      endDate: this.endDate
        ? this.toDateString(this.endDate)
        : null,
      status: this.status
    };

    this.projectService.update(this.projectId!, request).subscribe({
      next: () => {
        this.router.navigate(['/projects', this.projectId]);
      },
      error: (err) => {
        console.error('Project could not be updated.', err);
      }
    });
  }

  cancel() {
    if (this.isEditMode() && this.projectId) {
      this.router.navigate(['/projects', this.projectId]);
      return;
    }

    this.router.navigate(['/projects']);
  }

  private toDateString(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}