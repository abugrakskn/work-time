import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const currentUser = authService.currentUser();

  if (currentUser) {
    return currentUser.role === 'ADMIN'
      ? true
      : router.createUrlTree(['/dashboard']);
  }

  return authService.getCurrentUser().pipe(
    map((user) => {
      return user.role === 'ADMIN'
        ? true
        : router.createUrlTree(['/dashboard']);
    }),
    catchError(() => {
      return of(router.createUrlTree(['/']));
    })
  );
};