import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  inject,
  Input, OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {CdkPortal, PortalModule} from '@angular/cdk/portal';
import {Overlay, OverlayConfig} from '@angular/cdk/overlay';
import {NgClass, NgStyle} from '@angular/common';

@Component({
  selector: 'app-modal',
  imports: [PortalModule, NgStyle, NgClass],
  template: `
    <ng-template cdkPortal>
      <div class="modal-backdrop">
        <div class="modal" [ngClass]="variant" [ngStyle]="{ 'max-width': width }">
          <div class="modal__header">
            <ng-content select="[modal-header]"></ng-content>
            <button class="modal__close" (click)="closeModal.emit()" aria-label="Cerrar">
              <i class="lni lni-xmark"></i>
            </button>
          </div>
          <div class="modal__body">
            <ng-content select="[modal-body]"></ng-content>
          </div>
          <div class="modal__footer">
            <ng-content select="[modal-footer]"></ng-content>
          </div>
        </div>
      </div>
    </ng-template>
  `,
  styleUrl: './modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class ModalComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild(CdkPortal) portal: CdkPortal | undefined;
  @Input() width: string = '600px';
  @Input() variant: 'default' | 'danger' | 'warning' | 'info'  = 'default';
  @Output() closeModal = new EventEmitter<void>();

  overlay = inject(Overlay);
  overlayConfig = new OverlayConfig({
    hasBackdrop: true,
    positionStrategy: this.overlay
      .position()
      .global()
      .centerHorizontally()
      .centerVertically(),
    scrollStrategy: this.overlay.scrollStrategies.block(),
    minWidth: 500,
  });
  overlayRef = this.overlay.create(this.overlayConfig);

  ngOnInit(): void {
    // this.overlayRef.backdropClick().subscribe(() => {
    //   this.closeModal.emit();
    // });
  }

  ngAfterViewInit(): void {
    this.overlayRef?.attach(this.portal);
  }

  ngOnDestroy(): void {
    this.overlayRef?.detach();
    this.overlayRef?.dispose();
  }
}
