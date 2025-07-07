import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {Subscription} from "../models/subscription.interface";

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  getSubscriptionInfo() {
    return this.http.get<Subscription>(`${this.baseUrl}/subscriptions`);
  }

  upgradeSubscription(newPlanType: string) {
    return this.http.put<{ approval_url: string }>(
      `${this.baseUrl}/subscriptions/upgrade?newPlanType=${newPlanType}`,
      {}
    );
  }
}
