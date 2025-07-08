import {Component, computed, HostListener, inject, signal} from '@angular/core';
import {UserStoreService} from '../../services/user-store.service';
import {AuthService} from '../../../auth/services/auth.service';
import {RouterLink, RouterLinkActive} from '@angular/router';
import {ThemeService} from '../../../shared/services/theme.service';
import {TranslateModule, TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    RouterLinkActive,
    TranslateModule,
    TranslatePipe
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {

  @HostListener('window:resize')
  onResize() {
    if (window.innerWidth > 650 && this.isMenuOpen) {
      this.isMenuOpen = false;
    }
  }

  private userStore = inject(UserStoreService);
  private authService = inject(AuthService);
  private themeService = inject(ThemeService);

  currentUser = this.userStore.currentUser;

  isAuthenticated = signal(false);
  role = computed(() => this.currentUser()?.roleType ?? null);

  isMenuOpen = false;

  constructor() {
    this.authService.isAuthenticated().subscribe(auth => {
      this.isAuthenticated.set(auth);
    });
  }

  logout() {
    this.userStore.clearUser();
    this.authService.logout();
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenuOnNavigate() {
    this.isMenuOpen = false;
  }

  get isDarkTheme() {
    return this.themeService.isDarkTheme;
  }
}
