import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
  Renderer2, inject, AfterViewInit
} from '@angular/core';
import {FilterConfig} from '../../models/filter-config.interface';
import {FormsModule} from '@angular/forms';
import {GAMEMODE_OPTIONS, getSportIdByValue} from '../../models/sport-space.constants';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-filter',
  imports: [
    FormsModule,
    TranslatePipe
  ],
  templateUrl: './filter.component.html',
  styleUrl: './filter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FiltersComponent implements OnInit, AfterViewInit {
  @Input() config: FilterConfig[] = [];
  @Output() filtersChanged = new EventEmitter<Record<string, any>>();
  @ViewChild('filtersContainer') filtersContainer!: ElementRef;

  private renderer = inject(Renderer2);

  filters: Record<string, any> = {};

  ngOnInit() {
    this.config.forEach(filter => {
      if (filter.type === 'select' || filter.type === 'time') {
        this.filters[filter.field] = '';
      }
    });
  }

  ngAfterViewInit() {
    this.renderer.listen('window', 'scroll', () => {
      const offset = window.scrollY || window.pageYOffset;
      if (offset > 172) {
        this.renderer.addClass(this.filtersContainer.nativeElement, 'scrolled');
      } else {
        this.renderer.removeClass(this.filtersContainer.nativeElement, 'scrolled');
      }
    });
  }

  emitChanges() {
    this.filtersChanged.emit(this.filters);
  }

  clearFilters() {
    this.filters = {};
    this.config.forEach(filter => {
      if (filter.type === 'select' || filter.type === 'time') {
        this.filters[filter.field] = '';
      }
    });
    this.emitChanges();
  }

  timeOptions: string[] = Array.from({ length: 16 }, (_, i) => {
    const hour = (i + 8).toString().padStart(2, '0');
    return `${hour}:00`;
  });

  onSelectChange(field: string, value: any) {
    this.filters[field] = value;

    if (field === 'sport') {
      this.filters['gamemode'] = null;

      const sportId = getSportIdByValue(value);
      const gameModeConfig = this.config.find(c => c.field === 'gamemode');
      if (gameModeConfig) {
        gameModeConfig.options = [
          { label: 'Seleccionar...', value: null, disabled: true },
          ...GAMEMODE_OPTIONS
            .filter(g => g.sportId === sportId)
            .map(g => ({ label: g.label, value: g.value }))
        ];
      }

    }

    this.emitChanges();
  }

  getSelectValue(event: Event): string {
    return (event.target as HTMLSelectElement).value;
  }

  hasSelectedFilters(): boolean {
    return Object.values(this.filters).some(value => value !== '' && value !== null && value !== undefined);
  }

  protected readonly Object = Object;
}
