import {ChangeDetectionStrategy, Component, computed, inject, OnInit, signal} from '@angular/core';
import {BankTransferService} from '../../services/bank-transfer.service';
import {FormGroup, FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Ticket, TicketRequest} from '../../models/ticket.interface';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {customAccountNumberLengthByBank} from '../../../../shared/validators/banks.validator';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {TicketCardComponent} from '../../components/ticket-card/ticket-card.component';
import {ToastrService} from 'ngx-toastr';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {UserRole} from '../../../../core/models/user.role.enum';
import {TimeUtil} from '../../../../shared/utils/time.util';
import {ProfileService} from '../../../profile/services/profile.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-list-tickets',
  imports: [
    FormsModule,
    ModalComponent,
    ReactiveFormsModule,
    TicketCardComponent,
    SpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './list-tickets.component.html',
  styleUrl: './list-tickets.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListTicketsComponent implements OnInit {
  private bankTransferService = inject(BankTransferService);
  private userStore = inject(UserStoreService);
  private profileService = inject(ProfileService);
  private fb = inject(NonNullableFormBuilder);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  userRole = this.userStore.getRoleFromToken();
  userCredits = 0;

  activeTab = signal<'pending' | 'confirmed'>('pending');
  tickets = signal<Ticket[] | null>(null);
  filteredTickets = computed(() => {
    const currentTab = this.activeTab();
    const allTickets = this.tickets() || [];
    return allTickets.filter(ticket =>
      ticket.status === (currentTab === 'pending' ? 'PENDING' : 'CONFIRMED')
    );
  });
  isLoadingSubmitRequest = signal(false);

  bankType: 'asociado' | 'otro' = 'asociado';
  associatedBanks = ['BCP', 'BBVA', 'Interbank'];

  ticketForm!: FormGroup;
  showTicketModal = false;

  ngOnInit() {
    this.initForm();
    this.loadUserCredits();
    this.loadTickets();
  }

  initForm() {
    this.ticketForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(5)]],
      bankName: ['', Validators.required],
      accountNumber: ['', [Validators.required, Validators.pattern(/^\d+$/)]],
      transferType: ['CC', Validators.required]
    });

    this.updateValidatorsByBankType();

    this.ticketForm.get('bankName')?.valueChanges.subscribe((bank: string) => {
      const accountControl = this.ticketForm.get('accountNumber');
      const transferType = this.isAssociatedBank(bank) ? 'CC' : 'CCI';
      this.ticketForm.get('transferType')?.setValue(transferType);

      const validators = [Validators.required, Validators.pattern(/^\d+$/)];

      if (this.isAssociatedBank(bank)) {
        validators.push(customAccountNumberLengthByBank(bank));
      } else {
        validators.push(customAccountNumberLengthByBank('Otros'));
      }

      accountControl?.setValidators(validators);
      accountControl?.updateValueAndValidity();
    });
  }

  loadUserCredits() {
    this.profileService.getUserInfo().subscribe({
      next: (user) => {
        this.userCredits = user.credits;
      }
    });
  }

  loadTickets() {
    if (this.userRole === UserRole.OWNER) {
      this.bankTransferService.getTicketsByOwner().subscribe({
        next: (tickets) => {
          this.tickets.set(tickets);
        },
        error: (err) => {
          if (err.status === 404) {
            this.tickets.set([]);
          }
        }
      });
    } else {
      this.bankTransferService.getAllTickets().subscribe({
        next: (tickets) => {
          this.tickets.set(tickets);
        },
      });
    }
  }

  openTicketModal() {
    this.showTicketModal = true;
  }

  closeTicketModal() {
    this.showTicketModal = false;
    this.ticketForm.reset();
  }

  submitTicketRequest() {

    if (!this.ticketForm.valid) {
      this.ticketForm.markAllAsTouched();
      return;
    }
    const bankTransferData: TicketRequest = this.ticketForm.getRawValue();
    this.isLoadingSubmitRequest.set(true);
    this.bankTransferService.createTicket(bankTransferData).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.closeTicketModal();
        this.loadTickets();
        this.toastService.success(
          this.translate.instant('tickets.toast.successCreate'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingSubmitRequest.set(false);
        this.toastService.error(
          this.translate.instant('tickets.toast.errorCreate'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  onBankTypeChange() {
    this.ticketForm.reset();
    this.updateValidatorsByBankType();
  }

  private isAssociatedBank(bankName: string): boolean {
    return this.associatedBanks.includes(bankName);
  }

  private updateValidatorsByBankType() {
    const bankNameControl = this.ticketForm.get('bankName');
    const accountControl = this.ticketForm.get('accountNumber');
    const transferTypeControl = this.ticketForm.get('transferType');

    if (this.bankType === 'asociado') {
      transferTypeControl?.setValue('CC');
      accountControl?.setValidators([Validators.required, Validators.pattern(/^\d+$/)]);

      bankNameControl?.valueChanges.subscribe((bank: string) => {
        const validators = [Validators.required, Validators.pattern(/^\d+$/)];
        validators.push(customAccountNumberLengthByBank(bank));
        accountControl?.setValidators(validators);
        accountControl?.updateValueAndValidity();
      });
    } else {
      transferTypeControl?.setValue('CCI');
      bankNameControl?.setValidators([Validators.required]);
      accountControl?.setValidators([Validators.required, Validators.pattern(/^\d+$/)]);

      bankNameControl?.updateValueAndValidity();
      accountControl?.updateValueAndValidity();
    }
  }

  setTab(tab: 'pending' | 'confirmed') {
    this.activeTab.set(tab);
  }

  protected readonly UserRole = UserRole;
  protected readonly TimeUtil = TimeUtil;
}
