import { Injectable } from '@angular/core';

import { BehaviorSubject, Observable, tap } from 'rxjs';
import {Api} from "./api";
import {HttpClient} from "@angular/common/http";

interface LoginRequest {
  email: string;
  password: string;
}


@Injectable({
  providedIn: 'root'
})
export class Auth {

  static readonly LOGIN_ENDPOINT = 'auth/login';
  static readonly LOGOUT_ENDPOINT = 'auth/logout';
  static readonly REFRESH_TOKEN_ENDPOIN = 'auth/refresh';
  static readonly HEALTH_ENDPOINT = 'health';

  private currentUserSubject = new BehaviorSubject<boolean>(false);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private api: Api, private call: HttpClient) {}

  initCsrf(): Observable<any> {
    return this.call.get(
        this.api.getEndpointUrl(Auth.HEALTH_ENDPOINT),
        { withCredentials: true }
    );
  }

  login(username: string, password: string): Observable<any> {
    return this.call.post(
        this.api.getEndpointUrl(Auth.LOGIN_ENDPOINT),
        { username, password },
        { withCredentials: true }
    ).pipe(
        tap(() => this.currentUserSubject.next(true))
    );
  }

  logout(): Observable<any> {
    return this.call.post(
        this.api.getEndpointUrl(Auth.LOGOUT_ENDPOINT),
        {},
        { withCredentials: true }
    ).pipe(
        tap(() => this.currentUserSubject.next(false))
    );
  }

  refreshToken(): Observable<any> {
    return this.call.post(
        this.api.getEndpointUrl(Auth.REFRESH_TOKEN_ENDPOIN),
        {},
        { withCredentials: true }
    );
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value;
  }

  setAuthenticated(auth: boolean) {
    this.currentUserSubject.next(auth);
  }
}
