import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, throwError, from, switchMap } from 'rxjs';
import { LocalStorageService } from '../services/localstorage.service';
import {
  AuthService,
  USER_STORAGE_ATOKEN_KEY,
} from 'src/app/modules/auth/services/auth.service';
import { Router } from '@angular/router';
import { sessionExpiredAction } from 'src/app/modules/auth/store/auth.actions';
import { Store } from '@ngrx/store';
import { environment } from 'src/environments/environment';
import { FingerprintService } from '../services/FingerPrintService';
import jwtDecode from 'jwt-decode';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private fingerprint: string | null = null;
  private fingerprintPromise: Promise<string> | null = null;

  constructor(
    private localStorageService: LocalStorageService,
    private authService: AuthService,
    private router: Router,
    private store: Store,
    private fingerprintService: FingerprintService
  ) {
    // Initialize fingerprint in memory
    this.loadFingerprint();
  }

  private loadFingerprint(): void {
    // Start asynchronous fingerprint generation
    this.fingerprintPromise = this.fingerprintService
      .getFingerprint()
      .then((fp) => {
        this.fingerprint = fp;

        this.fingerprintPromise = null; // Clear promise after resolution
        return fp;
      })
      .catch((error) => {
        this.fingerprintPromise = null;
        throw error;
      });
  }

  private getFingerprint(): Observable<string> {
    if (this.fingerprint) {
      return from([this.fingerprint]);
    }
    if (this.fingerprintPromise) {
      return from(this.fingerprintPromise);
    }
    // Fallback: Generate a new fingerprint
    this.fingerprintPromise = this.fingerprintService
      .getFingerprint()
      .then((fp) => {
        this.fingerprint = fp;

        this.fingerprintPromise = null;
        return fp;
      })
      .catch((error) => {
        this.fingerprintPromise = null;
        throw error;
      });
    return from(this.fingerprintPromise);
  }

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    const isApiUrl = request.url.startsWith(`${environment.apiUrl}`);
    const isAuthRequest = request.url.includes('/auth/authenticate');
    const accessToken = this.localStorageService.getAccessToken();

    // Skip adding headers for authentication requests
    if (isAuthRequest) {
      return next.handle(request);
    }

    if (isApiUrl && accessToken) {
      try {
        // Validate token format
        const decoded: any = jwtDecode(accessToken);
        if (!decoded.sub) {
          this.authService.logOut();
          this.store.dispatch(
            sessionExpiredAction({ error: 'Invalid token, please login again' })
          );
          this.router.navigate(['']);
          return throwError(() => new Error('Invalid JWT token'));
        }

        // Get fingerprint and add headers
        return this.getFingerprint().pipe(
          switchMap((fingerprint) => {
            request = request.clone({
              setHeaders: {
                Authorization: `Bearer ${accessToken}`,
                'X-Client-Fingerprint': fingerprint,
              },
            });

            return next.handle(request).pipe(
              catchError((httpError) => {
                // Only handle actual authentication errors
                if (httpError.status === 401 || httpError.status === 403) {
                  this.authService.logOut();
                  this.store.dispatch(
                    sessionExpiredAction({
                      error: 'Session expired, please login again',
                    })
                  );
                  this.router.navigate(['']);
                }
                // For other errors (400, 500, etc.), just pass them through
                return throwError(() => httpError);
              })
            );
          }),
          catchError((fingerprintError) => {
            // Only logout if fingerprint is critical for auth, otherwise warn
            console.warn('Fingerprint generation failed:', fingerprintError);

            // Try request without fingerprint
            request = request.clone({
              setHeaders: {
                Authorization: `Bearer ${accessToken}`,
              },
            });

            return next.handle(request).pipe(
              catchError((httpError) => {
                // Only handle actual authentication errors
                if (httpError.status === 401 || httpError.status === 403) {
                  this.authService.logOut();
                  this.store.dispatch(
                    sessionExpiredAction({
                      error: 'Session expired, please login again',
                    })
                  );
                  this.router.navigate(['']);
                }
                return throwError(() => httpError);
              })
            );
          })
        );
      } catch (error) {
        this.authService.logOut();
        this.store.dispatch(
          sessionExpiredAction({ error: 'Invalid token, please login again' })
        );
        this.router.navigate(['']);
        return throwError(() => new Error('Invalid JWT token'));
      }
    } else {
      return next.handle(request).pipe(
        catchError((error) => {
          // Only handle actual authentication errors for non-authenticated requests
          if (error.status === 401 || error.status === 403) {
            this.authService.logOut();
            this.store.dispatch(
              sessionExpiredAction({
                error: 'Session expired, please login again',
              })
            );
            this.router.navigate(['']);
          }
          // For other errors (400, 500, etc.), just pass them through
          return throwError(() => error);
        })
      );
    }
  }
}
