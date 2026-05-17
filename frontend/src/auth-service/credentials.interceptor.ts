import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../environments/environment';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('localhost:8443')) {
    req = req.clone({ withCredentials: true });
  }
  return next(req);
};
