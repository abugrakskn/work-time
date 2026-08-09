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

import { CreateTaskRequest } from '../../../core/models/create-task-request';
import { UpdateTaskRequest } from '../../../core/models/update-task-request';
import { Project } from '../../../core/models/project';
import { ProjectService } from '../../../core/services/project';
import { TaskService } from '../../../core/services/task';

import { toLocalDateString } from '../../../core/utils/date.utils';

@Component({
  selector: 'app-task-form',
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
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss'
})
export class TaskForm implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private taskService = inject(TaskService);
  private projectService = inject(ProjectService);

  isEditMode = signal(false);
  taskId: number | null = null;

  projects = signal<Project[]>([]);

  title = '';
  description = '';
  dueDate: Date | null = null;
  estimatedDurationMinutes: number | null = null;
  priority = 'MEDIUM';
  status = 'TODO';
  projectId: number | null = null;
  assignedUserId: number | null = null;
  
  minDate = new Date();

  ngOnInit() {
    this.loadProjects();

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.isEditMode.set(true);
      this.taskId = Number(idParam);
      this.loadTask(this.taskId);
    }
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

  loadTask(id: number) {
    this.taskService.getById(id).subscribe({
      next: (task) => {
        this.title = task.title;
        this.description = task.description;
        this.dueDate = task.dueDate
          ? new Date(task.dueDate)
          : null;

        this.estimatedDurationMinutes =
          task.estimatedDurationMinutes;

        this.priority = task.priority;
        this.status = task.status;
        this.projectId = task.projectId;
        this.assignedUserId = task.assignedUserId;
      },
      error: (err) => {
        console.error('Task could not be loaded.', err);
      }
    });
  }

  saveTask() {
    if (!this.projectId) {
      return;
    }

    if (this.isEditMode() && this.taskId) {
      this.updateTask();
    } else {
      this.createTask();
    }
  }

  createTask() {
    const request: CreateTaskRequest = {
      title: this.title,
      description: this.description,
      dueDate: this.dueDate
        ? toLocalDateString(this.dueDate)
        : null,
      estimatedDurationMinutes: this.estimatedDurationMinutes,
      priority: this.priority,
      projectId: this.projectId!,
      assignedUserId: this.assignedUserId
    };

    this.taskService.create(request).subscribe({
      next: () => {
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        console.error('Task could not be created.', err);
      }
    });
  }

  updateTask() {
    const request: UpdateTaskRequest = {
      title: this.title,
      description: this.description,
      dueDate: this.dueDate
        ? toLocalDateString(this.dueDate)
        : null,
      estimatedDurationMinutes: this.estimatedDurationMinutes,
      priority: this.priority,
      status: this.status,
      projectId: this.projectId!,
      assignedUserId: this.assignedUserId
    };

    this.taskService.update(this.taskId!, request).subscribe({
      next: () => {
        this.router.navigate(['/tasks', this.taskId]);
      },
      error: (err) => {
        console.error('Task could not be updated.', err);
      }
    });
  }

  cancel() {
    this.router.navigate(['/tasks']);
  }
}