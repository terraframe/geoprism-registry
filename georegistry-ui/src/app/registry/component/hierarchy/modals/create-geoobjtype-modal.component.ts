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
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { GeoObjectType } from "@registry/model/registry";

import { RegistryService } from "@registry/service";
import { LocalizationService, AuthService } from "@shared/service";
import { Organization } from "@shared/model/core";
import { HierarchyType } from "@registry/model/hierarchy";

@Component({
    selector: "create-geoobjtype-modal",
    templateUrl: "./create-geoobjtype-modal.component.html",
    styleUrls: []
})
export class CreateGeoObjTypeModalComponent implements OnInit {

    geoObjectType: GeoObjectType;
    organization: Organization = null;
    message: string = null;
    parents: GeoObjectType[];
    hierarchyType: HierarchyType;
    organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onGeoObjTypeCreate: Subject<GeoObjectType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(private lService: LocalizationService, private auth: AuthService, private registryService: RegistryService, public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onGeoObjTypeCreate = new Subject();

        this.geoObjectType = {
            code: "",
            label: this.lService.create(),
            description: this.lService.create(),
            geometryType: "MULTIPOINT",
            isLeaf: false,
            isGeometryEditable: true,
            organizationCode: "",
            attributes: []
        };
    }

    init(organization: Organization, parents: GeoObjectType[], groupSuperType: GeoObjectType, isAbstract: boolean) {
        this.geoObjectType.isAbstract = isAbstract || false;

        if (groupSuperType) {
            this.geoObjectType.superTypeCode = groupSuperType.code;
            this.geoObjectType.geometryType = groupSuperType.geometryType;
            this.geoObjectType.isPrivate = groupSuperType.isPrivate;
        }

        // Filter out parents that are not abstract
        this.parents = parents.filter(parent => parent.isAbstract);

        // Filter out organizations they're not RA's of
        this.organization = organization;
        this.geoObjectType.organizationCode = this.organization.code;
        this.organizationLabel = this.organization.label.localizedValue;
    }

    handleOnSubmit(): void {
        this.message = null;

        this.registryService.createGeoObjectType(this.geoObjectType).then(data => {
            this.onGeoObjTypeCreate.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    toggleIsLeaf(): void {
        this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
    }

    toggleIsGeometryEditable(): void {
        this.geoObjectType.isGeometryEditable = !this.geoObjectType.isGeometryEditable;
    }

    toggleIsAbstract(): void {
        this.geoObjectType.isAbstract = !this.geoObjectType.isAbstract;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
