import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

import { CreateUserRequest } from '../../../core/models/create-user-request';
import { UpdateUserRequest } from '../../../core/models/update-user-request';
import { UserRole } from '../../../core/models/user';

import { AuthService } from '../../../core/services/auth';
import { NotificationService } from '../../../core/services/notification';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-user-form',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule
  ],
  templateUrl: './user-form.html',
  styleUrl: './user-form.scss'
})
export class UserForm implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);

  private authService = inject(AuthService);
  private notificationService =
    inject(NotificationService);
  private userService = inject(UserService);

  protected readonly currentUser =
    this.authService.currentUser;

  isEditMode = signal(false);
  isSaving = signal(false);

  userId: number | null = null;
  originalEmail = '';

  firstName = '';
  lastName = '';
  email = '';
  password = '';
  role: UserRole = 'EMPLOYEE';
  active = true;

  ngOnInit(): void {
    const idParam =
      this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      return;
    }

    this.isEditMode.set(true);
    this.userId = Number(idParam);

    this.loadUser(this.userId);
  }

  protected isOwnAccount(): boolean {
    return this.currentUser()?.id === this.userId;
  }

  loadUser(id: number): void {
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.email = user.email;
        this.originalEmail = user.email;
        this.role = user.role;
        this.active = user.active;
      },
      error: (err) => {
        console.error(
          'User could not be loaded.',
          err
        );
      }
    });
  }

  saveUser(): void {
    if (this.isSaving()) {
      return;
    }

    this.isSaving.set(true);

    if (this.isEditMode() && this.userId) {
      this.updateUser();
      return;
    }

    this.createUser();
  }

  createUser(): void {
    const request: CreateUserRequest = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      role: this.role
    };

    this.userService.create(request)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.notificationService.success(
            'User created successfully.'
          );

          this.router.navigate(['/users']);
        },
        error: (err) => {
          console.error(
            'User could not be created.',
            err
          );
        }
      });
  }

  updateUser(): void {
    const request: UpdateUserRequest = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      role: this.role,
      active: this.active
    };

    this.userService
      .update(this.userId!, request)
      .pipe(
        finalize(() => {
          this.isSaving.set(false);
        })
      )
      .subscribe({
        next: () => {
          const ownEmailChanged =
            this.isOwnAccount()
            && this.email.trim().toLowerCase()
              !== this.originalEmail.toLowerCase();

          if (ownEmailChanged) {
            this.authService.logout().subscribe({
              next: () => {
                this.notificationService.success(
                  'Your account was updated. Please sign in again.'
                );

                this.router.navigate(['/']);
              },
              error: (err) => {
                console.error(
                  'Logout after email update failed.',
                  err
                );
              }
            });

            return;
          }

          this.notificationService.success(
            'User updated successfully.'
          );

          this.router.navigate(['/users']);
        },
        error: (err) => {
          console.error(
            'User could not be updated.',
            err
          );
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/users']);
  }
}