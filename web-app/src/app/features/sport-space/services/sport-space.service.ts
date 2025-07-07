import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {SportSpace} from '../models/sport-space.interface';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SportSpaceService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  getSportSpaceById(id: number) {
    return this.http.get<SportSpace>(`${this.baseUrl}/sport-spaces/${id}`);
  }

  getSportSpaces() {
    return this.http.get<SportSpace[]>(`${this.baseUrl}/sport-spaces/all`);
  }

  getMySportSpaces() {
    return this.http.get<SportSpace[]>(`${this.baseUrl}/sport-spaces/my-space`);
  }

  canAddSportSpace() {
    return this.http.get<{ canAdd: boolean }>(`${this.baseUrl}/sport-spaces/can-add-sport-space`);
  }

  createSportSpace(formData: FormData) {
    return this.http.post<void>(`${this.baseUrl}/sport-spaces/create`, formData);
  }

  checkAvailability(id: number) {
    return this.http.get(`${this.baseUrl}/sport-spaces/${id}/availability`);
  }

  deleteSportSpace(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/sport-spaces/${id}`);
  }

  getMetrics(id: number, year: string): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(`${this.baseUrl}/sport-spaces/get-number/${id}/amount-people?currentYear=${year}`);
  }
}
