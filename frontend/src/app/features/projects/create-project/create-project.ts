import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

import { CreateProjectRequest } from '../../../core/models/create-project-request';
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
export class CreateProject {

  private projectService = inject(ProjectService);
  protected router = inject(Router);

  name = '';
  description = '';
  startDate: Date | null = null;
  endDate: Date | null = null;
  status = 'PLANNED';

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

  private toDateString(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}