import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('localhost:8080')) {
    req = req.clone({ withCredentials: true });
  }
  return next(req);
};
