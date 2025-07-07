import {
  ChangeDetectionStrategy,
  Component,
  computed, EventEmitter,
  inject,
  Input, Output,
  signal
} from '@angular/core';
import {SportSpace} from '../../models/sport-space.interface';
import {RouterLink} from '@angular/router';
import {SportSpaceService} from '../../services/sport-space.service';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {gamemodeIdToLabelMap} from '../../../../shared/models/sport-space.constants';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {ToastrService} from 'ngx-toastr';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
@Component({
  selector: 'app-sport-space-card',
  imports: [
    RouterLink,
    ModalComponent,
    TranslatePipe
  ],
  template: `
    <div class="sportspace-card" [routerLink]="['/sport-spaces', sportSpace.id]">
      <div class="sportspace-card__image-container">
        <img src="{{ sportSpace.imageUrl }}" alt="{{ sportSpace.name }}" class="sportspace-card__image"/>

        <div class="sportspace-card__type-badge">
          {{ sportIdToEmojiMap[sportSpace.sportId] }}
          {{ String(gamemodeIdToLabelMap[sportSpace.gamemodeId]) | translate }}
        </div>

        @if (isOwner()) {
          <button class="sportspace-card__dashboard-btn" [routerLink]="['/sport-spaces', sportSpace.id, 'dashboard']"
                  (click)="$event.stopPropagation()">
            <i class="lni lni-bar-chart-4"></i>
          </button>
          <button class="sportspace-card__delete-btn" (click)="handleOpen(); $event.stopPropagation()">
            <i class="lni lni-trash-3"></i>
          </button>
        }

        <div class="sportspace-card__price-tag">
          {{ sportSpace.price }} {{ 'spaces.card.priceUnit' | translate }}
        </div>
      </div>

      <div class="sportspace-card__details">
        <h2 class="sportspace-card__title">{{ sportSpace.name }}</h2>
        <p class="sportspace-card__address">
          <i class="lni lni-map-marker-5"></i>
          {{ sportSpace.address }}
        </p>
      </div>
    </div>
    @if (showModal) {
      <app-modal [width]="'400px'" [variant]="'danger'" (closeModal)="handleClose()">
        <div modal-header>{{ 'spaces.card.deleteTitle' | translate }}</div>
        <div modal-body>{{ 'spaces.card.deleteMsg' | translate }}</div>
        <div modal-footer>
          <button class="button-submit--danger" (click)="confirmDelete()" [disabled]="isLoadingRequest()">
            @if (isLoadingRequest()) {
              <span class="spinner-danger"></span>
            } @else {
              {{ 'spaces.card.delete' | translate }}
            }
          </button>
        </div>
      </app-modal>
    }
  `,
  styleUrl: './sport-space-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SportSpaceCardComponent {
  @Input() sportSpace!: SportSpace;
  @Output() spaceDeleted = new EventEmitter<void>();

  private sportSpaceService = inject(SportSpaceService);
  private userStore = inject(UserStoreService);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  sportIdToEmojiMap: { [key: number]: string } = {
    1: '⚽',
    2: '🎱'
  };

  showModal = false;

  isLoadingRequest = signal(false);
  currentUser = this.userStore.currentUser;
  isOwner = computed(() => {
    const currentUser = this.currentUser();
    return currentUser && this.sportSpace
      ? currentUser.id === this.sportSpace.user.id
      : false;
  });

  handleOpen(): void {
    this.showModal = true;
  }

  handleClose(): void {
    this.showModal = false;
  }

  confirmDelete(): void {
    if (this.sportSpace) {
      this.isLoadingRequest.set(true);
      this.sportSpaceService.deleteSportSpace(this.sportSpace.id).subscribe({
        next: () => {
          this.isLoadingRequest.set(false);
          this.handleClose();
          this.spaceDeleted.emit();
          this.toastService.success(
            this.translate.instant('spaces.card.deleteSuccess'),
            this.translate.instant('toastStatus.success')
          );
        },
        error: () => {
          this.isLoadingRequest.set(false);
          this.toastService.error(
            this.translate.instant('spaces.card.deleteError'),
            this.translate.instant('toastStatus.error')
          );
        }
      });
    }
  }

  protected readonly gamemodeIdToLabelMap = gamemodeIdToLabelMap;
  protected readonly String = String;
}
