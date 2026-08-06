import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../models/login-request';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  login(request: LoginRequest) {
    return this.http.post(
      'http://localhost:8080/api/auth/login',
      request,
      {
        withCredentials: true
      }
    );
  }

  getCurrentUser() {
    return this.http.get(
      'http://localhost:8080/api/auth/me',
      {
        withCredentials: true
      }
    );
  }

}