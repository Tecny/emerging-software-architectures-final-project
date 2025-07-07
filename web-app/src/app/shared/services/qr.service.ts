import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../environment/environment';
import {map} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class QrService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  generateQrToken(reservationId: number) {
    return this.http.get<{ qrToken: string }>(`${this.baseUrl}/reservations/generate-qr-session`, {
      params: { reservationId }
    });
  }

  generateQRImage(token: string) {
    return this.http.get(`${this.baseUrl}/reservations/verify-qr-image`, {
      params: { token },
      responseType: 'blob'
    }).pipe(
      map((blob: Blob) => {
        return URL.createObjectURL(blob);
      })
    );
  }
}
