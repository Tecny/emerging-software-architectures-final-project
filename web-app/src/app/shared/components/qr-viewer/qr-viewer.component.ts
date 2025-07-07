import {
  ChangeDetectionStrategy, ChangeDetectorRef,
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import {ModalComponent} from '../modal/modal.component';
import {QrService} from '../../services/qr.service';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-qr-viewer',
  imports: [
    ModalComponent,
    TranslatePipe
  ],
  template: `
    <app-modal [width]="'400px'" [variant]="'info'" (closeModal)="onClose()">
      <div modal-header>{{ 'qr.title' | translate }}</div>
      <div modal-body>
        @if (qrImageUrl) {
          <div style="display: flex; justify-content: center; align-items: center; height: 300px;">
            <img [src]="qrImageUrl" alt="QR de la reserva" width="250" height="250"/>
          </div>
        } @else {
          <p>{{ 'qr.notAvailable' | translate }}</p>
        }
      </div>
      <div modal-footer>
        <button class="button-submit--info" (click)="onClose()">{{ 'qr.accept' | translate }}</button>
      </div>
    </app-modal>
  `,
  styleUrl: './qr-viewer.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class QrViewerComponent implements OnChanges {
  @Input() reservationId!: number;
  @Output() close = new EventEmitter<void>();

  qrImageUrl: string | null = null;
  private qrService = inject(QrService);
  private cdr = inject(ChangeDetectorRef);

  ngOnChanges(changes: SimpleChanges) {
    if (changes['reservationId'] && this.reservationId) {
      this.qrService.generateQrToken(this.reservationId).subscribe({
        next: (res) => {
          this.qrService.generateQRImage(res.qrToken).subscribe({
            next: (url) => {
              this.qrImageUrl = url;
              this.cdr.markForCheck();
            },
            error: () => {
              this.qrImageUrl = null;
              this.cdr.markForCheck();
            },
          });
        },
        error: () => {
          this.qrImageUrl = null;
          this.cdr.markForCheck();
        },
      });
    }
  }

  onClose() {
    this.qrImageUrl = null;
    this.close.emit();
  }

  protected readonly onclose = onclose;
}
