import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {Subscription} from '../../models/subscription.interface';
import {SubscriptionService} from '../../services/subscription.service';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-view-subscription',
  imports: [TranslatePipe],
  templateUrl: './view-subscription.component.html',
  styleUrl: './view-subscription.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewSubscriptionComponent implements OnInit {
  private subscriptionService = inject(SubscriptionService);

  isLoadingRequest = signal(false);
  subscriptionInfo= signal<Subscription | null>(null);

  ngOnInit(){
    this.loadSubscriptionInfo();
  }

  loadSubscriptionInfo() {
    this.subscriptionService.getSubscriptionInfo().subscribe({
      next: (user) => {
        this.subscriptionInfo.set(user);
      },
      error: () => this.subscriptionInfo.set(null),
    });
  }

  upgradeSubscription(newPlanType: string) {
    this.isLoadingRequest = signal(true);
    this.subscriptionService.upgradeSubscription(newPlanType).subscribe({
      next: (response) => {
        const approvalUrl = response.approval_url;
        const paymentWindow = window.open(approvalUrl, 'PayPal Payment', 'width=800, height=600');
        if (paymentWindow) {
          const interval = setInterval(() => {
            if (paymentWindow.closed) {
              this.isLoadingRequest.set(false);
              clearInterval(interval);
              this.loadSubscriptionInfo();
            }
          }, 1000);
        } else {
          this.isLoadingRequest.set(false);
        }
      }
    });
  }
}
