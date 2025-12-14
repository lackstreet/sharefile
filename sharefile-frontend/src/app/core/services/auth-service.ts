import {computed, Injectable, signal} from '@angular/core';
import {AuthResourceService, UserInfo} from "../../generated/api";
import {map, of, tap} from "rxjs";
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

    //stato interno
    private readonly _user = signal<UserInfo | null>(null);
    private readonly _isLoading =signal<boolean>(false);

    //stato pubblico
    readonly user = computed(() =>this._user());
    readonly isAuthenticated = computed(() => this._user() !== null);
    readonly isLoading = computed(() => this._isLoading());

    constructor(private readonly authApi: AuthResourceService){}

    checkAuth(){
        this._isLoading.set(true);

        return this.authApi.getCurrentUser().pipe(
            map(user => {
                this._user.set(user);
                return true;
            }),
            catchError(() => {
                this._user.set(null);
                return of(false);
            }),
            tap(() => this._isLoading.set(false))
        );
    }

    login(returnUrl?: string): void{
        const urlToSave = returnUrl || window.location.pathname;
        sessionStorage.setItem('returnUrl', urlToSave);
        window.location.href = '/api/v1/auth/login';
    }

    logout(): void{
        sessionStorage.removeItem('returnUrl');
        this._user.set(null);
        window.location.href = '/api/v1/auth/logout';
    }

    getReturnUrl(): string {
        return sessionStorage.getItem('returnUrl') || '/';
    }

    clearReturnUrl(): void {
        sessionStorage.removeItem('returnUrl');
    }

    getCurrentUser(): UserInfo | null {
        return this._user();
    }

}
