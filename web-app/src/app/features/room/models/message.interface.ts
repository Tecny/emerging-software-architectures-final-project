export interface Message {
  content: string;
  user: User;
  createdAt: string;
  roomId: number;
}

interface User {
  id: number;
  name: string;
}
