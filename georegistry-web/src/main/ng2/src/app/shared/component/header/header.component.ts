import { Component, Input } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ProfileComponent } from "../profile/profile.component";

import { AuthService, ProfileService, LocalizationService } from "@shared/service";

import { RegistryRoleType, LocaleView } from "@shared/model/core";

import { GeoRegistryConfiguration } from "@core/model/registry";
declare let registry: GeoRegistryConfiguration;

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
        localizationService: LocalizationService
    ) {
        this.context = registry.contextPath;
        this.isAdmin = service.isAdmin();
        this.isMaintainer = this.isAdmin || service.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

        this.enableBusinessData = registry.enableBusinessData || false;

        if (localizationService.getLocales()) {
            this.locales = localizationService.getLocales().filter(locale => locale.toString !== "defaultLocale");
            this.defaultLocaleView = localizationService.getLocales().filter(locale => locale.toString === "defaultLocale")[0];
        } else {
            this.locales = [];
            this.defaultLocaleView = null;
        }
        this.locale = localizationService.getLocale();

        let found: boolean = false;

        for (let i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].toString === this.locale) {
                found = true;
            }
        }

        if (!found) {
            this.locale = "";
        }
    }

    shouldShowMenuItem(item: string): boolean {
        if (item === "HIERARCHIES") {
            return true;
        } else if (item === "LISTS") {
            // return this.service.hasExactRole(RegistryRoleType.SRA) || this.service.hasExactRole(RegistryRoleType.RA) || this.service.hasExactRole(RegistryRoleType.RM) || this.service.hasExactRole(RegistryRoleType.RC) || this.service.hasExactRole(RegistryRoleType.AC);
            return true;
        } else if (item === "BUSINESS-TYPES") {
            if (registry.enableBusinessData) {
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
        sessionStorage.removeItem("locales");

        window.location.href = registry.contextPath + "/session/logout";

        //        this.sessionService.logout().then( response => {
        //            this.router.navigate( ['/login'] );
        //        } );
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
