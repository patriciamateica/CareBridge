import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../environments/environment';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  req = req.clone({ withCredentials: true });
  return next(req);
};
