import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

import { LoginRequest } from '../models/login-request';
import { User } from '../models/user';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/auth`;

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
      `${this.apiUrl}/me`
    ).pipe(
      tap((user) => {
        this.currentUserState.set(user);
      })
    );
  }

  logout() {
    return this.http.post<void>(
      `${this.apiUrl}/logout`,
      {}
    ).pipe(
      tap(() => {
        this.currentUserState.set(null);
      })
    )
  }
}