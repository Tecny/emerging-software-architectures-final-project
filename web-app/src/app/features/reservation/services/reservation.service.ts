import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {Reservation, ReservationRequest} from '../models/reservation.interface';
import {map} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  createReservation(reservation: ReservationRequest) {
    return this.http.post<void>(`${this.baseUrl}/reservations/create`, reservation);
  }

  myReservations() {
    return this.http.get<Reservation[]>(`${this.baseUrl}/reservations/my-reservations`).pipe(
      map(reservations => reservations.filter(reservation => reservation.type === 'PERSONAL'))
    );
  }

  deleteReservation(id: number) {
    return this.http.delete(`${this.baseUrl}/reservations/${id}`);
  }
}
