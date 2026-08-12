import { Component, inject } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';

import { AuthService } from '../../core/services/auth';
import { NotificationService } from '../../core/services/notification';

@Component({
  selector: 'app-main-layout',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatToolbarModule
  ],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss'
})
export class MainLayout {

  private authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);

  protected readonly currentUser =
          this.authService.currentUser;
  protected readonly isAdmin =
          this.authService.isAdmin;

  protected logout(): void {
    const confirmed = window.confirm(
      'Are you sure you want to log out?'
    );

    if (!confirmed) {
      return;
    }

    this.authService.logout().subscribe({
      next: () => {
        this.notificationService.success(
          'Logged out successfully.'
        );

        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Logout failed.', err);
      }
    });
  }
}