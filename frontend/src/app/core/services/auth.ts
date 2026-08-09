import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

import { LoginRequest } from '../models/login-request';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  private currentUserState = signal<User | null>(null);

  currentUser = this.currentUserState.asReadonly();

  isAdmin = computed(() => {
    return this.currentUserState()?.role === 'ADMIN';
  });

  login(request: LoginRequest) {
    return this.http.post<void>(
      'http://localhost:8080/api/auth/login',
      request
    );
  }

  getCurrentUser() {
    return this.http.get<User>(
      'http://localhost:8080/api/auth/me'
    ).pipe(
      tap((user) => {
        this.currentUserState.set(user);
      })
    );
  }
}