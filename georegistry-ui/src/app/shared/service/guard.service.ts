///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Injectable } from "@angular/core";
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from "@angular/router";
import { ConfigurationService } from "@core/service/configuration.service";
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

@Injectable()
export class LabeledPropertyGraphGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private configuration: ConfigurationService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.configuration.isEnableLabeledPropertyGraph()) {
            return true;
        }

        this.router.navigate(["/"]);

        return false;
    }
}

@Injectable()
export class BusinessDataGuard implements CanActivate {

    // eslint-disable-next-line no-useless-constructor
    constructor(private configuration: ConfigurationService, private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
        if (this.configuration.isEnableBusinessData()) {
            return true;
        }

        this.router.navigate(["/"]);

        return false;
    }
}
