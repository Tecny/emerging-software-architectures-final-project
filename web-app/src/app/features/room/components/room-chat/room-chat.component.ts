import {ChangeDetectionStrategy, Component, inject, Input, OnDestroy, OnInit, signal, effect} from '@angular/core';
import {Room} from '../../models/room.interface';
import {ChatService} from '../../services/chat.service';
import {Message} from '../../models/message.interface';
import {Subscription} from 'rxjs';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {DatePipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-room-chat',
  imports: [
    DatePipe,
    TranslatePipe
  ],
  templateUrl: './room-chat.component.html',
  styleUrl: './room-chat.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoomChatComponent implements OnInit, OnDestroy {
  @Input() room!: Room;

  private chatService = inject(ChatService);
  private userStore = inject(UserStoreService);
  private wsSubscription?: Subscription;

  currentUser = this.userStore.currentUser;

  newMessageContent = signal('');
  messages = signal<any[]>([]);

  constructor() {
    effect(() => {
      this.messages();
      this.scrollToBottom();
    });
  }

  ngOnInit() {
    this.getRoomChat();
    this.listenToNewMessages();
  }

  ngOnDestroy() {
    this.wsSubscription?.unsubscribe();
  }

  getRoomChat(): void {
    if (!this.room) return;

    this.chatService.getChatMessages(this.room.id).subscribe((messages: Message[]) => {
      const sortedMessages = messages.sort((a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );

      this.messages.set(sortedMessages);
      this.scrollToBottom();
    });
  }

  listenToNewMessages(): void {
    this.wsSubscription = this.chatService.receiveMessages().subscribe((newMessage: Message) => {
      if (newMessage?.user && this.room && (newMessage as any).roomId === this.room.id) {
        this.messages.update(current => {
          const updated = [...current, newMessage];
          return updated.sort((a, b) =>
            new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
          );
        });
        this.scrollToBottom();
      }
    });
  }

  sendMessage(content: string): void {
    if (!content.trim()) return;

    this.chatService.sendMessage(this.room.id, content).subscribe({
      next: () => {
        this.newMessageContent.set('');
        this.scrollToBottom();
      }
    });
  }



  scrollToBottom(): void {
    setTimeout(() => {
      const chatContainer = document.getElementById('chat-container');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    });
  }
}
