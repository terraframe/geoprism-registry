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

import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { GeoObjectType } from "@registry/model/registry";

import { RegistryService } from "@registry/service";
import { LocalizationService } from "@shared/service";
import { Organization } from "@shared/model/core";

@Component({
    selector: "create-geo-object-type",
    templateUrl: "./create-geo-object-type.component.html",
    styleUrls: []
})
export class CreateGeoObjectTypeComponent implements OnInit {

    @Input() organization: Organization = null;
    @Input() parents: GeoObjectType[] = [];
    @Input() groupSuperType: GeoObjectType = null;
    @Input() isAbstract: boolean;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>()

    geoObjectType: GeoObjectType = null;

    organizationLabel: string;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private lService: LocalizationService,
        private registryService: RegistryService) { }

    ngOnInit(): void {
        this.geoObjectType = {
            code: "",
            label: this.lService.create(),
            description: this.lService.create(),
            geometryType: "MULTIPOINT",
            isLeaf: false,
            isGeometryEditable: true,
            organizationCode: this.organization.code,
            isAbstract: (this.isAbstract || false),
            attributes: []
        };

        if (this.groupSuperType) {
            this.geoObjectType.superTypeCode = this.groupSuperType.code;
            this.geoObjectType.geometryType = this.groupSuperType.geometryType;
            this.geoObjectType.isPrivate = this.groupSuperType.isPrivate;
        }

        this.organizationLabel = this.organization.label.localizedValue;



        // Filter out parents that are not abstract
        this.parents = this.parents.filter(parent => parent.isAbstract);
    }

    handleCancel(): void {
        this.onCancel.emit();
    }

    handleOnSubmit(): void {

        this.registryService.createGeoObjectType(this.geoObjectType).then(data => {
            this.typeChange.emit(data);
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
        this.onError.emit(err);
    }

}
