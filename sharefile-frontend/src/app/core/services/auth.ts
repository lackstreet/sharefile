import { Injectable } from '@angular/core';

import { BehaviorSubject, Observable, tap } from 'rxjs';
import {Api} from "./api";

interface LoginRequest {
  email: string;
  password: string;
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

  login(username: string, password: string): Observable<any> {
    return this.api.post('auth/login', { username, password }).pipe(
        tap((response: any) => {
          // Leggi token dall'header Authorization
          const authHeader = response.headers.get('Authorization');
          if (authHeader && authHeader.startsWith('Bearer ')) {
            const token = authHeader.substring(7); // Rimuovi "Bearer "
            localStorage.setItem(this.tokenKey, token);
            this.loadUserFromToken();
          }
        })
    );
  }

  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token) {
      const payload = this.decodeToken(token);
      this.currentUserSubject.next(payload);
    }
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

  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      return null;
    }
  }
}