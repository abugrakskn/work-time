import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { API_BASE_URL } from '../config/api.config';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/users`;

  getAll() {
    return this.http.get<User[]>(this.apiUrl);
  }
}