import { HttpInterceptorFn } from '@angular/common/http';

export const CsrfInterceptor: HttpInterceptorFn = (req, next) => {

  const getCookie = (name: string): string | null => {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  };

  const csrfToken = getCookie('csrf-token');

  console.log('🔍 CSRF Token trovato:', csrfToken);
  console.log('📨 Metodo HTTP:', req.method);
  console.log('🍪 Tutti i cookies:', document.cookie);

  if (csrfToken && req.method !== 'GET') {
    req = req.clone({
      setHeaders: {
        'X-CSRF-TOKEN': csrfToken
      }
    });
    console.log('✅ Header X-CSRF-TOKEN aggiunto');
  }

  // withCredentials deve essere impostato globalmente o per richiesta
  if (!req.withCredentials) {
    req = req.clone({
      withCredentials: true
    });
  }

  return next(req);
};