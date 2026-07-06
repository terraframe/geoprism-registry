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

import { Component, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from "ngx-bootstrap/modal";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { Organization } from "@shared/model/core";

import Utils from "@registry/utility/Utils";
import { TabsModule } from "ngx-bootstrap/tabs";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";
import { RegistryService } from "@registry/service";
import { LocalizePipe } from "@shared/pipe/localize.pipe";
import { ConceptClass } from "@registry/model/object-class";
import { ConceptClassPageComponent } from "./concept-class-page/concept-class-page.component";
import { ConceptClassService } from "@registry/service/concept-class.service";


@Component({
    selector: "concept-ontology",
    templateUrl: "./concept-ontology.component.html",
    styleUrls: ["./concept-ontology.css"],
    standalone: true,
    imports: [PageContainerComponent, TabsModule, ConceptClassPageComponent, LocalizePipe]
})
export class ConceptOntologyComponent implements OnInit {

    isSRA: boolean = false;

    organizations: Organization[] = [];
    conceptClasses: ConceptClass[] = [];

    constructor(
        private localizeService: LocalizationService,
        private registryService: RegistryService,
        private service: ConceptClassService,
        private modalService: BsModalService,
        private authService: AuthService) {
        this.isSRA = authService.isSRA();
    }

    ngOnInit(): void {
        this.refreshAll();
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    isOrganizationRA(orgCode: string): boolean {
        return this.isSRA || this.authService.isOrganizationRA(orgCode);
    }

    setConceptClasss(types: ConceptClass[]): void {
        this.conceptClasses = types;
    }

    refreshAll(): void {
        // Clear the types to then refresh
        this.conceptClasses = [];
        this.organizations = [];

        this.registryService.getOrganizations().then(orgs => {

            if (!this.authService.isSRA()) {
                const myorg = this.authService.getMyOrganizations();

                let index = orgs.findIndex(org => {
                    return org.code === myorg[0];
                });

                if (index !== -1) {
                    Utils.arrayMove(orgs, index, 0);
                }
            }

            this.organizations = orgs
        });


        this.service.getAll().then(conceptClasses => {
            this.setConceptClasss(conceptClasses);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleConceptClass(type: ConceptClass): void {
        const conceptClasses = [...this.conceptClasses];

        const index = conceptClasses.findIndex(t => t.code === type.code);

        if (index != -1) {
            conceptClasses[index] = type;
        }
        else {
            conceptClasses.push(type);
        }

        this.setConceptClasss(conceptClasses);
    }

    error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
