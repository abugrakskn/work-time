import { Component, OnInit, inject, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

import { Project } from '../../core/models/project';
import { ProjectService } from '../../core/services/project';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-projects',
  imports: [
    RouterLink,
    FormsModule,
    NgClass,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule
  ],
  templateUrl: './projects.html',
  styleUrl: './projects.scss'
})
export class Projects implements OnInit {

  private projectService = inject(ProjectService);
  protected readonly isAdmin = inject(AuthService).isAdmin;

  allProjects = signal<Project[]>([]);
  projects = signal<Project[]>([]);

  selectedStatus = 'ACTIVE';
  projectNameFilter = '';

  startDateFrom: Date | null = null;
  startDateTo: Date | null = null;

  endDateFrom: Date | null = null;
  endDateTo: Date | null = null;

  showAdvancedFilters = false;

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.projectService.getAll().subscribe({
      next: (projects) => {
        this.allProjects.set(projects);
        this.applyFilters();
      },
      error: (err) => {
        console.error('Projects could not be loaded.', err);
      }
    });
  }

  applyFilters() {
    let filteredProjects = [...this.allProjects()];

    if (this.selectedStatus) {
      filteredProjects = filteredProjects.filter(
        project => project.status === this.selectedStatus
      );
    }

    if (this.projectNameFilter.trim()) {
      const searchText = this.projectNameFilter
        .trim()
        .toLowerCase();

      filteredProjects = filteredProjects.filter(
        project =>
          project.name
            .toLowerCase()
            .includes(searchText)
      );
    }

    if (this.startDateFrom) {
      filteredProjects = filteredProjects.filter(
        project =>
          new Date(project.startDate) >= this.startDateFrom!
      );
    }

    if (this.startDateTo) {
      filteredProjects = filteredProjects.filter(
        project =>
          new Date(project.startDate) <= this.startDateTo!
      );
    }

    if (this.endDateFrom) {
      filteredProjects = filteredProjects.filter(
        project =>
          new Date(project.endDate) >= this.endDateFrom!
      );
    }

    if (this.endDateTo) {
      filteredProjects = filteredProjects.filter(
        project =>
          new Date(project.endDate) <= this.endDateTo!
      );
    }

    filteredProjects.sort(
      (a, b) =>
        new Date(a.endDate).getTime() -
        new Date(b.endDate).getTime()
    );

    this.projects.set(filteredProjects);
  }

  toggleAdvancedFilters() {
    this.showAdvancedFilters = !this.showAdvancedFilters;
  }

  clearFilters() {
    this.selectedStatus = '';
    this.projectNameFilter = '';

    this.startDateFrom = null;
    this.startDateTo = null;

    this.endDateFrom = null;
    this.endDateTo = null;

    this.applyFilters();
  }

  showActiveProjects() {
    this.selectedStatus = 'ACTIVE';
    this.projectNameFilter = '';

    this.startDateFrom = null;
    this.startDateTo = null;

    this.endDateFrom = null;
    this.endDateTo = null;

    this.applyFilters();
  }

  getDaysUntilEnd(endDate: string): number {
    const today = new Date();

    today.setHours(0, 0, 0, 0);

    const end = new Date(endDate);
    end.setHours(0, 0, 0, 0);

    const difference = end.getTime() - today.getTime();

    return Math.ceil(
      difference / (1000 * 60 * 60 * 24)
    );
  }

  getDeadlineClass(endDate: string): string {
    const days = this.getDaysUntilEnd(endDate);

    if (days < 0) {
      return 'deadline-overdue';
    }

    if (days <= 7) {
      return 'deadline-soon';
    }

    return 'deadline-normal';
  }

  getDeadlineText(endDate: string): string {
    const days = this.getDaysUntilEnd(endDate);

    if (days < 0) {
      return `${Math.abs(days)} day(s) overdue`;
    }

    if (days === 0) {
      return 'Ends today';
    }

    if (days === 1) {
      return '1 day remaining';
    }

    return `${days} days remaining`;
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