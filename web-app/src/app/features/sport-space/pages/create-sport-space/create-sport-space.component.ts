import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  inject,
  signal
} from '@angular/core';
import {FormGroup, NonNullableFormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {SportSpaceService} from '../../services/sport-space.service';
import {timeRangeValidator} from '../../../../shared/validators/forms.validator';
import {GAMEMODE_OPTIONS, gamemodesMap, SPORTS} from '../../../../shared/models/sport-space.constants';
import {Router} from '@angular/router';
import {GeolocationService} from '../../../../shared/services/geolocation.service';
import * as L from 'leaflet';
import {environment} from '../../../../../environment/environment';
import {Location, NgClass} from '@angular/common';
import {ToastrService} from 'ngx-toastr';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'app-create-sport-space',
  imports: [
    ReactiveFormsModule,
    NgClass,
    TranslatePipe
  ],
  templateUrl: './create-sport-space.component.html',
  styleUrl: './create-sport-space.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateSportSpaceComponent implements AfterViewInit {
  private location = inject(Location);
  private sportSpaceService = inject(SportSpaceService);
  private geolocationService = inject(GeolocationService);
  private fb = inject(NonNullableFormBuilder)
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private toastService = inject(ToastrService);
  private translate = inject(TranslateService);

  private _sportId = signal<number>(1);
  gamemodes = computed(() => this.getGamemodesBySport(this._sportId()));
  isLoadingSubmitRequest = signal(false);

  createSportSpaceForm: FormGroup;
  selectedImageUrl: string | null = null;
  selectedImageFile: File | null = null;
  latitude: number | null = null;
  longitude: number | null = null;
  locationNotSelected = false;
  selectedAddress: string = '';
  submitted = false;

  constructor() {
    this.createSportSpaceForm = this.fb.group(
      {
        name: ['', [Validators.required, Validators.minLength(7)]],
        sportId: ['', [Validators.required, Validators.min(1), Validators.max(2)]],
        gamemodeId: ['', [Validators.required]],
        price: ['', [Validators.required, Validators.min(25)]],
        description: ['', [Validators.required, Validators.minLength(10)]],
        openTime: ['', [Validators.required]],
        closeTime: ['', [Validators.required]],
      },
      { validators: timeRangeValidator() }
    );

    this.createSportSpaceForm.get('sportId')?.valueChanges.subscribe(value => {
      this._sportId.set(value);
      this.cdr.detectChanges();
      this.createSportSpaceForm.get('gamemodeId')?.reset('', { emitEvent: false, onlySelf: true });
    });
  }

  ngAfterViewInit(): void {
    delete (L.Icon.Default.prototype as any)._getIconUrl;

    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
    });

    const map = L.map('map').setView([-12.0603, -77.0416], 13);

    L.tileLayer(`https://tiles.locationiq.com/v3/streets/r/{z}/{x}/{y}.png?key=${environment.locationIQKey}`, {
      attribution: '&copy; <a href="https://www.locationiq.com/">LocationIQ</a> contributors'
    }).addTo(map);

    let marker: L.Marker | null = null;

    const updateLocation = (lat: number, lng: number) => {
      this.latitude = lat;
      this.longitude = lng;
      this.locationNotSelected = false;
      this.cdr.detectChanges();
    };

    map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;

      if (!marker) {
        marker = L.marker([lat, lng], { draggable: true }).addTo(map);

        marker.on('dragend', () => {
          const position = marker!.getLatLng();
          updateLocation(position.lat, position.lng);
        });
      } else {
        marker.setLatLng([lat, lng]);
      }

      updateLocation(lat, lng);
      this.onMapClick({ lat, lng });
    });
  }

  createSportSpace() {
    this.submitted = true;

    if (this.createSportSpaceForm.invalid || !this.selectedImageFile || this.latitude === null || this.longitude === null) {
      this.createSportSpaceForm.markAllAsTouched();
      this.locationNotSelected = this.latitude === null || this.longitude === null;
      return;
    }

    this.locationNotSelected = false;

    this.isLoadingSubmitRequest.set(true);
    const formValues = this.createSportSpaceForm.getRawValue();

    const formData = new FormData();
    formData.append('name', formValues.name);
    formData.append('image', this.selectedImageFile);
    formData.append('sportId', formValues.sportId.toString());
    formData.append('gamemodeId', formValues.gamemodeId.toString());
    formData.append('price', formValues.price.toString());
    formData.append('description', formValues.description);
    formData.append('openTime', formValues.openTime);
    formData.append('closeTime', formValues.closeTime);
    formData.append('latitude', this.latitude.toString());
    formData.append('longitude', this.longitude.toString());

    this.sportSpaceService.createSportSpace(formData).subscribe({
      next: () => {
        this.isLoadingSubmitRequest.set(false);
        this.createSportSpaceForm.reset();
        this.selectedImageUrl = null;
        this.selectedImageFile = null;
        this._sportId.set(1);
        this.router.navigate(['/sport-spaces']).then();
        this.toastService.success(
          this.translate.instant('spaces.create.toasts.success'),
          this.translate.instant('toastStatus.success')
        );
      },
      error: () => {
        this.isLoadingSubmitRequest.set(false);
        this.toastService.error(
          this.translate.instant('spaces.create.toasts.error'),
          this.translate.instant('toastStatus.error')
        );
      }
    });
  }

  onImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files[0]) {
      this.selectedImageFile = input.files[0];

      const reader = new FileReader();
      reader.onload = () => {
        this.selectedImageUrl = reader.result as string;
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(this.selectedImageFile);
    }
  }

  onMapClick(event: { lat: number, lng: number }) {
    this.latitude = event.lat;
    this.longitude = event.lng;

    this.geolocationService.reverseGeocode(event.lat, event.lng).subscribe({
      next: (data) => {
        this.selectedAddress = data.display_name;
        this.createSportSpaceForm.get('address')?.setValue(data.display_name);
        this.cdr.detectChanges();
      },
    });
  }

  private getGamemodesBySport(sportId: number): { id: number, label: string, value: string, sportId: number }[] {
    const values = gamemodesMap[sportId] ?? [];
    return GAMEMODE_OPTIONS.filter(g => values.includes(g.value));
  }

  goBack() {
    this.location.back();
  }

  protected readonly SPORTS = SPORTS;
}
