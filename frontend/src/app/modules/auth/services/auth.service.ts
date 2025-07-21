import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {
  BehaviorSubject,
  Observable,
  catchError,
  filter,
  from,
  map,
  of,
  switchMap,
  throwError,
} from 'rxjs';
import {
  User,
  FetchUsersState,
  RegisterUserRequest,
  RegisterUserResponse,
  UpdateUserRequest,
  UpdateUserResponse,
} from '../interfaces/user.interface';
import jwtDecode from 'jwt-decode';
import { Router, UrlTree } from '@angular/router';
import { environment } from 'src/environments/environment';
import { CryptoService } from 'src/app/core/services/crypto.service';
import { FingerprintService } from 'src/app/core/services/FingerPrintService';

export const USER_STORAGE = 'USER';
export const USER_STORAGE_ATOKEN_KEY = 'ACCESS_TOKEN';
export const USER_STORAGE_RTOKEN_KEY = 'REFRESH_TOKEN';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private user: BehaviorSubject<User | null | undefined> = new BehaviorSubject<
    User | null | undefined
  >(undefined);
  private roleSubject = new BehaviorSubject<string | null>(null);
  private fingerprint: string | null = null; // Store fingerprint in memory
  private fingerprintPromise: Promise<string> | null = null;

  constructor(
    private http: HttpClient,
    private cryptoService: CryptoService,
    private fingerprintService: FingerprintService
  ) {
    this.loadUser();
    this.loadStoredRole();
  }

  loadUser() {
    const token = localStorage.getItem(USER_STORAGE_ATOKEN_KEY);
    const rtoken = localStorage.getItem(USER_STORAGE_RTOKEN_KEY);
    const loggeduser = localStorage.getItem(USER_STORAGE);

    if (token && rtoken && loggeduser) {
      const decoded: any = token;
      const user: User = {
        access_token: token,
        refresh_token: rtoken,
        user: loggeduser,
      };
      this.user.next(user);
    } else {
      this.user.next(null);
    }
  }

  private loadFingerprint(): void {
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
  }

  public getFingerprint(): Observable<string> {
    if (this.fingerprint) {
      return from([this.fingerprint]);
    }
    if (this.fingerprintPromise) {
      return from(this.fingerprintPromise);
    }
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

  login(domain: string, userPass: string): Observable<any> {
    return this.getFingerprint().pipe(
      switchMap((fingerprint) => {
        const headers = new HttpHeaders({
          'X-Client-Fingerprint': fingerprint,
          'Content-Type': 'application/json',
        });

        return this.http.post(
          `${environment.apiUrl}/auth/authenticate`,
          { domain, userPass },
          { headers }
        );
      }),
      map((res: any) => {
        if (!res || !res.access_token || !res.refresh_token || !res.user) {
          throw new Error('Invalid response from server');
        }
        try {
          const decoded: any = jwtDecode(res.access_token);
          if (!decoded.sub) {
            throw new Error('Invalid JWT received from server');
          }
        } catch (error) {
          throw new Error('Invalid JWT received from server');
        }

        localStorage.setItem(USER_STORAGE, res.user);
        localStorage.setItem(USER_STORAGE_ATOKEN_KEY, res.access_token);
        localStorage.setItem(USER_STORAGE_RTOKEN_KEY, res.refresh_token);

        if (res.role) {
          this.cryptoService.setRole(res.role);
          this.roleSubject.next(res.role);
        }

        const user: User = {
          access_token: res.access_token,
          refresh_token: res.refresh_token,
          user: res.user,
        };
        this.user.next(user);

        return res;
      }),
      catchError((error) => {
        return throwError(
          () => new Error('Login failed: ' + (error.message || 'Unknown error'))
        );
      })
    );
  }
  refresh_token(): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/refresh-token`, {});
  }
  logOut() {
    localStorage.removeItem(USER_STORAGE);
    localStorage.removeItem(USER_STORAGE_ATOKEN_KEY);
    localStorage.removeItem(USER_STORAGE_RTOKEN_KEY);
    this.cryptoService.clearRole();
    this.roleSubject.next(null);
    this.http.post(`${environment.apiUrl}/auth/logout`, {});
    this.user.next(null);
  }

  getCurrentuser() {
    return this.user.asObservable();
  }

  getCurrentUserId() {
    return this.user.getValue()?.user;
  }

  isLoggedIn(): Observable<boolean | UrlTree> {
    const router = inject(Router);

    return this.getCurrentuser().pipe(
      filter((user) => user !== undefined),
      map((isAuthenticated) => {
        if (isAuthenticated) {
          return true;
        } else {
          return router.createUrlTree(['/']);
        }
      })
    );
  }

  shouldLogin(): Observable<boolean | UrlTree> {
    const router = inject(Router);

    return this.getCurrentuser().pipe(
      filter((user) => user !== undefined),
      map((isAuthenticated) => {
        if (isAuthenticated) {
          return router.createUrlTree(['/dashboard']);
        } else {
          return true;
        }
      })
    );
  }

  getUsers(
    pageNumber: number,
    pageSize: number,
    search?: string
  ): Observable<FetchUsersState> {
    let url = `${environment.apiUrl}/auth/users?pageNumber=${pageNumber}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<FetchUsersState>(url);
  }

  registerUser(request: RegisterUserRequest): Observable<RegisterUserResponse> {
    return this.http.post<RegisterUserResponse>(
      `${environment.apiUrl}/auth/register`,
      request
    );
  }

  updateUser(
    userId: number,
    request: UpdateUserRequest
  ): Observable<UpdateUserResponse> {
    return this.http.put<UpdateUserResponse>(
      `${environment.apiUrl}/auth/users/${userId}`,
      request
    );
  }

  private loadStoredRole() {
    const storedRole = this.cryptoService.getRole();
    if (storedRole) {
      this.roleSubject.next(storedRole);
    }
  }

  getUserRole(): Observable<string | null> {
    const storedRole = this.cryptoService.getRole();
    if (storedRole) {
      return of(storedRole);
    }

    return this.http
      .post<{ role: string }>(
        `${environment.apiUrl}/auth/getLoggedInUserRole`,
        {}
      )
      .pipe(
        map((response) => {
          const role = response?.role;
          if (role) {
            this.cryptoService.setRole(role);
            return role;
          }
          this.logOut();
          return null;
        }),
        catchError((error) => {
          // If the error is due to network failure, logout
          if (error.status === 0) {
            this.logOut();
          }
          return of(null);
        })
      );
  }

  fetchUserRole(): Observable<string> {
    return this.http
      .get<{ role: string }>(`${environment.apiUrl}/user/role`)
      .pipe(
        map((response) => {
          this.cryptoService.setRole(response.role);
          this.roleSubject.next(response.role);
          return response.role;
        })
      );
  }

  isAdmin(): boolean {
    const userRole = this.cryptoService.getRole();
    return userRole === 'ADMIN';
  }

  isAdminOrIT(): boolean {
    const userRole = this.cryptoService.getRole();
    return userRole === 'IT' || userRole === 'ADMIN';
  }
}
