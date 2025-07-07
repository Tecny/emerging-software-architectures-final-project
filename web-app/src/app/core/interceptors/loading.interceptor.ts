import { HttpInterceptorFn } from '@angular/common/http';
import {inject} from '@angular/core';
import {LoadingService} from '../services/loading.service';
import {catchError, finalize, throwError} from 'rxjs';
import {ToastrService} from 'ngx-toastr';

export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);
  const toastr = inject(ToastrService);

  loadingService.start();

  return next(req).pipe(
    finalize(() => loadingService.stop()),
    catchError((error) => {
      if (error.status >= 500 && error.status < 600) {
        toastr.error('Error del servidor. Intenta más tarde.', 'Error');
      }
      return throwError(() => error);
    })
  );
};
