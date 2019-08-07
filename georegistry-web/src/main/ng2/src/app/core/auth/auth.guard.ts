import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import { AuthService} from './auth.service';

declare var acp: any;

@Injectable()
export class AuthGuard implements CanActivate {

  constructor(private service:AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    
    if (this.service.isLoggedIn()) {
      return true; 
    }
    
    this.router.navigate([ '/login' ]);
    
    return false;
  }
}
