import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import { AuthService} from './auth.service';

@Injectable()
export class AdminGuard implements CanActivate {

  constructor(private service:AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    
    if (this.service.isAdmin()) {
      return true; 
    }
    
    this.router.navigate([ '/hierarchies' ]);
    
    return false;
  }
}
