import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {UserStoreService} from '../../../core/services/user-store.service';
import {UserRole} from '../../../core/models/user.role.enum';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'app-home',
  imports: [
    RouterLink,
    TranslatePipe
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HomeComponent {
  private userStore = inject(UserStoreService);

  userRole = this.userStore.getRoleFromToken();

  protected readonly UserRole = UserRole;
}
