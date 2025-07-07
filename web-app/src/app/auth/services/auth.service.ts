import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environment/environment';
import {RegisterRequest} from '../models/register.interface';
import {LoginRequest} from '../models/login.interface';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {Router} from '@angular/router';
import {UserStoreService} from '../../core/services/user-store.service';
import {UserProfile} from '../../features/profile/models/user-profile.interface';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private userStore = inject(UserStoreService);
  private isLoggedIn = new BehaviorSubject<boolean>(false)
  private baseUrl = environment.baseUrl;

  constructor() {
    this.checkAuth();
  }

  login(loginRequest: LoginRequest) {
    return this.http.post<{ token: string }>(`${this.baseUrl}/authentication/sign-in`, loginRequest).pipe(
      tap((response) => {
        const expires = new Date((JSON.parse(atob(response.token.split('.')[1])).exp * 1000) - (5 * 60 * 60 * 1000)).toUTCString();
        document.cookie = `tokenCookie=${response.token}; path=/; expires=${expires};`;
        this.isLoggedIn.next(true);

        this.http.get<UserProfile>(`${this.baseUrl}/users/me`).subscribe({
          next: (user) => this.userStore.setUser(user)
        });
      })
    );
  }

  register(registerRequest: RegisterRequest) {
    return this.http.post(`${this.baseUrl}/users/sign-up`, registerRequest);
  }

  checkAuth(): void {
    const token = this.userStore.getCookie('tokenCookie');
    const expired = this.userStore.isTokenExpired();

    const isValid = !!token && !expired;
    this.isLoggedIn.next(isValid);

    if (isValid) {
      this.http.get<UserProfile>(`${this.baseUrl}/users/me`).subscribe({
        next: (user) => this.userStore.setUser(user),
        error: () => {
          this.isLoggedIn.next(false);
        }
      });
    } else {
      this.isLoggedIn.next(false);
      this.userStore.clearUser();
    }
  }

  isAuthenticated(): Observable<boolean> {
    return this.isLoggedIn.asObservable();
  }

  logout(): void {
    this.http.post(`${this.baseUrl}/authentication/log-out`, {}).subscribe({
      next: () => {
        document.cookie = 'tokenCookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        this.userStore.clearUser();
        this.isLoggedIn.next(false);
        this.router.navigate(['/login']).then();
      }
    });
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/recover-password/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/recover-password/reset-password`, { token, newPassword });
  }
}
