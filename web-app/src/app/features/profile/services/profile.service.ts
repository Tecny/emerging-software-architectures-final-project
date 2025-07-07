import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {UserProfile} from '../models/user-profile.interface';
import {tap} from 'rxjs';
import {AuthService} from '../../../auth/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private baseUrl = environment.baseUrl;

  getUserInfo() {
    return this.http.get<UserProfile>(`${this.baseUrl}/users/me`);
  }

  changeName(name: string) {
    return this.http.put(`${this.baseUrl}/users/name`, { name });
  }

  changeEmail(newEmail: string) {
    return this.http.put(`${this.baseUrl}/users/email`, { newEmail }).pipe(
      tap(() => {
        this.authService.logout();
      })
    );
  }

  changePassword(newPassword: string) {
    return this.http.put(`${this.baseUrl}/users/password`, { newPassword }).pipe(
      tap(() => {
        this.authService.logout();
      })
    );
  }

  rechargeCredits(amount: number) {
    return this.http.post<{ approval_url: string }>(
      `${this.baseUrl}/deposit/create-deposit?amount=${amount}`,
      {}
    );
  }

}
