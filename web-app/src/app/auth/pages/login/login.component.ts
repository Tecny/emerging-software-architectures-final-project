import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterLink} from '@angular/router';
import {LoginRequest} from '../../models/login.interface';
import {customEmailValidator} from '../../../shared/validators/forms.validator';
import {ModalComponent} from '../../../shared/components/modal/modal.component';
import {ToastrService} from 'ngx-toastr';
import {ThemeService} from '../../../shared/services/theme.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    ModalComponent,
    TranslatePipe
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {

  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(NonNullableFormBuilder);
  private toastService = inject(ToastrService);
  private themeService = inject(ThemeService);
  private translate = inject(TranslateService);

  showRecoverModal = false;

  isLoadingSignInRequest = signal(false);
  isLoadingRecoverRequest = signal(false);
  correctMessage = signal<boolean | null>(null);
  errorMessage = signal<boolean | null>(null);

  loginForm = this.fb.group({
    email: ['', [Validators.required, customEmailValidator()]],
    password: ['', Validators.required],
  });

  recoverPasswordForm = this.fb.group({
    email: ['', [Validators.required, customEmailValidator()]],
  });

  login() {
    if (this.loginForm.invalid || this.isLoadingSignInRequest()) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.isLoadingSignInRequest.set(true);
    const userData: LoginRequest = this.loginForm.getRawValue();

    this.authService.login(userData).subscribe({
      next: () => {
        this.isLoadingSignInRequest.set(false);
        this.router.navigate(['/home']).then();
        this.toastService.success(
          this.translate.instant('login.toast.successLogin'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingSignInRequest.set(false);
        this.errorMessage.set(true);
        this.toastService.error(
          this.translate.instant('login.toast.errorLogin'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  openRecoverModal() {
    this.showRecoverModal = true;
  }

  closeRecoverModal() {
    this.showRecoverModal = false;
    this.recoverPasswordForm.reset();
  }

  forgotPassword(email: string) {
    if (this.recoverPasswordForm.invalid || this.isLoadingRecoverRequest()) {
      this.recoverPasswordForm.markAllAsTouched();
      return;
    }
    this.isLoadingRecoverRequest.set(true);
    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.recoverPasswordForm.reset();
        this.showRecoverModal = false;
        this.isLoadingRecoverRequest.set(false);
        this.toastService.success(
          this.translate.instant('login.toast.successRecover'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingRecoverRequest.set(false);
        this.errorMessage.set(true);
        this.toastService.error(
          this.translate.instant('login.toast.errorRecover'),
          this.translate.instant('toastStatus.error')
        );
      }
    })
  }

  get isDarkTheme() {
    return this.themeService.isDarkTheme;
  }
}
