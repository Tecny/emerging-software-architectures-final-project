import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {Ticket, TicketRequest} from '../models/ticket.interface';

@Injectable({
  providedIn: 'root'
})
export class BankTransferService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  createTicket(ticketRequest: TicketRequest) {
    return this.http.post<void>(`${this.baseUrl}/bank-transfer/create`, ticketRequest);
  }

  getAllTickets() {
    return this.http.get<Ticket[]>(`${this.baseUrl}/bank-transfer/all`);
  }

  getTicketsByOwner() {
    return this.http.get<Ticket[]>(`${this.baseUrl}/bank-transfer/owner`);
  }

  confirmTicket(id: number) {
    return this.http.patch<void>(`${this.baseUrl}/bank-transfer/confirm/${id}`, {});
  }

  deferTicket(id: number) {
    return this.http.patch<void>(`${this.baseUrl}/bank-transfer/defer/${id}`, {});
  }
}
