export interface UserProfile {
  id: number;
  name: string;
  email: string;
  roleType: 'PLAYER' | 'OWNER' | 'ADMIN';
  credits: number;
}
