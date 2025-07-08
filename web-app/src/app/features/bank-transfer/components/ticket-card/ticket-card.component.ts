import {ChangeDetectionStrategy, Component, computed, EventEmitter, inject, Input, Output, signal} from '@angular/core';
import {Ticket} from '../../models/ticket.interface';
import {NgClass} from '@angular/common';
import {UserStoreService} from "../../../../core/services/user-store.service";
import {BankTransferService} from "../../services/bank-transfer.service";
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {ToastrService} from 'ngx-toastr';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-ticket-card',
  imports: [
    TranslatePipe,
    NgClass,
    ModalComponent
  ],
  template: `
    <div class="ticket-card"
         [ngClass]="{
        'ticket-card--pending': ticket.status === 'PENDING',
        'ticket-card--confirmed': ticket.status === 'CONFIRMED',
        'ticket-card--deferred': ticket.status === 'DEFERRED'
      }">
      <div class="ticket-card__header">
        <p class="ticket-card__status">{{ 'tickets.card.status.' + ticket.status | translate }}</p>
        <h2>{{ 'tickets.card.fields.ticketNumber' | translate }}: {{ ticket.ticketNumber }}</h2>
      </div>
      <div class="ticket-card__content">
        <p>{{ 'tickets.card.fields.fullName' | translate }}: {{ ticket.fullName }}</p>
        <p>{{ 'tickets.card.fields.bankName' | translate }}: {{ ticket.bankName }}</p>
        <p>{{ 'tickets.card.fields.transferType' | translate }}: {{ ticket.transferType }}</p>
        <p>{{ 'tickets.card.fields.accountNumber' | translate }}: {{ ticket.accountNumber }}</p>
        <p>{{ 'tickets.card.fields.amount' | translate }}: {{ ticket.amount }} {{ 'tickets.card.fields.credits' | translate }}</p>
      </div>
      @if (this.currentUser()?.roleType === 'ADMIN') {
        <div class="ticket-card__actions">
          @if (canConfirmTicket()) {
            <button (click)="showConfirmModal = true">
              <i class="lni lni-check-circle-1"></i>
              {{ 'tickets.card.actions.confirm' | translate }}
            </button>
          }
          @if (canDeferTicket()) {
            <button (click)="showDeferModal = true">
              <i class="lni lni-alarm-1"></i>
              {{ 'tickets.card.actions.defer' | translate }}
            </button>
          }
        </div>
      }
    </div>
    @if (showConfirmModal) {
      <app-modal [width]="'400px'" [variant]="'default'" (closeModal)="handleClose()">
        <div modal-header>{{ 'tickets.card.modals.confirmTitle' | translate }}</div>
        <div modal-body>{{ 'tickets.card.modals.confirmMsg' | translate }}</div>
        <div modal-footer>
          <button type="submit" class="button-submit" (click)="confirmTicket()" [disabled]="isLoadingRequest()" >
            @if (isLoadingRequest()) {
              <span class="spinner-default"></span>
            } @else {
              {{ 'tickets.card.actions.confirm' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
    @if (showDeferModal) {
      <app-modal [width]="'400px'" [variant]="'warning'" (closeModal)="handleClose()">
        <div modal-header>{{ 'tickets.card.modals.deferTitle' | translate }}</div>
        <div modal-body>{{ 'tickets.card.modals.deferMsg' | translate }}</div>
        <div modal-footer>
          <button type="submit" class="button-submit--warning" (click)="deferTicket()" [disabled]="isLoadingRequest()" >
            @if (isLoadingRequest()) {
              <span class="spinner-warning"></span>
            } @else {
              {{ 'tickets.card.actions.confirm' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
  `,
  styleUrl: './ticket-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketCardComponent {
  @Input() ticket!: Ticket;
  @Output() ticketConfirmed = new EventEmitter<void>();
  @Output() ticketDeferred = new EventEmitter<void>();

  private userStore = inject(UserStoreService);
  private bankTransferService = inject(BankTransferService);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  currentUser = this.userStore.currentUser;
  isAdmin = computed(() => {
    const currentUser = this.currentUser();
    return currentUser && currentUser.roleType === 'ADMIN';
  });
  isLoadingRequest = signal(false);

  showConfirmModal = false;
  showDeferModal = false;

  canConfirmTicket(){
    return (this.ticket.status === 'PENDING' || this.ticket.status === 'DEFERRED') && this.isAdmin();
  }

  canDeferTicket(){
    return this.ticket.status === 'PENDING' && this.isAdmin();
  }

  confirmTicket() {
    this.isLoadingRequest.set(true);
    if (this.isAdmin()) {
      this.bankTransferService.confirmTicket(this.ticket.id).subscribe({
        next: () => {
          this.isLoadingRequest.set(false);
          this.ticketConfirmed.emit();
          this.toastService.success(
            this.translate.instant('tickets.card.toast.successConfirm'),
            this.translate.instant('toastStatus.success')
          );
        },
        error: () => {
          this.isLoadingRequest.set(false);
          this.toastService.error(
            this.translate.instant('tickets.card.toast.errorConfirm'),
            this.translate.instant('toastStatus.error')
          );
        }
      });
    } else {
      this.isLoadingRequest.set(false);
    }
  }

  deferTicket() {
    this.isLoadingRequest.set(true);
    if (this.isAdmin()) {
      this.bankTransferService.deferTicket(this.ticket.id).subscribe({
        next: () => {
          this.isLoadingRequest.set(false);
          this.ticketDeferred.emit();
          this.toastService.success(
            this.translate.instant('tickets.card.toast.successDefer'),
            this.translate.instant('toastStatus.success')
          );
        },
        error: () => {
          this.isLoadingRequest.set(false);
          this.toastService.error(
            this.translate.instant('tickets.card.toast.errorDefer'),
            this.translate.instant('toastStatus.error')
          );
        }
      });
    } else {
      this.isLoadingRequest.set(false);
    }
  }

  handleClose() {
    this.showConfirmModal = false;
    this.showDeferModal = false;
  }
}
