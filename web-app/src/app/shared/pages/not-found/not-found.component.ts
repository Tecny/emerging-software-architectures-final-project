import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterModule, TranslatePipe],
  template: `
    <div class="not-found-container">
      <div class="not-found-card">
        <svg viewBox="0 0 24 24" class="not-found-icon">
          <path fill="currentColor"
                d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
        </svg>
        <div class="not-found-center">
          <h3 class="not-found-title">{{ 'externalPages.notFound.title' | translate }}</h3>
          <p class="not-found-message">{{ 'externalPages.notFound.message' | translate }}</p>
          <a routerLink="/home" class="not-found-btn">{{ 'externalPages.notFound.button' | translate }}</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      background-color: var(--bg-color);
      margin-top: 3rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .not-found-card {
      background-color: var(--bg-color);
      padding: 1.5rem;
      margin-left: auto;
      margin-right: auto;
    }

    .not-found-icon {
      color: #f59e42;
      width: 4rem;
      height: 4rem;
      display: block;
      margin: 0 auto;
    }

    .not-found-title {
      font-size: 2rem;
      color: var(--external-pages-color);
      font-weight: 600;
      text-align: center;
      margin-top: 0.2rem;
      margin-bottom: 1rem;
    }

    .not-found-message {
      font-size: 1.25rem;
      text-align: center;
      color: #f59e42;
      background-color: #fff7ed;
      padding: 1.5rem 2rem;
      border-radius: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      max-width: 500px;
      margin: 1rem auto 2rem auto;
    }

    .not-found-center {
      text-align: center;
      padding: 0;
    }

    .not-found-btn {
      display: inline-block;
      margin-top: 1rem;
      padding: 0.75rem 2rem;
      background-color: #f59e42;
      color: #fff;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 500;
      text-decoration: none;
      cursor: pointer;
      transition: background 0.2s;
    }
    .not-found-btn:hover {
      background-color: #d97706;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NotFoundComponent {}
