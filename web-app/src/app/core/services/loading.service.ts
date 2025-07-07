import {Injectable, signal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private _isLoading = signal(false);
  isLoading = this._isLoading.asReadonly();

  start() {
    this._isLoading.set(true);
  }

  stop() {
    this._isLoading.set(false);
  }
}
