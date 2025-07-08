import { Routes } from '@angular/router';
import {LoginComponent} from './auth/pages/login/login.component';
import {RegisterComponent} from './auth/pages/register/register.component';
import {authGuard} from './core/guards/auth.guard';
import {UnauthorizedComponent} from './shared/pages/unauthorized/unauthorized.component';
import {NotFoundComponent} from './shared/pages/not-found/not-found.component';
import {UserRole} from './core/models/user.role.enum';
import {roomGuard} from './core/guards/room.guard';
import {ResetPasswordComponent} from './auth/pages/reset-password/reset-password.component';
import {RedirectComponent} from './core/components/redirect/redirect.component';
import {HomeComponent} from './shared/pages/home/home.component';
import {CorrectPaymentComponent} from './shared/pages/correct-payment/correct-payment.component';
import {ErrorPaymentComponent} from './shared/pages/error-payment/error-payment.component';
import {guestGuard} from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: RegisterComponent,
    canActivate: [guestGuard]
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [authGuard],
    data: { roles: [UserRole.PLAYER, UserRole.OWNER, UserRole.ADMIN] },
  },
  {
    path: 'profile',
    loadComponent: () => import('./features/profile/pages/view-profile/view-profile.component')
      .then(m => m.ViewProfileComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.PLAYER, UserRole.OWNER] },
  },
  {
    path: 'subscription',
    loadComponent: () => import('./features/subscription/pages/view-subscription/view-subscription.component')
      .then(m => m.ViewSubscriptionComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.OWNER] },
  },
  {
    path: 'sport-spaces',
    loadComponent: () => import('./features/sport-space/pages/list-sport-spaces/list-sport-spaces.component')
      .then(m => m.ListSportSpacesComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.PLAYER, UserRole.OWNER] },
  },
  {
    path: 'sport-spaces/create',
    loadComponent: () => import('./features/sport-space/pages/create-sport-space/create-sport-space.component')
      .then(m => m.CreateSportSpaceComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.OWNER] },
  },
  {
    path: 'sport-spaces/:id',
    loadComponent: () => import('./features/sport-space/pages/sport-space-detail/sport-space-detail.component')
      .then(m => m.SportSpaceDetailComponent),
  },
  {
    path: 'sport-spaces/:id/dashboard',
    loadComponent: () => import('./features/sport-space/pages/sport-space-dashboard/sport-space-dashboard.component')
      .then(m => m.SportSpaceDashboardComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.OWNER] },
  },
  {
    path: 'reservations',
    loadComponent: () => import('./features/reservation/pages/list-reservations/list-reservations.component')
      .then(m => m.ListReservationsComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.PLAYER, UserRole.OWNER] },
  },
  {
    path: 'rooms',
    loadComponent: () => import('./features/room/pages/list-rooms/list-rooms.component')
      .then(m => m.ListRoomsComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.PLAYER] },
  },
  {
    path: 'rooms/:id',
    loadComponent: () => import('./features/room/pages/room-detail/room-detail.component')
      .then(m => m.RoomDetailComponent),
    canActivate: [authGuard, roomGuard],
    data: { roles: [UserRole.PLAYER] },
  },
  {
    path: 'bank-transfer',
    loadComponent: () => import('./features/bank-transfer/pages/list-tickets/list-tickets.component')
      .then(m => m.ListTicketsComponent),
    canActivate: [authGuard],
    data: { roles: [UserRole.OWNER, UserRole.ADMIN] },
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent
  },
  {
    path: 'correct-payment',
    component: CorrectPaymentComponent
  },
  {
    path: 'error-payment',
    component: ErrorPaymentComponent
  },
  {
    path: 'unauthorized',
    component: UnauthorizedComponent
  },
  {
    path: 'notfound',
    component: NotFoundComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    component: RedirectComponent
  },
  { path: '**',
    redirectTo: 'notfound'
  }
];
