import { HttpInterceptorFn } from '@angular/common/http';
import {environment} from '../../../environment/environment';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  const isBackend = req.url.startsWith(environment.baseUrl);

  const modifiedReq = isBackend
    ? req.clone({ withCredentials: true })
    : req.clone({ withCredentials: false });

  return next(modifiedReq);
};
