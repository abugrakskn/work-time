import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { User } from '../../core/models/user';
import { UserService } from '../../core/services/user';

@Component({
  selector: 'app-users',
  imports: [
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './users.html',
  styleUrl: './users.scss'
})
export class Users implements OnInit {

  private userService = inject(UserService);

  allUsers = signal<User[]>([]);
  users = signal<User[]>([]);

  searchText = '';
  selectedRole = '';
  selectedStatus = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAll().subscribe({
      next: (users) => {
        const sortedUsers = [...users].sort(
          (firstUser, secondUser) => {
            const firstName =
              `${firstUser.firstName} ${firstUser.lastName}`;

            const secondName =
              `${secondUser.firstName} ${secondUser.lastName}`;

            return firstName.localeCompare(secondName);
          }
        );

        this.allUsers.set(sortedUsers);
        this.applyFilters();
      },
      error: (err) => {
        console.error(
          'Users could not be loaded.',
          err
        );
      }
    });
  }

  applyFilters(): void {
    const normalizedSearch =
      this.searchText.trim().toLowerCase();

    const filteredUsers = this.allUsers().filter(
      (user) => {
        const fullName =
          `${user.firstName} ${user.lastName}`
            .toLowerCase();

        const matchesSearch =
          !normalizedSearch
          || fullName.includes(normalizedSearch)
          || user.email
            .toLowerCase()
            .includes(normalizedSearch);

        const matchesRole =
          !this.selectedRole
          || user.role === this.selectedRole;

        const matchesStatus =
          !this.selectedStatus
          || (
            this.selectedStatus === 'ACTIVE'
              ? user.active
              : !user.active
          );

        return matchesSearch
          && matchesRole
          && matchesStatus;
      }
    );

    this.users.set(filteredUsers);
  }

  clearFilters(): void {
    this.searchText = '';
    this.selectedRole = '';
    this.selectedStatus = '';

    this.applyFilters();
  }
}