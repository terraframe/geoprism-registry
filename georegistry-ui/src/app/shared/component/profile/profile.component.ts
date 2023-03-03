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

import { Component, Input } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Profile } from "@shared/model/profile";
import { AuthService, ProfileService } from "@shared/service";

@Component({
    selector: "profile",
    templateUrl: "./profile.component.html",
    styles: [".modal-form .check-block .chk-area { margin: 10px 0px 0 0;}"]
})
export class ProfileComponent {

    public _profile: Profile = {
        oid: "",
        username: "",
        password: "",
        firstName: "",
        lastName: "",
        email: "",
        changePassword: false
    };

    // eslint-disable-next-line accessor-pairs
    @Input("profile")
    set profile(value: Profile) {
        this._profile = value;
        this.getRoles();
    }

    roles: any[] = [];

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: ProfileService, public bsModalRef: BsModalRef, private authService: AuthService) { }

    getRoles(): void {
        this.service.getRolesForUser(this._profile.oid).then(roles => {
            this.roles = roles;
        });
    }

    onSubmit(): void {
        if (!this._profile.changePassword) {
            delete this._profile.password;
        }

        this.service.apply(this._profile).then(profile => {
            this.bsModalRef.hide();
        });
    }

    onChangePassword(): void {
        this._profile.changePassword = !this._profile.changePassword;
    }

    // getRoles():string {
    //   return this.authService.getRoleDisplayLabels();
    // }

    getRolesArray(): any {
        return this.authService.getRoles();
    }

    cancel(): void {
        this.service.unlock(this._profile.oid).then(profile => {
            this.bsModalRef.hide();
        });
    }

}
