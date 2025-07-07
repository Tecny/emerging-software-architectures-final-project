export interface TicketRequest {
  fullName: string;
  bankName: string;
  transferType: string;
  accountNumber: string;
}

export interface Ticket {
  id: number;
  userId: number;
  fullName: string;
  bankName: string;
  transferType: string;
  accountNumber: string;
  amount: number;
  status: string;
  ticketNumber: string;
}
