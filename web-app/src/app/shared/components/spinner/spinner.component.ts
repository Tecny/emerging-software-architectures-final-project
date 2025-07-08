import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {NgOptimizedImage} from '@angular/common';
import {ThemeService} from '../../services/theme.service';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-spinner',
  template: `
    <div class="spinner-container" [class.dark-theme]="isDarkTheme">
      <img ngSrc="assets/images/soccer-ball.png" alt="" class="soccer-ball" width="50" height="50" priority/>
      <p class="loading-text">{{ 'common.spinner.loading' | translate}}</p>
    </div>
  `,
  styles: [`
    .spinner-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 20vw;
      width: 20vw;
      max-width: 120px;
      max-height: 120px;
      min-width: 60px;
      min-height: 60px;
      text-align: center;
      margin: 2rem auto;
    }

    .soccer-ball {
      width: 100%;
      height: auto;
      animation: spin 1s linear infinite;
      opacity: 0.9;
      transition: filter 0.3s;
    }

    .dark-theme .soccer-ball {
      filter: brightness(2) invert(1);
    }

    .loading-text {
      margin-top: 10px;
      font-size: clamp(0.8rem, 3vw, 1.6rem);
      font-weight: 500;
      color: var(--text-color);
    }

    @media (max-width: 600px) {
      .spinner-container {
        width: 30vw;
        height: 30vw;
        max-width: 80px;
        max-height: 80px;
      }
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `],
  imports: [
    NgOptimizedImage,
    TranslatePipe
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpinnerComponent {
  private themeService = inject(ThemeService);

  get isDarkTheme() {
    return this.themeService.isDarkTheme;
  }
}
