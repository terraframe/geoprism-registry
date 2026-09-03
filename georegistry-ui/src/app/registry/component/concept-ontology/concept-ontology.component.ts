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
import { ConceptClass, ConceptEdgeType, ConceptSet } from "@registry/model/object-class";
import { ConceptClassPageComponent } from "./concept-class-page/concept-class-page.component";
import { ConceptClassService } from "@registry/service/concept-class.service";
import { ConceptEdgeTypePageComponent } from "./concept-edge-type-page/concept-edge-type-page.component";
import { ConceptSetService } from "@registry/service/concept-set.service";
import { ConceptSetPageComponent } from "./concept-set-page/concept-set-page.component";
import { forkJoin, from } from "rxjs";
import { ConceptEdgeTypeService } from "@registry/service/concept-edge-type.service";


@Component({
    selector: "concept-ontology",
    templateUrl: "./concept-ontology.component.html",
    styleUrls: ["./concept-ontology.css"],
    standalone: true,
    imports: [PageContainerComponent, TabsModule, ConceptClassPageComponent, ConceptEdgeTypePageComponent, ConceptSetPageComponent, LocalizePipe]
})
export class ConceptOntologyComponent implements OnInit {

    isSRA: boolean = false;

    organizations: Organization[] = [];
    conceptClasses: ConceptClass[] = [];
    conceptEdgeTypes: ConceptEdgeType[] = [];

    constructor(
        private localizeService: LocalizationService,
        private registryService: RegistryService,
        private cClassService: ConceptClassService,
        private cEdgeTypeService: ConceptEdgeTypeService,
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

    setConceptEdgeTypes(conceptEdgeTypes: ConceptEdgeType[]): void {
        this.conceptEdgeTypes = conceptEdgeTypes;
    }

    refreshAll(): void {
        // Clear the types to then refresh
        this.conceptClasses = [];
        this.organizations = [];

        // Convert promises to observables and join them
        forkJoin([
            from(this.registryService.getOrganizations()),
            from(this.cClassService.getAll()),
            from(this.cEdgeTypeService.getAll())
        ]).subscribe({
            next: ([orgs, conceptClasses, conceptEdgeTypes]) => {
                this.organizations = orgs;
                this.setConceptClasss(conceptClasses);
                this.setConceptEdgeTypes(conceptEdgeTypes);
            },
            error: (err) => {
                this.error(err);
            }
        });
    }

    error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
