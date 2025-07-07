import {inject, Injectable} from '@angular/core';
import {Room} from '../models/room.interface';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {PlayerList} from '../models/player-list.interface';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  private allowedRoomAccess = new Set<number>();

  allowAccess(roomId: number) {
    this.allowedRoomAccess.add(roomId);
  }

  hasAccess(roomId: number): boolean {
    return this.allowedRoomAccess.has(roomId);
  }

  clearAccess(roomId: number) {
    this.allowedRoomAccess.delete(roomId);
  }

  getAllRooms() {
    return this.http.get<Room[]>(`${this.baseUrl}/rooms/all`);
  }

  getRoomById(id: number) {
    return this.http.get<Room>(`${this.baseUrl}/rooms/${id}`);
  }

  joinRoom(id: number) {
    return this.http.post(`${this.baseUrl}/player-lists/join/${id}`, {});
  }

  leaveRoom(id: number) {
    return this.http.delete(`${this.baseUrl}/player-lists/leave/${id}`, {});
  }

  getPlayerList(roomId: number) {
    return this.http.get<PlayerList[]>(`${this.baseUrl}/player-lists/room/${roomId}`);
  }

  userRoomStatus(roomId: number) {
    return this.http.get<{ isMember: boolean, isRoomCreator: boolean }>(`${this.baseUrl}/player-lists/${roomId}/user-room-status`);
  }

  getMyRooms() {
    return this.http.get<Room[]>(`${this.baseUrl}/rooms/my-rooms`);
  }

  getRoomsJoined() {
    return this.http.get<Room[]>(`${this.baseUrl}/rooms/my-join-rooms`);
  }

  getRoomsBySportspaces(){
    return this.http.get<Room[]>(`${this.baseUrl}/rooms/my-rooms-by-spaces`);
  }
}
