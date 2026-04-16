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

import { HierarchyType, HierarchyNode } from "@registry/model/hierarchy";
import { GeoObjectType } from "@registry/model/registry";
import { Organization } from "@shared/model/core";
import { RegistryService, HierarchyService } from "@registry/service";

import { ImportTypesModalComponent } from "./modals/import-types-modal.component";
import Utils from "@registry/utility/Utils";
import { ExportTypesModalComponent } from "./modals/export-types-modal.component";
import { environment } from "src/environments/environment";


@Component({

    selector: "geo-ontology",
    templateUrl: "./geo-ontology.component.html",
    styleUrls: ["./geo-ontology.css"]
})
export class GeoOntologyComponent implements OnInit {

    isSRA: boolean = false;

    userOrganization: string = null;

    hierarchies: HierarchyType[];
    organizations: Organization[];
    geoObjectTypes: GeoObjectType[] = [];

    constructor(
        public hierarchyService: HierarchyService,
        public localizeService: LocalizationService,
        private modalService: BsModalService,
        private registryService: RegistryService,
        private authService: AuthService) {
        this.isSRA = authService.isSRA();
    }

    ngOnInit(): void {
        this.refreshAll();
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    isRA(): boolean {
        return this.authService.isRA();
    }

    isOrganizationRA(orgCode: string, dropZone: boolean = false): boolean {
        return this.isSRA || this.authService.isOrganizationRA(orgCode);
    }

    getTypesByOrg(org: Organization): GeoObjectType[] {
        let orgTypes: GeoObjectType[] = [];

        for (let i = 0; i < this.geoObjectTypes.length; ++i) {
            let geoObjectType: GeoObjectType = this.geoObjectTypes[i];

            if (geoObjectType.organizationCode === org.code) {
                orgTypes.push(geoObjectType);
            }
        }

        return orgTypes;
    }

    getHierarchiesByOrg(org: Organization): HierarchyType[] {
        let orgHierarchies: HierarchyType[] = [];

        for (let i = 0; i < this.hierarchies.length; ++i) {
            let hierarchy: HierarchyType = this.hierarchies[i];

            if (hierarchy.organizationCode === org.code) {
                orgHierarchies.push(hierarchy);
            }
        }

        return orgHierarchies;
    }

    public refreshAll() {
        // Clear the types to then refresh
        this.geoObjectTypes = [];
        this.hierarchies = [];
        this.organizations = [];

        this.registryService.init().then(response => {
            this.localizeService.setLocales(response.locales);

            this.setGeoObjectTypes(response.types);

            this.organizations = response.organizations;

            this.organizations.forEach(org => {
                if (this.isOrganizationRA(org.code)) {
                    this.userOrganization = org.code;
                }
            });

            if (!this.authService.isSRA()) {
                let myorg = this.authService.getMyOrganizations();

                let pos = response.organizations.findIndex(org => {
                    return org.code === myorg[0];
                });

                if (pos >= 0) {
                    Utils.arrayMove(response.organizations, pos, 0);
                }
            }

            // let pos = this.getGeoObjectTypePosition("ROOT");
            // if (pos) {
            //     this.geoObjectTypes.splice(pos, 1);
            // }

            this.setHierarchyTypes(response.hierarchies);

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public setGeoObjectTypes(types: GeoObjectType[]): void {
        this.geoObjectTypes = [];

        // Set group parent types
        this.setAbstractTypes(types);

        // Set GeoObjectTypes that aren't part of a group.
        types.forEach(type => {
            if (!type.isAbstract) {
                if (!type.superTypeCode) {
                    this.geoObjectTypes.push(type);
                }
            }
        });

        // Sort aphabetically because all other types to add will be children in a group.
        this.geoObjectTypes.sort((a, b) => {
            if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
            else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
            else return 0;
        });

        // Add group children
        types.forEach(type => {
            if (!type.isAbstract) {
                if (type.superTypeCode && type.superTypeCode.length > 0) {
                    for (let i = 0; i < this.geoObjectTypes.length; i++) {
                        const setType = this.geoObjectTypes[i];

                        if (type.superTypeCode === setType.code) {
                            this.geoObjectTypes.splice(i + 1, 0, type);
                        }
                    }
                }
            }
        });
    }

    private setAbstractTypes(types: GeoObjectType[]): void {
        types.forEach(type => {
            if (type.isAbstract) {
                this.geoObjectTypes.push(type);
            }
        });
    }




    setHierarchyTypes(data: HierarchyType[]): void {
        const hierarchies: HierarchyType[] = [];

        data.forEach((hierarchyType, index) => {
            if (hierarchyType.rootGeoObjectTypes.length > 0) {
                hierarchyType.rootGeoObjectTypes.forEach(rootGeoObjectType => {
                    this.processHierarchyNodes(rootGeoObjectType);
                });
            }

            hierarchies.push(hierarchyType);
        });

        this.hierarchies = hierarchies;

        this.hierarchies.sort((a, b) => {
            if (a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase()) return -1;
            else if (a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase()) return 1;
            else return 0;
        });
    }

    private processHierarchyNodes(node: HierarchyNode) {
        if (node != null) {
            node.label = this.getHierarchyLabel(node.geoObjectType);

            node.children.forEach(child => {
                this.processHierarchyNodes(child);
            });
        }
    }

    private getHierarchyLabel(geoObjectTypeCode: string): string {
        let label: string = null;
        this.geoObjectTypes.forEach(function (gOT) {
            if (gOT.code === geoObjectTypeCode) {
                label = gOT.label.localizedValue;
            }
        });

        return label;
    }

    handleRemoveHierarchyType(code: string): void {
        const hierarchies = [...this.hierarchies];

        const index = hierarchies.findIndex(t => t.code === code);

        if (index != -1) {
            hierarchies.splice(index, 1);
        }

        this.setHierarchyTypes(hierarchies);
    }

    handleHierarchyType(type: HierarchyType): void {
        const hierarchies = [...this.hierarchies];

        const index = hierarchies.findIndex(t => t.code === type.code);

        if (index != -1) {
            hierarchies[index] = type;
        }
        else {
            hierarchies.push(type);
        }

        this.setHierarchyTypes(hierarchies);
    }

    handleGeoObjectType(type: GeoObjectType): void {
        const geoObjectTypes = [...this.geoObjectTypes];

        const index = geoObjectTypes.findIndex(t => t.code === type.code);

        if (index != -1) {
            geoObjectTypes[index] = type;
        }
        else {
            geoObjectTypes.push(type);
        }

        this.setGeoObjectTypes(geoObjectTypes);
    }



    public importTypes(): void {
        const bsModalRef = this.modalService.show(ImportTypesModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });

        bsModalRef.content.init(this.organizations);

        bsModalRef.content.onNodeChange.subscribe(data => {
            // Reload the page
            this.refreshAll();
        });
    }

    public exportTypes(): void {
        const bsModalRef = this.modalService.show(ExportTypesModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            class: "upload-modal"
        });

        bsModalRef.content.init(this.organizations);

        bsModalRef.content.onNodeChange.subscribe(orgCode => {
            if (orgCode != null && orgCode.length > 0) {
                window.location.href = environment.apiUrl + "/api/cgr/export-types?code=" + orgCode;
            }
        });
    }

    public error(err: HttpErrorResponse): void {
        ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
