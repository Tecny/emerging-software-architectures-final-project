import {
  ChangeDetectionStrategy,
  Component, effect,
  inject,
  OnInit,
  signal
} from '@angular/core';
import {SportSpaceCardComponent} from '../../components/sport-space-card/sport-space-card.component';
import {SportSpaceService} from '../../services/sport-space.service';
import {SportSpace} from '../../models/sport-space.interface';
import {UserStoreService} from '../../../../core/services/user-store.service';
import {UserRole} from '../../../../core/models/user.role.enum';
import {RouterLink} from '@angular/router';
import {FiltersComponent} from '../../../../shared/components/filter/filter.component';
import {
  GAMEMODE_OPTIONS,
  getSportIdByValue,
  SPORTS
} from '../../../../shared/models/sport-space.constants';
import {environment} from '../../../../../environment/environment';
import * as L from 'leaflet';
import {NgClass} from '@angular/common';
import {SpinnerComponent} from '../../../../shared/components/spinner/spinner.component';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-list-sport-spaces',
  imports: [
    SportSpaceCardComponent,
    RouterLink,
    FiltersComponent,
    NgClass,
    SpinnerComponent,
    TranslatePipe
  ],
  templateUrl: './list-sport-spaces.component.html',
  styleUrl: './list-sport-spaces.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListSportSpacesComponent implements OnInit {
  private userStore = inject(UserStoreService);
  private sportSpaceService = inject(SportSpaceService);

  userRole = this.userStore.getRoleFromToken();
  sportSpaces = signal<SportSpace[] | null>(null);
  showAddSportSpaceButton = signal(false);
  isMapView = signal(false);

  map!: L.Map;
  allSpaces: SportSpace[] = [];
  filters = {
    sport: null,
    gamemode: null,
    price: null,
    openTime: null
  };

  constructor() {
    effect(() => {
      const spaces = this.sportSpaces();

      if (!spaces) return;

      if (this.isMapView() && !this.map) {
        setTimeout(() => this.initMap(), 0);
        return;
      }

      if (!this.map) return;

      this.map.eachLayer(layer => {
        if (layer instanceof L.Marker) {
          this.map?.removeLayer(layer);
        }
      });

      spaces.forEach(space => {
        if (space.latitude && space.longitude) {
          const popupHtml = `
        <div style="
          width: 100%;
          font-family: 'Poppins', sans-serif;
          color: #111;
          display: flex;
          flex-direction: column;
          gap: 0.5rem;
          border-radius: 0.75rem;
          overflow: hidden;
          padding: 0.75rem 0.5rem;
          box-sizing: border-box;
        ">
          <div style="
            width: 100%;
            height: 110px;
            overflow: hidden;
            position: relative;
            border-radius: 0.5rem;
          ">
            <img src="${space.imageUrl}" alt="${space.name}" style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
              border-radius: 0.5rem;
            " />
          </div>
          <div>
            <div style="font-size: 0.95rem; font-weight: 600; margin-bottom: 0.25rem;">${space.name}</div>
            <div style="
              font-size: 0.75rem;
              color: #555;
              line-height: 1.2;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: normal;
              margin-bottom: 1rem;
            ">
              ${space.address}
            </div>
            <a href="/sport-spaces/${space.id}" style="
              display: flex;
              justify-content: center;
              align-items: center;
              background: #10b981;
              color: white;
              text-align: center;
              padding: 0.4rem 0.75rem;
              font-size: 0.75rem;
              border-radius: 0.375rem;
              text-decoration: none;
              transition: background 0.3s;
              margin: 0 auto;
              width: fit-content;
            ">
              Ver más
            </a>
          </div>
        </div>
      `;

          L.marker([space.latitude, space.longitude])
            .addTo(this.map!)
            .bindPopup(popupHtml, {
              maxWidth: 220,
              minWidth: 220,
              autoPan: true,
              closeButton: true
            });
        }
      });
    });
  }

  ngOnInit() {
    this.loadSportSpaces();
  }

  initMap() {
    if (this.map) return;

    const mapEl = document.getElementById('map');
    if (!mapEl) return;

    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
    });

    this.map = L.map(mapEl).setView([-12.0603, -77.0416], 13);

    L.tileLayer(`https://tiles.locationiq.com/v3/streets/r/{z}/{x}/{y}.png?key=${environment.locationIQKey}`, {
      attribution: '&copy; <a href="https://www.locationiq.com/">LocationIQ</a> contributors'
    }).addTo(this.map);

    this.sportSpaces()?.forEach(space => {
      if (space.latitude && space.longitude) {
        const popupHtml = `
          <div style="
            width: 100%;
            font-family: 'Poppins', sans-serif;
            color: #111;
            display: flex;
            flex-direction: column;
            gap: 0.5rem;
            border-radius: 0.75rem;
            overflow: hidden;
            padding: 0.75rem 0.5rem;
            box-sizing: border-box;
          ">
          <div style="
            width: 100%;
            height: 110px;
            overflow: hidden;
            position: relative;
            border-radius: 0.5rem;
          ">
            <img src="${space.imageUrl}" alt="${space.name}" style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: block;
              border-radius: 0.5rem;
            " />
          </div>
          <div>
            <div style="font-size: 0.95rem; font-weight: 600; margin-bottom: 0.25rem;">${space.name}</div>
            <div style="
              font-size: 0.75rem;
              color: #555;
              line-height: 1.2;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: normal;
              margin-bottom: 1rem;
            ">
              ${space.address}
            </div>
            <a href="/sport-spaces/${space.id}" style="
              display: flex;
              justify-content: center;
              align-items: center;
              background: #10b981;
              color: white;
              text-align: center;
              padding: 0.4rem 0.75rem;
              font-size: 0.75rem;
              border-radius: 0.375rem;
              text-decoration: none;
              transition: background 0.3s;
              margin: 0 auto;
              width: fit-content;
            ">
              Ver más
            </a>
          </div>
        </div>
        `;

        L.marker([space.latitude, space.longitude])
          .addTo(this.map!)
          .bindPopup(popupHtml,{
            maxWidth: 220,
            minWidth: 220,
            autoPan: true,
            closeButton: true
          });
      }
    });

  }

  loadSportSpaces() {
    const request$ = this.userRole === UserRole.OWNER
      ? this.sportSpaceService.getMySportSpaces()
      : this.sportSpaceService.getSportSpaces();

    request$.subscribe({
      next: (spaces) => {
        this.allSpaces = spaces;
        this.applyFilters();

        if (this.userRole === UserRole.OWNER) {
          this.canAddSportSpace();
        } else {
          this.showAddSportSpaceButton.set(false);
        }
      },
      error: (err) => {
        if (err.status === 404) {
          this.allSpaces = [];
          this.sportSpaces.set([]);
        }

        if (this.userRole === UserRole.OWNER) {
          this.canAddSportSpace();
        } else {
          this.showAddSportSpaceButton.set(false);
        }
      }
    });
  }

  canAddSportSpace(): void {
    this.sportSpaceService.canAddSportSpace().subscribe({
      next: (allow) => {
        this.showAddSportSpaceButton.set(allow.canAdd);
      },
      error: () => {
        this.showAddSportSpaceButton.set(false);
      }
    });
  }

  onFiltersChanged(filters: any) {
    this.filters = filters;
    this.applyFilters();
  }

  applyFilters() {
    const { sport, price, openTime, gamemode } = this.filters;

    const sportId = sport ? getSportIdByValue(sport) : undefined;

    const gamemodeId = gamemode
      ? GAMEMODE_OPTIONS.find(g => g.value === gamemode)?.id
      : undefined;

    const filtered = this.allSpaces.filter(space => {
      return (
        (!sportId || space.sportId === sportId) &&
        (!gamemodeId || space.gamemodeId === gamemodeId) &&
        (!price || space.price <= price) &&
        (!openTime || String(space.openTime) <= String(openTime))
      );
    });

    this.sportSpaces.set(filtered);
  }

  toggleView() {
    this.isMapView.update(current => {
      const next = !current;

      if (!next) {
        this.destroyMap();
      }

      return next;
    });
  }

  destroyMap() {
    if (this.map) {
      this.map.off();
      this.map.remove();
      this.map = undefined!;
    }
  }


  protected readonly SPORTS = SPORTS;
  protected readonly UserRole = UserRole;
}
