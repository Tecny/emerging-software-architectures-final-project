import {ChangeDetectionStrategy, Component, inject, Input} from '@angular/core';
import {Reservation} from '../../models/reservation.interface';
import {LowerCasePipe} from '@angular/common';
import {PriceUtil, TimeUtil} from '../../../../shared/utils/time.util';
import {QrViewerComponent} from "../../../../shared/components/qr-viewer/qr-viewer.component";
import {RouterLink} from '@angular/router';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-reservation-card',
  imports: [
    QrViewerComponent,
    LowerCasePipe,
    RouterLink,
    ModalComponent,
    TranslatePipe
  ],
  template: `
    <div class="reservation-card">
      <div class="reservation-card__header">
        <div class="reservation-card__header-top">
          <span class="reservation-card__title">{{ reservation.name }}</span>
          @if (currentUser()?.roleType !== 'ADMIN') {
            <button class="btn btn--blockchain" (click)="showBCModal = true">
              <i class="lni lni-ethereum-logo"></i>
            </button>
            <!-- <button class="btn btn--qr" (click)="showQRModal = true">
              <i class="fa-solid fa-qrcode"></i>
            </button> -->
          }
        </div>
        <span class="reservation-card__status badge badge--{{ reservation.status | lowercase }}">
            {{ 'reservations.card.status.' + reservation.status | translate }}
        </span>
      </div>

      <div class="reservation-card__body">
        <div class="reservation-card__details">
          <p>
            <strong>{{ 'reservations.card.fields.mode' | translate }}:</strong>
            {{ ('gamemodes.' + reservation.sportSpaces.gamemode.replace('_', '').toLowerCase()) | translate }}
          </p>
          <p>
            <strong>{{ 'reservations.card.fields.date' | translate }}:</strong>
            {{ TimeUtil.formatDate(reservation.gameDay) }}, {{ reservation.startTime }} - {{ reservation.endTime }}
          </p>
          <p>
            <strong>{{ 'reservations.card.fields.price' | translate }}:</strong>
            {{ getPrice() }} créditos
          </p>
          <p>
            <strong>{{ 'reservations.card.fields.sportSpace' | translate }}: </strong>
            <a [routerLink]="['/sport-spaces', reservation.sportSpaces.id]">
              {{ reservation.sportSpaces.name }}
            </a>
          </p>
          <p>
            <strong>{{ 'reservations.card.fields.place' | translate }}:</strong>
            {{ reservation.sportSpaces.address }}
          </p>
        </div>
      </div>
    </div>

    @if (showBCModal) {
      <app-modal [width]="'400px'" [variant]="'info'" (closeModal)="handleClose()">
        <div modal-header>{{ 'blockchain.data' | translate }}</div>
        <div modal-body>
          @if (reservation.blockchain === 'Not available') {
            <div class="reservation-card__details">
              <p>{{ 'blockchain.loading' | translate }}</p>
            </div>
          } @else {
            <div class="reservation-card__details">
              <p><strong>{{ 'blockchain.txHash' | translate }}:</strong><br> <span class="tx-hash">{{ reservation.blockchain.txHash }}</span></p>
              <p><strong>{{ 'blockchain.inputHex' | translate }}:</strong><br> <span class="input-hex">{{ reservation.blockchain.inputHex }}</span></p>
              <p><strong>{{ 'blockchain.spaceId' | translate }}:</strong> {{ reservation.blockchain.spaceId }}</p>
              <p><strong>{{ 'blockchain.userId' | translate }}:</strong> {{ reservation.blockchain.userId }}</p>
            </div>
          }
        </div>
        <div modal-footer>
          <button class="button-submit--info" (click)="handleClose()">{{ 'blockchain.accept' | translate }}</button>
        </div>
      </app-modal>
    }

    @if (showQRModal) {
      <app-qr-viewer [reservationId]="reservation.id" (close)="showQRModal = false"/>
    }
  `,
  styleUrl: './reservation-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReservationCardComponent {
  @Input() reservation!: Reservation;

  private userStore = inject(UserStoreService);

  currentUser = this.userStore.currentUser;

  showQRModal = false;
  showBCModal = false;

  getPrice(): number {
    const hours = TimeUtil.getHoursDifference(this.reservation.startTime, this.reservation.endTime);
    return PriceUtil.calculatePrice(
      this.reservation.type,
      this.reservation.sportSpaces.price,
      this.reservation.sportSpaces.amount,
      hours
    );
  }

  handleClose() {
    this.showBCModal = false;
  }

  protected readonly TimeUtil = TimeUtil;
}
