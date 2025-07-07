import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {ProfileService} from '../../services/profile.service';
import {UserProfile} from '../../models/user-profile.interface';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../../../auth/services/auth.service';
import {customEmailValidator} from '../../../../shared/validators/forms.validator';
import {ModalComponent} from '../../../../shared/components/modal/modal.component';
import {ToastrService} from 'ngx-toastr';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {ThemeService} from '../../../../shared/services/theme.service';
import {TranslateModule, TranslateService, TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-view-profile',
  imports: [
    ReactiveFormsModule,
    ModalComponent,
    FormsModule,
    SpinnerComponent,
    TranslateModule,
    TranslatePipe
  ],
  templateUrl: './view-profile.component.html',
  styleUrl: './view-profile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ViewProfileComponent implements OnInit {
  private profileService = inject(ProfileService);
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private toastService = inject(ToastrService);
  private themeService = inject(ThemeService);
  private translate = inject(TranslateService);

  userInfo = signal<UserProfile | null>(null);
  isLoadingSubmitRequest = signal(false);
  activeTab = signal<'info' | 'prefs'>('info');

  profileForm!: FormGroup;
  editingName = false;
  editingEmail = false;
  editingPassword = false;
  showRechargeModal = false;
  selectedLanguage = 'es';

  ngOnInit(): void {
    const savedLang = localStorage.getItem('language');
    this.selectedLanguage = savedLang || this.translate.currentLang || 'es';
    this.translate.use(this.selectedLanguage);
    this.loadUserInfo();
  }

  setTab(tab: 'info' | 'prefs') {
    this.activeTab.set(tab);
  }

  loadUserInfo() {
    this.profileService.getUserInfo().subscribe({
      next: (user) => {
        this.userInfo.set(user);
        this.initForm(user);
      },
      error: () => this.userInfo.set(null),
    });
  }

  initForm(user: UserProfile) {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, customEmailValidator()]],
      password: ['', [
        Validators.required,
        Validators.minLength(16),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{16,}$/)
      ]],
      amount: ['', [Validators.required, Validators.min(1)]],
    });
  }

  updateName() {
    const nameControl = this.profileForm.get('name');
    const name = nameControl?.value.trim();
    const currentName = this.userInfo()?.name;
    if (!nameControl?.valid) {
      nameControl?.markAsTouched();
      return;
    }

    if (name === currentName) {
      this.translate.get('profile.toast.nameRepeat').subscribe((msg) => {
        this.toastService.error(msg, this.translate.instant('toastStatus.error'));
      });
      return;
    }

    this.isLoadingSubmitRequest.set(true);
    this.profileService.changeName(name).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.editingName = false;
        this.loadUserInfo();
        nameControl.reset();
        this.translate.get('profile.toast.nameUpdated').subscribe((msg) => {
          this.toastService.success(msg, this.translate.instant('toastStatus.success'));
        });
      },
      error: () => {
        this.isLoadingSubmitRequest.set(false);
        this.translate.get('profile.toast.nameUpdateError').subscribe((msg) => {
          this.toastService.error(msg, this.translate.instant('toastStatus.error'));
        });
      }
    });
  }

  updateEmail() {
    const emailControl = this.profileForm.get('email');
    const email = emailControl?.value.trim();
    const currentEmail = this.userInfo()?.email;

    if (!emailControl?.valid) {
      emailControl?.markAsTouched();
      return;
    }

    if (email === currentEmail) {
      this.translate.get('profile.toast.emailRepeat').subscribe((msg) => {
        this.toastService.error(msg, this.translate.instant('toastStatus.error'));
      });
      return;
    }

    this.isLoadingSubmitRequest.set(true);
    this.profileService.changeEmail(email).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.editingEmail = false;
        this.loadUserInfo();
        emailControl.reset();
        this.translate.get('profile.toast.emailUpdated').subscribe((msg) => {
          this.toastService.success(msg, this.translate.instant('toastStatus.success'));
        });
      },
      error: () => {
        this.isLoadingSubmitRequest.set(false);
        this.translate.get('profile.toast.emailUpdateError').subscribe((msg) => {
          this.toastService.error(msg, this.translate.instant('toastStatus.error'));
        });
      }
    });
  }

  updatePassword() {
    const passwordControl = this.profileForm.get('password');
    const password = passwordControl?.value;

    const passwordPattern = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{16,}$/;

    if (!passwordControl?.valid || !passwordPattern.test(password)) {
      passwordControl?.markAsTouched();
      return;
    }

    this.isLoadingSubmitRequest.set(true);
    this.profileService.changePassword(password).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.editingPassword = false;
        passwordControl.reset();
        this.translate.get('profile.toast.passwordUpdated').subscribe((msg) => {
          this.toastService.success(msg, this.translate.instant('toastStatus.success'));
        });
      },
      error: () => {
        this.isLoadingSubmitRequest.set(false);
        this.translate.get('profile.toast.passwordUpdateError').subscribe((msg) => {
          this.toastService.error(msg, this.translate.instant('toastStatus.error'));
        });
      }
    });
  }

  openRechargeModal() {
    this.showRechargeModal = true;
  }

  closeRechargeModal() {
    this.showRechargeModal = false;
    this.profileForm.get('amount')?.reset();
  }

  recharge() {
    if (!this.profileForm.get('amount')?.valid) {
      this.profileForm.get('amount')?.markAsTouched();
      return;
    }

    const width = Math.min(800, Math.floor(window.innerWidth * 0.9));
    const height = Math.min(600, Math.floor(window.innerHeight * 0.85));
    const left = Math.floor((window.innerWidth - width) / 2);
    const top = Math.floor((window.innerHeight - height) / 2);

    const features = `width=${width},height=${height},left=${left},top=${top},resizable=yes,scrollbars=yes`;

    const amountControl = this.profileForm.get('amount');
    const amount = amountControl?.value;

    this.isLoadingSubmitRequest.set(true);
    this.profileService.rechargeCredits(amount).subscribe({
      next: (response) => {
        const approvalUrl = response.approval_url;
        const paymentWindow = window.open(approvalUrl, 'PayPal Payment', features);
        if (paymentWindow) {
          const interval = setInterval(() => {
            if (paymentWindow.closed) {
              this.isLoadingSubmitRequest.set(false);
              this.closeRechargeModal();
              clearInterval(interval);
              this.loadUserInfo();
            }
          }, 1000);
        } else {
          this.translate.get('profile.toast.paymentOpenError').subscribe((msg) => {
            this.toastService.error(msg, this.translate.instant('toastStatus.error'));
          });
        }
      },
      error: () => {
        this.translate.get('profile.toast.paymentOpenError').subscribe((msg) => {
          this.toastService.error(msg, this.translate.instant('toastStatus.error'));
        });
      }
    });
  }

  logout() {
    this.authService.logout();
    this.translate.get('profile.toast.sessionClosed').subscribe((msg) => {
      this.toastService.success(msg, this.translate.instant('toastStatus.success'));
    });
  }

  toggleTheme(isDark: boolean) {
    this.themeService.toggleTheme(isDark);
  }

  get isDarkTheme() {
    return this.themeService.isDarkTheme;
  }

  onThemeChange(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    this.toggleTheme(value === 'Oscuro' || value === 'Dark');
  }

  onLanguageChange(language: string) {
    this.selectedLanguage = language;
    localStorage.setItem('language', language);
    this.translate.use(language);
  }
}

