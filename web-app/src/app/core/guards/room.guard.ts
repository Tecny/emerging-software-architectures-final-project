import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {RoomService} from '../../features/room/services/room.service';
import {catchError, map, of} from 'rxjs';

export const roomGuard: CanActivateFn = (route) => {
  const roomId = Number(route.params['id']);
  const roomService = inject(RoomService);
  const router = inject(Router);

  return roomService.userRoomStatus(roomId).pipe(
    map(status => {
      if (status.isMember || status.isRoomCreator) {
        return true;
      }
      router.navigate(['/rooms']).then();
      return false;
    }),
    catchError(() => {
      router.navigate(['/rooms']).then();
      return of(false);
    })
  );
};


