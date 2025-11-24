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

import { Component, EventEmitter, Input, Output } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { ProfileComponent } from "../profile/profile.component";

import { AuthService, ProfileService } from "@shared/service";

import { RegistryRoleType } from "@shared/model/core";

import { environment } from 'src/environments/environment';
import { ConfigurationService } from "@core/service/configuration.service";
import { LocaleView, MenuSection } from "@core/model/core";
import { Router } from "@angular/router";
import EnvironmentUtil from "@core/utility/environment-util";
import { HubService } from "@core/service/hub.service";

@Component({

    selector: "side-nav",
    templateUrl: "./side-nav.component.html",
    styleUrls: ['./side-nav.css']
})
export class SideNavComponent {



    context: string;

    sections: MenuSection[];

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;
    isPublic: boolean = true;

    defaultLocaleView: LocaleView;
    locales: LocaleView[];
    locale: string;

    enableBusinessData: boolean = false;

    @Input() loggedIn: boolean = true;
    @Input() expanded: boolean = true;

    constructor(
        private hService: HubService,
        private service: AuthService
    ) {
        this.context = EnvironmentUtil.getApiUrl();
        this.sections = hService.getMenuSections();

        this.isPublic = service.isPublic();
    }

    handleToggle(): void {
        this.hService.setExpanded(!this.expanded);
    }

    shouldShowMenuItem(item: string): boolean {
        return this.service.shouldShowMenuItem(item);
    }
}
