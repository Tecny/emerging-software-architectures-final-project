import {Component, inject} from '@angular/core';
import {UserStoreService} from '../../services/user-store.service';
import {Router} from '@angular/router';
import {UserRole} from '../../models/user.role.enum';

@Component({
  standalone: true,
  template: ''
})
export class RedirectComponent {
  private userStore = inject(UserStoreService);
  private router = inject(Router);

  constructor() {
    const role = this.userStore.getRoleFromToken();

    switch (role) {
      case UserRole.PLAYER:
      case UserRole.OWNER:
      case UserRole.ADMIN:
        this.router.navigateByUrl('/home').then();
        break;
      default:
        this.router.navigateByUrl('/login').then();
        break;
    }
  }
}
