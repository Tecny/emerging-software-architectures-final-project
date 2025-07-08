import {ChangeDetectionStrategy, Component, computed, inject, Input, OnInit, signal} from '@angular/core';
import {SportSpaceService} from '../../services/sport-space.service';
import {ReservationService} from '../../../reservation/services/reservation.service';
import {NgClass} from '@angular/common';
import {SportSpace} from '../../models/sport-space.interface';
import {FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {Router} from '@angular/router';
import {TimeUtil} from '../../../../shared/utils/time.util';
import {ReservationRequest} from '../../../reservation/models/reservation.interface';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {ToastrService} from 'ngx-toastr';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {TranslateService, TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-sport-space-availability',
  imports: [
    NgClass,
    ModalComponent,
    ReactiveFormsModule,
    SpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './sport-space-availability.component.html',
  styleUrl: './sport-space-availability.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SportSpaceAvailabilityComponent implements OnInit {
  @Input() sportSpace!: SportSpace;

  protected readonly TimeUtil = TimeUtil;

  private userStore = inject(UserStoreService);
  private sportSpaceService = inject(SportSpaceService);
  private reservationService = inject(ReservationService);
  private router = inject(Router);
  private fb = inject(NonNullableFormBuilder);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  availabilityMap: Record<string, string[]> = {};
  timeSlots: string[] = [];
  weekDays: string[] = [];
  selectedGameDay: string = '';
  selectedStartTime: string = '';
  selectedEndTime: string = '';
  showError = false;

  isLoadingAvailability = signal<boolean>(true);
  isLoadingSubmitRequest = signal(false);
  currentUser = this.userStore.currentUser;
  reservationType = signal<string>('');
  selectedSlots = signal<{ gameDay: string; startTime: string; endTime: string }[]>([]);

  totalCost = computed(() => {
    const slots = this.selectedSlots();
    if (slots.length === 0) return 0;

    const type = this.reservationType();

    switch (type) {
      case 'PERSONAL':
        return Math.trunc((this.sportSpace.price * slots.length) / 2);
      case 'COMMUNITY':
        return Math.trunc(this.sportSpace.amount * slots.length);
      default:
        return 0;
    }
  });

  reservationForm!: FormGroup;
  showReservationModal = false;

  ngOnInit(): void {
    this.generateTimeSlots();
    this.fetchAvailability();
    this.initForm();

    this.reservationForm.get('type')?.valueChanges.subscribe((type) => {
      this.reservationType.set(type);
    });
  }

  initForm() {
    this.reservationForm = this.fb.group({
      gameDay: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      sportSpacesId: [this.sportSpace.id, Validators.required],
      type: ['', Validators.required],
      reservationName: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(25)]],
    });
  }

  generateTimeSlots() {
    const start = parseInt(this.sportSpace.openTime.split(':')[0], 10);
    const end = parseInt(this.sportSpace.closeTime.split(':')[0], 10);
    this.timeSlots = Array.from({ length: end - start }, (_, i) => {
      const hour = start + i;
      return `${hour.toString().padStart(2, '0')}:00`;
    });
  }

  fetchAvailability() {
    this.isLoadingAvailability.set(true);
    this.sportSpaceService.checkAvailability(this.sportSpace.id).subscribe({
      next: (res: any) => {
        this.availabilityMap = res.weeklyAvailability;
        const today = new Date();
        const todayYMD = [today.getFullYear(), today.getMonth() + 1, today.getDate()];
        this.weekDays = Object.keys(res.weeklyAvailability)
          .filter(dateStr => {
            const [year, month, day] = dateStr.split('-').map(Number);
            const hasHours = (res.weeklyAvailability[dateStr] ?? []).length > 0;
            if (!hasHours) return false;
            if (year > todayYMD[0]) return true;
            if (year < todayYMD[0]) return false;
            if (month > todayYMD[1]) return true;
            if (month < todayYMD[1]) return false;
            return day >= todayYMD[2];
          })
          .sort();
        this.isLoadingAvailability.set(false);
      },
      error: () => {
        this.isLoadingAvailability.set(false);
      }
    });
  }

  isAvailable(date: string, hour: string): boolean {
    return this.availabilityMap[date]?.includes(hour) ?? false;
  }

  isPast(date: string, hour: string): boolean {
    const now = new Date();
    const [year, month, day] = date.split('-').map(Number);
    const [h, m] = hour.split(':').map(Number);
    const slotDate = new Date(year, month - 1, day, h, m);

    return slotDate < now;
  }

  onSlotClick(gameDay: string, time: string): void {

    if(this.currentUser()?.roleType === 'OWNER') {
      this.toastService.warning(
        this.translate.instant('spaces.detail.availability.toast.ownerNotAllowed'),
        this.translate.instant('toastStatus.warning')
      );
      return;
    }

    if (!this.isAvailable(gameDay, time)) return;

    const currentSlots = this.selectedSlots();

    if (currentSlots.length > 0 && currentSlots[0].gameDay !== gameDay) {
      this.toastService.warning(
        this.translate.instant('spaces.detail.availability.toast.sameDayOnly'),
        this.translate.instant('toastStatus.warning')
      );
      return;
    }

    const selectedIndex = currentSlots.findIndex(slot => slot.gameDay === gameDay && slot.startTime === time);

    let newSlots = [...currentSlots];

    if (selectedIndex === -1) {
      if (currentSlots.length >= 2) {
        this.toastService.warning(
          this.translate.instant('spaces.detail.availability.toast.maxTwoHours'),
          this.translate.instant('toastStatus.warning')
        );
        return;
      }

      if (currentSlots.length === 0) {
        newSlots.push({ gameDay: gameDay, startTime: time, endTime: time });
      } else {
        const lastSelected = currentSlots[currentSlots.length - 1];
        const firstSelected = currentSlots[0];
        const selectedTimeIndex = this.timeSlots.indexOf(time);
        const lastEndTimeIndex = this.timeSlots.indexOf(lastSelected.endTime);
        const firstStartTimeIndex = this.timeSlots.indexOf(firstSelected.startTime);

        if (selectedTimeIndex === lastEndTimeIndex + 1 || selectedTimeIndex === firstStartTimeIndex - 1) {
          newSlots.push({ gameDay: gameDay, startTime: time, endTime: time });
          newSlots.sort((a, b) => this.timeSlots.indexOf(a.startTime) - this.timeSlots.indexOf(b.startTime));
        } else {
          this.toastService.warning(
            this.translate.instant('spaces.detail.availability.toast.consecutiveHours'),
            this.translate.instant('toastStatus.warning')
          );
          return;
        }
      }
    } else {
      newSlots.splice(selectedIndex, 1);

      for (let i = 1; i < newSlots.length; i++) {
        const prevIndex = this.timeSlots.indexOf(newSlots[i - 1].endTime);
        const currentIndex = this.timeSlots.indexOf(newSlots[i].startTime);

        if (currentIndex !== prevIndex + 1) {
          this.toastService.warning(
            this.translate.instant('spaces.detail.availability.toast.remainingConsecutive'),
            this.translate.instant('toastStatus.warning')
          );
          return;
        }
      }
    }

    this.selectedSlots.set(newSlots);
    this.showError = false;
  }

  isSelected(date: string, time: string): boolean {
    return this.selectedSlots().some(slot => slot.gameDay === date && slot.startTime === time);
  }

  confirmHours(): void {
    const slots = this.selectedSlots();
    if (slots.length > 0) {
      const firstSlot = slots[0];
      const lastSlot = slots[slots.length - 1];

      this.selectedGameDay = firstSlot.gameDay;
      this.selectedStartTime = firstSlot.startTime;

      const lastEndTimeIndex = this.timeSlots.indexOf(lastSlot.endTime);
      if (lastEndTimeIndex === this.timeSlots.length - 1) {
        const [hour, minute] = lastSlot.endTime.split(':').map(Number);
        const nextHour = (hour + 1).toString().padStart(2, '0');
        this.selectedEndTime = `${nextHour}:${minute.toString().padStart(2, '0')}`;
      } else {
        this.selectedEndTime = this.timeSlots[lastEndTimeIndex + 1];
      }

      this.reservationForm.patchValue({
        gameDay: this.selectedGameDay,
        startTime: this.selectedStartTime,
        endTime: this.selectedEndTime
      });

      this.showReservationModal = true;
    } else {
      this.toastService.warning(
        this.translate.instant('spaces.detail.availability.toast.selectAtLeastOne'),
        this.translate.instant('toastStatus.warning')
      );
    }
  }

  closeReservationModal() {
    this.showReservationModal = false;
    this.reservationForm.reset();
    this.selectedSlots.set([]);
  }

  submitReservation() {

    if (!this.reservationForm.valid) {
      this.reservationForm.markAllAsTouched();
      return;
    }

    const reservationData: ReservationRequest = this.reservationForm.getRawValue();

    this.isLoadingSubmitRequest.set(true);
    this.reservationService.createReservation(reservationData).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.selectedSlots.set([]);
        this.closeReservationModal();
        this.router.navigate(['/reservations']).then();
        this.toastService.success(
          this.translate.instant('spaces.detail.availability.toast.reservationCreated'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: (err) => {
        this.isLoadingSubmitRequest.set(false);
        if (
          err.status === 400 &&
          err.error?.message === 'User does not have enough credits to create a reservation'
        ) {
          this.toastService.error(
            this.translate.instant('spaces.detail.availability.toast.notEnoughCredits'),
            this.translate.instant('toastStatus.error')
          );
        } else {
          this.toastService.error(
            this.translate.instant('spaces.detail.availability.toast.reservationError'),
            this.translate.instant('toastStatus.error')
          );
        }
      },
    });
  }
}
