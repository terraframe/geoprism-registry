import { Injectable } from "@angular/core";
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from "@angular/router";
import { AuthService } from "./auth.service";

@Injectable()
export class AdminGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: AuthService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.service.isAdmin()) {
            return true;
        }

        this.router.navigate(["/"]);

        return false;
    }

}

@Injectable()
export class MaintainerGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: AuthService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.service.isAdmin() || this.service.isMaintainer()) {
            return true;
        }

        this.router.navigate(["/"]);

        return false;
    }

}

@Injectable()
export class ContributerGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: AuthService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.service.isAdmin() || this.service.isMaintainer() || this.service.isContributer()) {
            return true;
        }

        this.router.navigate(["/"]);

        return false;
    }

}

@Injectable()
export class AuthGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: AuthService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.service.isLoggedIn()) {
            return true;
        }

        this.router.navigate(["/login"]);

        return false;
    }

}

