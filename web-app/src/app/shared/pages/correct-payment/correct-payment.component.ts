import { ChangeDetectionStrategy, Component } from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-correct-payment',
  imports: [TranslatePipe],
  template: `
    <div class="correct-payment-container">
      <div class="correct-payment-card">
        <svg viewBox="0 0 24 24" class="correct-payment-icon">
          <path fill="currentColor"
                d="M12,0A12,12,0,1,0,24,12,12.014,12.014,0,0,0,12,0Zm6.927,8.2-6.845,9.289a1.011,1.011,0,0,1-1.43.188L5.764,13.769a1,1,0,1,1,1.25-1.562l4.076,3.261,6.227-8.451A1,1,0,1,1,18.927,8.2Z">
          </path>
        </svg>
        <div class="correct-payment-center">
          <h3 class="correct-payment-title">{{ 'externalPages.correctPayment.title' | translate }}</h3>
          <p class="correct-payment-message">{{ 'externalPages.correctPayment.message' | translate }}</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .correct-payment-container {
      background-color: var(--bg-color);
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .correct-payment-card {
      background-color: var(--bg-color);
      padding: 1.5rem;
      margin-left: auto;
      margin-right: auto;
    }

    .correct-payment-icon {
      background-color: var(--text-color);
      border-color: var(--bg-color);
      color: #16a34a;
      border-radius: 50%;
      width: 4rem;
      height: 4rem;
      display: block;
      margin: 0 auto;
    }

    .correct-payment-title {
      font-size: 2rem;
      color: var(--external-pages-color);
      font-weight: 600;
      text-align: center;
    }

    .correct-payment-message {
      font-size: 1.25rem;
      text-align: center;
      color: #2ecc71;
      background-color: #ecfdf5;
      padding: 1.5rem 2rem;
      border-radius: 16px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
      max-width: 500px;
      margin: 1rem auto;
    }

    .correct-payment-center {
      text-align: center;
      padding: 0;

      h3 {
        margin-top: 0.2rem;
        margin-bottom: 1rem;
      }
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CorrectPaymentComponent {

}
