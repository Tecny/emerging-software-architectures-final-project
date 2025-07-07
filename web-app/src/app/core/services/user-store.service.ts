import {Injectable, signal} from '@angular/core';
import {jwtDecode} from 'jwt-decode';
import {UserRole} from '../models/user.role.enum';
import {UserProfile} from '../../features/profile/models/user-profile.interface';

@Injectable({
  providedIn: 'root'
})
export class UserStoreService {

  private user = signal<UserProfile | null>(null);

  getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    return match ? match[2] : null;
  }

  getRoleFromToken(): UserRole | null {
    const token = this.getCookie('tokenCookie');
    if (token) {
      const decoded: any = jwtDecode(token);
      return decoded.role ? UserRole[decoded.role as keyof typeof UserRole] : null;
    }
    return null;
  }

  getTokenExpiration(): number | null {
    try {
      const token = this.getCookie('tokenCookie');
      if (!token) return null;
      const decoded: any = jwtDecode(token);
      return decoded.exp ? decoded.exp * 1000 : null;
    } catch (error) {
      return null;
    }
  }

  isTokenExpired(): boolean {
    const expiration = this.getTokenExpiration();
    return !expiration || Date.now() > expiration;
  }

  setUser(user: UserProfile) {
    this.user.set(user);
  }

  clearUser() {
    this.user.set(null);
  }

  currentUser = this.user.asReadonly()
}
