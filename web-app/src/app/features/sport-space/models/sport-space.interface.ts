export interface SportSpace {
  id: number;
  name: string;
  imageUrl: string;
  sportId: number;
  gamemodeId: number;
  price: number;
  amount: number;
  address: string;
  description: string;
  openTime: string;
  closeTime: string;
  latitude: number;
  longitude: number;
  user: User;
}

interface User {
  id: number;
  name: string;
}
