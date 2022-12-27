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
