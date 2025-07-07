import {ChangeDetectionStrategy, Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {SportSpaceService} from '../../services/sport-space.service';
import {SportSpace} from '../../models/sport-space.interface';
import {SportSpaceInfoComponent} from '../../components/sport-space-info/sport-space-info.component';
import {
  SportSpaceAvailabilityComponent
} from '../../components/sport-space-availability/sport-space-availability.component';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {Location} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-sport-space-detail',
  imports: [
    SportSpaceInfoComponent,
    SportSpaceAvailabilityComponent,
    SpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './sport-space-detail.component.html',
  styleUrl: './sport-space-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SportSpaceDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private location = inject(Location);
  private sportSpaceService = inject(SportSpaceService);

  activeTab = signal<'availability' | 'info'>('availability');
  sportSpace = signal<SportSpace | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.sportSpaceService.getSportSpaceById(id).subscribe({
      next: (space) => {
        this.sportSpace.set(space);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  setTab(tab: 'availability' | 'info') {
    this.activeTab.set(tab);
  }

  goBack() {
    this.location.back();
  }
}
