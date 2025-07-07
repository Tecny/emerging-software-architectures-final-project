interface SportSpace {
  id: number;
  name: string;
  imageUrl: string;
  address: string;
  sportType: string;
  gamemode: string;
  price: number;
  amount: number;
}

interface Blockchain {
  inputHex: string;
  txHash: string;
  spaceId: number;
  userId: number;
  timestamp: string;
}

interface Reservation {
  id: number;
  startTime: string;
  endTime: string;
  gameDay: string;
  userName: string;
  reservationName: string;
  userId: number;
  status: string;
  blockchain: Blockchain | 'Not available';
  sportSpace: SportSpace;
}

export interface Room {
  id: number;
  playerCount: string;
  reservation: Reservation;
}
