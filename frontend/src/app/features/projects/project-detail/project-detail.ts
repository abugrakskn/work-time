import { FormsModule } from '@angular/forms';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { Project } from '../../../core/models/project';
import { Task } from '../../../core/models/task';

import { ProjectService } from '../../../core/services/project';
import { TaskService } from '../../../core/services/task';

@Component({
  selector: 'app-project-detail',
  imports: [
    RouterLink,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './project-detail.html',
  styleUrl: './project-detail.scss'
})
export class ProjectDetail implements OnInit {

  private route = inject(ActivatedRoute);
  private projectService = inject(ProjectService);
  private taskService = inject(TaskService);

  project = signal<Project | null>(null);
  projectTasks = signal<Task[]>([]);

  allProjectTasks = signal<Task[]>([]);

  selectedTaskStatus = '';
  selectedTaskPriority = '';

  ngOnInit() {

    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    this.loadProject(id);
    this.loadProjectTasks(id);
  }

  loadProject(id: number) {
    this.projectService.getById(id).subscribe({
      next: (project) => {
        this.project.set(project);
      },
      error: (err) => {
        console.error('Project could not be loaded.', err);
      }
    });
  }

  loadProjectTasks(projectId: number) {
  this.taskService.getAll().subscribe({
    next: (tasks) => {
      const projectTasks = tasks.filter(
        task => task.projectId === projectId
      );

      this.allProjectTasks.set(projectTasks);

      this.applyTaskFilters();
    },
    error: (err) => {
      console.error('Project tasks could not be loaded.', err);
    }
  });
}

  applyTaskFilters() {
  let filteredTasks = [...this.allProjectTasks()];

  if (this.selectedTaskStatus) {
    filteredTasks = filteredTasks.filter(
      task => task.status === this.selectedTaskStatus
    );
  }

  if (this.selectedTaskPriority) {
    filteredTasks = filteredTasks.filter(
      task => task.priority === this.selectedTaskPriority
    );
  }

  const statusOrder: Record<string, number> = {
    IN_PROGRESS: 4,
    TODO: 3,
    COMPLETED: 2,
    CANCELLED: 1
  };

  const priorityOrder: Record<string, number> = {
    CRITICAL: 4,
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1
  };

  filteredTasks.sort((a, b) => {
    const statusDifference =
      (statusOrder[b.status] ?? 0) -
      (statusOrder[a.status] ?? 0);

    if (statusDifference !== 0) {
      return statusDifference;
    }

    return (
      (priorityOrder[b.priority] ?? 0) -
      (priorityOrder[a.priority] ?? 0)
    );
  });

  this.projectTasks.set(filteredTasks);
}

clearTaskFilters() {
  this.selectedTaskStatus = '';
  this.selectedTaskPriority = '';

  this.applyTaskFilters();
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

  getTaskStatusClass(status: string): string {
    switch (status) {
      case 'TODO':
        return 'task-status-todo';

      case 'IN_PROGRESS':
        return 'task-status-progress';

      case 'COMPLETED':
        return 'task-status-completed';

      case 'CANCELLED':
        return 'task-status-cancelled';

      default:
        return 'task-status-default';
    }
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'LOW':
        return 'priority-low';

      case 'MEDIUM':
        return 'priority-medium';

      case 'HIGH':
        return 'priority-high';

      case 'CRITICAL':
        return 'priority-critical';

      default:
        return 'priority-default';
    }
  }
}