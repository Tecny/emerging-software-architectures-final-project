export interface ReservationRequest {
  id: number;
  gameDay: string;
  startTime: string;
  endTime: string;
  sportSpaceId: string;
}

interface SportSpace {
  id: number;
  name: string;
  image: string;
  price: number;
  amount: number;
  sport: string;
  gamemode: string;
  address: string;
}

interface Blockchain {
  inputHex: string;
  txHash: string;
  spaceId: number;
  userId: number;
  timestamp: string;
}

export interface Reservation {
  id: number;
  name: string;
  type: string;
  sportSpaces: SportSpace;
  blockchain: Blockchain | 'Not available';
  gameDay: string;
  startTime: string;
  endTime: string;
  status: string;
}
