import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-reset-password',
  imports: [
    ReactiveFormsModule,
    TranslatePipe
  ],
  template: `
    <div class="recover-password">
      <h1>{{ 'resetPassword.title' | translate }}</h1>
      <p>{{ 'resetPassword.description' | translate }}</p>
      <form [formGroup]="recoverPasswordForm" (ngSubmit)="onSubmit()">
        <input type="password" formControlName="password" [placeholder]="'resetPassword.fields.password' | translate" required />
        <input type="password" formControlName="confirmPassword" [placeholder]="'resetPassword.fields.confirmPassword' | translate" required />
        @if (recoverPasswordForm.get('password')?.invalid && (recoverPasswordForm.get('password')?.dirty || recoverPasswordForm.get('password')?.touched)) {
          @if (recoverPasswordForm.get('password')?.errors?.['required']) {
            <small>{{ 'resetPassword.validation.passwordRequired' | translate }}</small>
          } @else {
            @if (recoverPasswordForm.get('password')?.errors?.['minlength']) {
              <small>{{ 'resetPassword.validation.passwordMin' | translate }}</small>
            }
            @else  {
              <small>{{ 'resetPassword.validation.passwordPattern' | translate }}</small>
            }
          }
        }
        @if (passwordsDoNotMatch) {
          <small>{{ 'resetPassword.validation.passwordsNotMatch' | translate }}</small>
        }
        <button type="submit">{{ 'resetPassword.buttons.update' | translate }}</button>
      </form>
    </div>
  `,
  styleUrl: './reset-password.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResetPasswordComponent implements OnInit {
  private fb = inject(NonNullableFormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  token: string | null = null;

  recoverPasswordForm = this.fb.group({
    password: ['', [
      Validators.required,
      Validators.minLength(16),
      Validators.pattern(/^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{16,}$/)
    ]],
    confirmPassword: ['', [Validators.required]]
  });

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
    });
  }

  get passwordsDoNotMatch(): boolean {
    const { password, confirmPassword } = this.recoverPasswordForm.value;
    return password !== confirmPassword && this.recoverPasswordForm.touched;
  }

  onSubmit() {
    if (this.recoverPasswordForm.invalid) {
      this.recoverPasswordForm.markAllAsTouched();
      return;
    }
    if (this.recoverPasswordForm.valid && !this.passwordsDoNotMatch) {
      const { password } = this.recoverPasswordForm.getRawValue();

      if (!this.token) {
        return;
      }

      this.authService.resetPassword(this.token, password).subscribe({
        next: () => {
          this.toastService.success(
            this.translate.instant('resetPassword.toast.success'),
            this.translate.instant('toastStatus.success')
          );
          this.router.navigate(['/login']).then();
        },
        error: () => {
          this.toastService.error(
            this.translate.instant('resetPassword.toast.error'),
            this.translate.instant('toastStatus.error')
          );
        }
      });
    }
  }
}
