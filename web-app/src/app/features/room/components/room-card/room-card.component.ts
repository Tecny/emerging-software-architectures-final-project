import {ChangeDetectionStrategy, Component, EventEmitter, inject, Input, OnInit, Output, signal} from '@angular/core';
import {Room} from '../../models/room.interface';
import {LowerCasePipe} from '@angular/common';
import {PriceUtil, TimeUtil} from '../../../../shared/utils/time.util';
import {RoomService} from '../../services/room.service';
import {Router, RouterLink} from '@angular/router';
import {ReservationService} from '../../../reservation/services/reservation.service';
import {QrViewerComponent} from '../../../../shared/components/qr-viewer/qr-viewer.component';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {ToastrService} from 'ngx-toastr';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-room-card',
  imports: [
    RouterLink,
    QrViewerComponent,
    LowerCasePipe,
    ModalComponent,
    TranslatePipe
  ],
  template: `
    <div class="room-card">
      <div class="room-card__header">
        <div class="room-card__header-top">
          <span class="room-card__title">{{ room.reservation.reservationName }}</span>
          <span class="room-card__players">{{ room.playerCount }}</span>
        </div>
        @if (showStatus) {
          <span class="room-card__status badge badge--{{ room.reservation.status | lowercase }}">
            {{ 'rooms.card.status.' + room.reservation.status | translate }}
          </span>
        }
      </div>

      <div class="room-card__body">
        <div class="room-card__details">
          <p><strong>{{ 'rooms.card.fields.mode' | translate }}:</strong> {{ ('gamemodes.' + room.reservation.sportSpace.gamemode.replace('_', '').toLowerCase()) | translate }}
          </p>
          <p><strong>{{ 'rooms.card.fields.date' | translate }}:</strong> {{ TimeUtil.formatDate(room.reservation.gameDay) }}
            , {{ room.reservation.startTime }} - {{ room.reservation.endTime }}</p>
          <p><strong>{{ 'rooms.card.fields.advance' | translate }}:</strong> {{ getAmount() }} {{ 'spaces.card.priceUnit' | translate }}</p>
          <p><strong>{{ 'rooms.card.fields.sportSpace' | translate }}: </strong>
            <a
              [routerLink]="['/sport-spaces', room.reservation.sportSpace.id]">{{ room.reservation.sportSpace.name }}</a>
          </p>
          <p><strong>{{ 'rooms.card.fields.place' | translate }}:</strong> {{ room.reservation.sportSpace.address }}</p>
        </div>
      </div>

      @if (currentUser()?.roleType === 'PLAYER') {
        <div class="room-card__actions">
          @if (isMember() !== null) {
            @if (isMember()) {
              <button class="btn btn--primary" (click)="viewRoom()">
                <i class="lni lni-location-arrow-right"></i> <span class="btn-text">{{ 'rooms.card.actions.goToRoom' | translate }}</span>
              </button>
              @if (isRoomCreator()) {
                <button class="btn btn--danger" (click)="showDeleteModal = true">
                  <i class="lni lni-trash-3"></i> <span class="btn-text">{{ 'rooms.card.actions.delete' | translate }}</span>
                </button>
                @if (room.reservation.status === 'CONFIRMED') {
                  <button class="btn btn--blockchain" (click)="showBCModal = true">
                    <i class="lni lni-ethereum-logo"></i>
                  </button>
                  <button class="btn btn--secondary" (click)="showQRModal = true">
                    <i class="fa-solid fa-qrcode"></i>
                  </button>
                }
              } @else {
                <button class="btn btn--warning" (click)="showLeaveModal = true">
                  <i class="lni lni-exit"></i> <span class="btn-text">{{ 'rooms.card.actions.leave' | translate }}</span>
                </button>
              }
            } @else {
              <button class="btn btn--success" (click)="showJoinModal = true">
                <i class="lni lni-enter"></i> <span class="btn-text">{{ 'rooms.card.actions.join' | translate }}</span>
              </button>
            }
          }
        </div>
      }
    </div>
    @if (showJoinModal) {
      <app-modal [width]="'400px'" [variant]="'default'" (closeModal)="handleClose()">
        <div modal-header>{{ 'rooms.card.modals.joinTitle' | translate }}</div>
        <div modal-body>{{ 'rooms.card.modals.joinMsg' | translate:{ amount: getAmount() } }}</div>
        <div modal-footer>
          <button type="submit" class="button-submit" (click)="joinRoom()" [disabled]="isLoadingRequest()" >
            @if (isLoadingRequest()) {
              <span class="spinner-default"></span>
            } @else {
              {{ 'rooms.card.actions.join' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
    @if (showLeaveModal) {
      <app-modal [width]="'400px'" [variant]="'warning'" (closeModal)="handleClose()">
        <div modal-header>{{ 'rooms.card.modals.leaveTitle' | translate }}</div>
        <div modal-body>{{ 'rooms.card.modals.leaveMsg' | translate }}</div>
        <div modal-footer>
          <button type="submit" class="button-submit--warning" (click)="leaveRoom()" [disabled]="isLoadingRequest()">
            @if (isLoadingRequest()) {
              <span class="spinner-warning"></span>
            } @else {
              {{ 'rooms.card.actions.leave' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
    @if (showDeleteModal) {
      <app-modal [width]="'400px'" [variant]="'danger'" (closeModal)="handleClose()">
        <div modal-header>{{ 'rooms.card.modals.deleteTitle' | translate }}</div>
        <div modal-body>{{ 'rooms.card.modals.deleteMsg' | translate }}</div>
        <div modal-footer>
          <button type="submit" class="button-submit--danger" (click)="deleteRoom()" [disabled]="isLoadingRequest()">
            @if (isLoadingRequest()) {
              <span class="spinner-danger"></span>
            } @else {
              {{ 'rooms.card.actions.delete' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
    @if (showBCModal) {
      <app-modal [width]="'400px'" [variant]="'info'" (closeModal)="handleClose()">
        <div modal-header>{{ 'blockchain.data' | translate }}</div>
        <div modal-body>
          @if (room.reservation.blockchain === 'Not available') {
            <div class="reservation-card__details">
              <p>{{ 'blockchain.loading' | translate }}</p>
            </div>
          } @else {
            <div class="reservation-card__details">
              <p><strong>{{ 'blockchain.txHash' | translate }}:</strong><br> <span class="tx-hash">{{ room.reservation.blockchain.txHash }}</span></p>
              <p><strong>{{ 'blockchain.inputHex' | translate }}:</strong><br> <span class="input-hex">{{ room.reservation.blockchain.inputHex }}</span></p>
              <p><strong>{{ 'blockchain.spaceId' | translate }}:</strong> {{ room.reservation.blockchain.spaceId }}</p>
              <p><strong>{{ 'blockchain.userId' | translate }}:</strong> {{ room.reservation.blockchain.userId }}</p>
            </div>
          }
        </div>
        <div modal-footer>
          <button class="button-submit--info" (click)="handleClose()">{{ 'blockchain.accept' | translate }}</button>
        </div>
      </app-modal>
    }
    @if (showQRModal) {
      <app-qr-viewer [reservationId]="room.reservation.id" (close)="showQRModal = false"/>
    }
  `,
  styleUrl: './room-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RoomCardComponent implements OnInit {
  @Input() room!: Room;
  @Input() showStatus: boolean = false;
  @Output() roomEvent = new EventEmitter<void>();

  protected readonly TimeUtil = TimeUtil;

  private userStore = inject(UserStoreService);
  private router = inject(Router);
  private roomService = inject(RoomService);
  private reservationService = inject(ReservationService);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  currentUser = this.userStore.currentUser;
  isMember = signal<boolean | null>(null);
  isRoomCreator = signal<boolean | null>(null);
  isLoadingRequest = signal<boolean>(false);

  showJoinModal = false;
  showLeaveModal = false;
  showDeleteModal = false;
  showQRModal = false;
  showBCModal = false;

  ngOnInit(): void {
    this.checkRoomAccess();
  }

  getAmount(): number {
    const hours = TimeUtil.getHoursDifference(this.room.reservation.startTime, this.room.reservation.endTime);
    return PriceUtil.calculatePrice(
      'COMMUNITY',
      this.room.reservation.sportSpace.price,
      this.room.reservation.sportSpace.amount,
      hours
    );
  }

  viewRoom() {
    this.roomService.allowAccess(this.room.id);
    this.router.navigate(['/rooms', this.room.id]).then();
  }

  checkRoomAccess() {
    if (this.room?.id) {
      this.roomService.userRoomStatus(this.room.id).subscribe({
        next: (res) => {
          this.isMember.set(res.isMember);
          this.isRoomCreator.set(res.isRoomCreator);
        },
        error: () => {
          this.isMember.set(false);
          this.isRoomCreator.set(false);
        }
      });
    }
  }

  joinRoom() {
    this.isLoadingRequest.set(true);
    this.roomService.joinRoom(this.room.id).subscribe({
      next: () => {
        this.isLoadingRequest.set(false);
        this.handleClose();
        this.roomService.allowAccess(this.room.id);
        this.router.navigate(['/rooms', this.room.id]).then();
        this.toastService.success(
          this.translate.instant('rooms.card.toast.successJoin'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingRequest.set(false);
        this.toastService.error(
          this.translate.instant('rooms.card.toast.errorJoin'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  leaveRoom() {
    this.isLoadingRequest.set(true);
    this.roomService.leaveRoom(this.room.id).subscribe({
      next: () => {
        this.isLoadingRequest.set(false);
        this.handleClose();
        this.roomService.clearAccess(this.room.id);
        this.roomEvent.emit();
        this.toastService.success(
          this.translate.instant('rooms.card.toast.successLeave'),
          this.translate.instant('toastStatus.success')
        );
        this.checkRoomAccess();
      },
      error: () => {
        this.isLoadingRequest.set(true);
        this.toastService.error(
          this.translate.instant('rooms.card.toast.errorLeave'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  deleteRoom() {
    this.isLoadingRequest.set(true);
    this.reservationService.deleteReservation(this.room.reservation.id).subscribe({
      next: () => {
        this.isLoadingRequest.set(false);
        this.handleClose();
        this.checkRoomAccess();
        this.roomEvent.emit();
        this.toastService.success(
          this.translate.instant('rooms.card.toast.successDelete'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingRequest.set(false);
        this.toastService.error(
          this.translate.instant('rooms.card.toast.errorDelete'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  handleClose() {
    this.showDeleteModal = false;
    this.showJoinModal = false;
    this.showLeaveModal = false;
    this.showQRModal = false;
    this.showBCModal = false;
  }
}
