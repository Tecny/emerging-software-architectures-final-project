import {inject, Injectable} from '@angular/core';
import {map, Observable, Subject} from 'rxjs';
import {environment} from '../../../../environment/environment';
import {HttpClient} from '@angular/common/http';
import {WebSocketService} from './web-socket.service';
import {Message} from '../models/message.interface';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private messagesSubject: Subject<MessageEvent>;
  private readonly messages$: Observable<Message>;

  private wsService = inject(WebSocketService);
  private http = inject(HttpClient);
  private baseUrl = environment.baseUrl;

  constructor() {
    this.messagesSubject = this.wsService.connect(environment.chatUrl) as Subject<MessageEvent>;
    this.messages$ = this.messagesSubject.pipe(
      map((event: MessageEvent) => {
        try {
          const data = JSON.parse(event.data);
          return {
            content: data.content,
            user: data.user,
            createdAt: data.createdAt,
            roomId: data.roomId
          } as Message;
        } catch (e) {
          throw e;
        }
      })
    );
  }

  sendMessage(chatRoomId: number, content: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/chat/rooms/${chatRoomId}/messages`, { content });
  }

  receiveMessages(): Observable<Message> {
    return this.messages$;
  }

  getChatMessages(chatRoomId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/chat/rooms/${chatRoomId}/messages`);
  }
}
