import {ChangeDetectionStrategy, Component, inject, Input, OnInit, signal} from '@angular/core';
import {RoomService} from '../../services/room.service';
import {PlayerList} from '../../models/player-list.interface';
import {Room} from '../../models/room.interface';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-room-player-list',
  imports: [TranslatePipe],
  template: `
    <h1><span>{{ 'rooms.detail.playerList.title' | translate:{ count: room.playerCount } }}</span></h1>
    @if (players()) {
      <div class="player-list">
        @for (player of players(); track player.id; let i = $index) {
          <div class="player-list__row">
            <div class="player-list__info">
              <div class="player-list__avatar">
                {{ player.name.charAt(0).toUpperCase() }}
              </div>
              <div class="player-list__name">
                {{ player.name }}
              </div>
            </div>
            @if (i === 0) {
              <span class="player-list__creator" title="Creador">
                <i class="lni lni-crown-3"></i>
                <span class="creator-text">{{ 'rooms.detail.playerList.creator' | translate }}</span>
              </span>
            }
          </div>
        }
      </div>
    }
  `,
  styleUrl: './room-player-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoomPlayerListComponent implements OnInit {
  @Input() room!: Room;

  private roomService = inject(RoomService);

  players = signal<PlayerList[] | null>(null);

  ngOnInit() {
    this.getPlayerList();
  }

  getPlayerList() {
    this.roomService.getPlayerList(this.room.id).subscribe({
      next: (res) => {
        this.players.set(res);
      }
    });
  }
}
