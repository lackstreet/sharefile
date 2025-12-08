import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';
import { inject } from '@angular/core';
import { firstValueFrom, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

export const authGuard: CanActivateFn = async (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  // Se già autenticato
  if (authService.isAuthenticated()) {
    return true;
  }

  try {
    // Prova a fare refresh token
    await firstValueFrom(
        authService.refreshToken().pipe(
            switchMap(() => of(true)),
            catchError(() => of(false))
        )
    ).then(isRefreshed => {
      if (!isRefreshed) {
        router.navigate(['/auth/login']);
      }
      return isRefreshed;
    });

    return authService.isAuthenticated();
  } catch {
    router.navigate(['/auth/login']);
    return false;
  }
};
