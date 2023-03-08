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
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ProfileComponent } from "../profile/profile.component";

import { AuthService, ProfileService } from "@shared/service";

import { RegistryRoleType } from "@shared/model/core";

import { environment } from 'src/environments/environment';
import { ConfigurationService } from "@core/service/configuration.service";
import { LocaleView } from "@core/model/core";
import { Router } from "@angular/router";
import EnvironmentUtil from "@core/utility/environment-util";

@Component({

    selector: "cgr-header",
    templateUrl: "./header.component.html",
    styleUrls: []
})
export class CgrHeaderComponent {

    context: string;
    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    isPublic: boolean = true;
    bsModalRef: BsModalRef;

    defaultLocaleView: LocaleView;
    locales: LocaleView[];
    locale: string;

    enableBusinessData: boolean = false;

    @Input() loggedIn: boolean = true;

    constructor(
        private modalService: BsModalService,
        private profileService: ProfileService,
        private service: AuthService,
        private configuration: ConfigurationService,
        private router: Router
    ) {
        this.context = EnvironmentUtil.getApiUrl();

        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();
        this.isPublic = service.isPublic();

        this.enableBusinessData = configuration.isEnableBusinessData() || false;

        const locales = configuration.getLocales();
        this.locale = configuration.getLocale();

        this.locales = locales.filter((l: LocaleView) => l.toString !== "defaultLocale");
        this.defaultLocaleView = locales.filter((l: LocaleView) => l.toString === "defaultLocale")[0];

        let found: boolean = false;

        for (let i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].toString === this.locale) {
                found = true;
            }
        }

        if (!found) {
            this.locale = "";
        }



        // } else {
        //     this.locales = [];
        //     this.defaultLocaleView = null;
        // }
    }

    shouldShowMenuItem(item: string): boolean {
        if (item === "HIERARCHIES") {
            return true;
        } else if (item === "LISTS") {
            // return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC) || this.service.hasExactRole(RegistryRoleType.AC);
            return true;
        } else if (item === "BUSINESS-TYPES") {
            if (this.configuration.isEnableBusinessData()) {
                return true;
            } else {
                return false;
            }
        } else if (this.service.hasExactRole(RegistryRoleType.SRA)) {
            return true;
        } else if (item === "IMPORT") {
            return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
        } else if (item === "SCHEDULED-JOBS") {
            return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
        } else if (item === "NAVIGATOR") {
            return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC);
        } else if (item === "CHANGE-REQUESTS") {
            return this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC);
        } else if (item === "TASKS") {
            return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
        } else if (item === "EVENTS") {
            // return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM);
            return true;
        } else if (item === "CONFIGS" || item === "CLASSIFICATION") {
            return this.service.hasExactRole(RegistryRoleType.RA);
        } else if (item === "SETTINGS") {
            return true;
        } else {
            return false;
        }
    }

    logout(): void {
        if (environment.production) {
            sessionStorage.removeItem("locales");

            window.location.href = environment.apiUrl + "/api/session/logout";
        }
        else {

            this.configuration.logout().catch(err => {
                // Ignore errors
                sessionStorage.removeItem("locales");

                this.service.clear();

                this.router.navigate(['/login']);
            }).then(response => {
                sessionStorage.removeItem("locales");

                this.service.clear();

                this.router.navigate(['/login']);
            });
        }
    }

    getUsername() {
        let name: string = this.service.getUsername();

        return name;
    }

    setLocale() {
        this.profileService.setLocale(this.locale).then(() => {
            // Refresh the page
            window.location.reload();
        });
    }

    account(): void {
        this.profileService.get().then(profile => {
            this.bsModalRef = this.modalService.show(ProfileComponent, { backdrop: "static", class: "gray modal-lg" });
            this.bsModalRef.content.profile = profile;
        });
    }
}
