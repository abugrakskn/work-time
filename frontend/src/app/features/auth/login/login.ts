import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { delay, finalize } from 'rxjs';

import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { LoginRequest } from '../../../core/models/login-request';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  isLoggingIn = signal(false);

  login(): void {
    if (this.isLoggingIn()) {
      return;
    }

    this.isLoggingIn.set(true);

    const request: LoginRequest = {
      email: this.email,
      password: this.password
    };

    this.authService.login(request)
      .pipe(
        delay(400),
        finalize(() => {
          this.isLoggingIn.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: () => {
          // Error feedback is handled by the HTTP error interceptor.
        }
      });
  }
}