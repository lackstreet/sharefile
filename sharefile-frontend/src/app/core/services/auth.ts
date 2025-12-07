import { Injectable } from '@angular/core';

import { BehaviorSubject, Observable, tap } from 'rxjs';
import {Api} from "./api";

interface LoginRequest {
  email: string;
  password: string;
}

interface AuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private tokenKey = 'sharefile_token';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private api: Api) {
    this.loadStoredUser();
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('auth/login', { email, password }).pipe(
        tap((response: AuthResponse) => {
          localStorage.setItem(this.tokenKey, response.token);
          this.currentUserSubject.next(response.user);
        })
    );
  }

  register(data: any): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('auth/register', data).pipe(
        tap((response: AuthResponse) => {
          localStorage.setItem(this.tokenKey, response.token);
          this.currentUserSubject.next(response.user);
        })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private loadStoredUser(): void {
    const token = this.getToken();
    if (token) {
      // Qui potresti fare una chiamata GET /auth/me per ricaricare l'utente
      // Per ora assume che il token sia valido
    }
  }
}