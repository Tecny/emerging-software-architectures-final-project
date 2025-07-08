import {ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {RoomService} from '../../services/room.service';
import {Room} from '../../models/room.interface';
import {RoomInfoComponent} from '../../components/room-info/room-info.component';
import {RoomChatComponent} from '../../components/room-chat/room-chat.component';
import {RoomPlayerListComponent} from '../../components/room-player-list/room-player-list.component';
import {Location} from '@angular/common';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-room-detail',
  imports: [
    RoomInfoComponent,
    RoomChatComponent,
    RoomPlayerListComponent,
    SpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './room-detail.component.html',
  styleUrl: './room-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoomDetailComponent implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private location = inject(Location);
  private roomService = inject(RoomService);

  room = signal<Room | null>(null);
  activeTab = signal<'chat' | 'info'>('chat');
  windowWidth = signal(window.innerWidth);

  constructor() {
    window.addEventListener('resize', () => {
      this.windowWidth.set(window.innerWidth);
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.roomService.getRoomById(id).subscribe({
      next: (room) => {
        this.room.set(room);
      }
    });
  }

  ngOnDestroy() {
    window.removeEventListener('resize', () => {
      this.windowWidth.set(window.innerWidth);
    });
    if (this.room()) {
      this.roomService.clearAccess(this.room()!.id);
    }
  }

  setTab(tab: 'chat' | 'info') {
    this.activeTab.set(tab);
  }

  goBack(): void {
    this.location.back();
  }
}
