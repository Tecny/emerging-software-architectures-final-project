import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {Room} from '../../models/room.interface';
import {TitleCasePipe} from '@angular/common';
import {TimeUtil} from '../../../../shared/utils/time.util';
import {RouterLink} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-room-info',
  imports: [
    TitleCasePipe,
    RouterLink,
    TranslatePipe
  ],
  template: `
    <h1><span>{{ 'rooms.detail.info.title' | translate }}</span></h1>
    <div class="room-info">

      <div class="room-info__header">
        <img src="{{ room.reservation.sportSpace.imageUrl }}" [alt]="'Imagen de ' + room.reservation.sportSpace.name" class="room-info__image" />
        <a [routerLink]="['/sport-spaces', room.reservation.sportSpace.id]"> {{ room.reservation.sportSpace.name }}</a>
      </div>

      <div class="room-info__content">
        <p><strong>{{ 'rooms.detail.info.gamemode' | translate }}: </strong>{{ room.reservation.sportSpace.gamemode.replaceAll('_', ' ') | titlecase}}</p>
        <p><strong>{{ 'rooms.detail.info.date' | translate }}: </strong> {{ TimeUtil.formatDate(room.reservation.gameDay) }}, {{ room.reservation.startTime }}-{{ room.reservation.endTime }}</p>
        <p class="room-info__address"> <strong>{{ 'rooms.detail.info.place' | translate }}: </strong> {{ room.reservation.sportSpace.address }}</p>
      </div>
    </div>
  `,
  styleUrl: './room-info.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoomInfoComponent {
  @Input() room!: Room;

  protected readonly TimeUtil = TimeUtil;
}
