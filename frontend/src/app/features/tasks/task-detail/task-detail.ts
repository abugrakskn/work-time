import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { Task } from '../../../core/models/task';
import { TaskService } from '../../../core/services/task';

@Component({
  selector: 'app-task-detail',
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.scss'
})
export class TaskDetail implements OnInit {

  private route = inject(ActivatedRoute);
  private taskService = inject(TaskService);

  task = signal<Task | null>(null);

  ngOnInit() {
    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    this.taskService.getById(id).subscribe({
      next: (task) => {
        this.task.set(task);
      },
      error: (err) => {
        console.error('Task could not be loaded.', err);
      }
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'TODO':
        return 'status-todo';
      case 'IN_PROGRESS':
        return 'status-progress';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return 'status-default';
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