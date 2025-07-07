import {inject, Injectable} from '@angular/core';
import {environment} from '../../../environment/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GeolocationService {
  private readonly apiKey = environment.locationIQKey;

  http = inject(HttpClient);

  reverseGeocode(lat: number, lon: number): Observable<any> {
    const url = `https://us1.locationiq.com/v1/reverse.php?key=${this.apiKey}&lat=${lat}&lon=${lon}&format=json`;
    return this.http.get(url);
  }
}
