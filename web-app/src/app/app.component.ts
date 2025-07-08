import {Component, computed, effect, inject, signal} from '@angular/core';
import {NavigationCancel, NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet} from '@angular/router';
import {HeaderComponent} from './core/components/header/header.component';
import {AuthService} from './auth/services/auth.service';
import {LoadingService} from './core/services/loading.service';
import {TranslateModule, TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    HeaderComponent,
    TranslateModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'dtaquito';

  private authService = inject(AuthService);
  private router = inject(Router);
  private loadingService = inject(LoadingService);

  isAuthenticated = signal(false);
  private isNavigating = signal(false);
  isLoading = signal(false);

  private currentUrl = signal('');

  private hiddenHeaderRoutes = ['correct-payment', 'error-payment', 'login', 'register', 'reset-password'];

  showHeader = computed(() =>
    !this.hiddenHeaderRoutes.some(route =>
      this.currentUrl().includes(route)
    )
  );

  constructor(private translate: TranslateService) {
    this.authService.isAuthenticated().subscribe((auth) => {
      this.isAuthenticated.set(auth);
    });

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.isNavigating.set(true);
      }

      if (
        event instanceof NavigationEnd ||
        event instanceof NavigationCancel ||
        event instanceof NavigationError
      ) {
        this.isNavigating.set(false);
        this.currentUrl.set(this.router.url);
      }
    });

    effect(() => {
      this.isLoading.set(
        this.isNavigating() || this.loadingService.isLoading()
      );
    });

    const savedLang = localStorage.getItem('language');
    if (savedLang) {
      this.translate.use(savedLang);
    }
  }
}
