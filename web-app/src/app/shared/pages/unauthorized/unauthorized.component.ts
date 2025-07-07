import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterModule, TranslatePipe],
  template: `
    <div class="unauthorized-container">
      <div class="unauthorized-card">
        <svg viewBox="0 0 24 24" class="unauthorized-icon">
          <path fill="currentColor"
                d="M12 17a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm6-7V8a6 6 0 1 0-12 0v2a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2zm-8-2a4 4 0 1 1 8 0v2H6V8zm10 11H6v-7h12v7z"/>
        </svg>
        <div class="unauthorized-center">
          <h3 class="unauthorized-title">{{ 'externalPages.unauthorized.title' | translate }}</h3>
          <p class="unauthorized-message">{{ 'externalPages.unauthorized.message' | translate }}</p>
          <a routerLink="/home" class="unauthorized-btn">{{ 'externalPages.unauthorized.button' | translate }}</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .unauthorized-container {
      background-color: var(--bg-color);
      margin-top: 3rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .unauthorized-card {
      background-color: var(--bg-color);
      padding: 1.5rem;
      margin-left: auto;
      margin-right: auto;
    }

    .unauthorized-icon {
      color: #f43f5e;
      width: 4rem;
      height: 4rem;
      display: block;
      margin: 0 auto;
    }

    .unauthorized-title {
      font-size: 2rem;
      color: var(--external-pages-color);
      font-weight: 600;
      text-align: center;
      margin-top: 0.2rem;
      margin-bottom: 1rem;
    }

    .unauthorized-message {
      font-size: 1.25rem;
      text-align: center;
      color: #f43f5e;
      background-color: #fef2f2;
      padding: 1.5rem 2rem;
      border-radius: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      max-width: 500px;
      margin: 1rem auto 2rem auto;
    }

    .unauthorized-center {
      text-align: center;
      padding: 0;
    }

    .unauthorized-btn {
      display: inline-block;
      margin-top: 1rem;
      padding: 0.75rem 2rem;
      background-color: #f43f5e;
      color: #fff;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      transition: background 0.2s;
    }
    .unauthorized-btn:hover {
      background-color: #be123c;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UnauthorizedComponent {}
