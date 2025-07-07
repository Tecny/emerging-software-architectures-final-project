import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {SportSpace} from '../../models/sport-space.interface';
import * as L from 'leaflet';
import {
  gamemodeIdToLabelMap
} from '../../../../shared/models/sport-space.constants';
import {environment} from '../../../../../environment/environment';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-sport-space-info',
  imports: [TranslatePipe],
  templateUrl: './sport-space-info.component.html',
  styleUrl: './sport-space-info.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SportSpaceInfoComponent implements OnInit {
  @Input() sportSpace!: SportSpace

  map!: L.Map;

  ngOnInit() {
    setTimeout(() => this.initMap(), 0);
  }

  initMap() {
    if (this.map) return;
    if (!this.sportSpace?.latitude || !this.sportSpace?.longitude) return;

    const mapEl = document.getElementById('map');
    if (!mapEl) return;

    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
    });

    this.map = L.map(mapEl).setView([this.sportSpace.latitude, this.sportSpace.longitude], 16);

    L.tileLayer(`https://tiles.locationiq.com/v3/streets/r/{z}/{x}/{y}.png?key=${environment.locationIQKey}`, {
      attribution: '&copy; <a href="https://www.locationiq.com/">LocationIQ</a> contributors'
    }).addTo(this.map);

    L.marker([this.sportSpace.latitude, this.sportSpace.longitude])
      .addTo(this.map)
      .bindPopup(`<span style="font-family: 'Poppins', sans-serif;">${this.sportSpace.name}</span>`)
  }

  protected readonly gamemodeIdToLabelMap = gamemodeIdToLabelMap;
  protected readonly String = String;
}
