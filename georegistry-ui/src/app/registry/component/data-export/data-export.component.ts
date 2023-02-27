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
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ErrorModalComponent } from "@shared/component";

import { IOService } from "@registry/service";
import { AuthService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({

    selector: "data-export",
    templateUrl: "./data-export.component.html",
    styleUrls: []
})
export class DataExportComponent implements OnInit {

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[]

    /*
     * Currently selected code
     */
    code: string = null;

    /*
     * List of the hierarchies this type is part of
     */
    hierarchies: { label: string, code: string }[] = [];

    /*
     * Currently selected hierarchy
     */
    hierarchy: string = null;

    /*
     * Currently selected format
     */
    format: string = null;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: IOService, private modalService: BsModalService, private authService: AuthService) { }

    ngOnInit(): void {
        this.service.listGeoObjectTypes(true).then(types => {
            // this.types = types;

            let myOrgTypes = [];
            for (let i = 0; i < types.length; ++i) {
                if (this.authService.isOrganizationRA(types[i].orgCode)) {
                    myOrgTypes.push(types[i]);
                }
            }
            this.types = myOrgTypes;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onChange(event: Event): void {
        const code = (event.target as HTMLInputElement).value;

        if (code != null && code.length > 0) {
            this.service.getHierarchiesForType(code, false).then(hierarchies => {
                this.hierarchies = hierarchies;
                this.hierarchy = null;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.hierarchies = [];
            this.hierarchy = null;
        }
    }

    onExport(): void {
        if (this.format === "SHAPEFILE") {
            window.location.href = environment.apiUrl + "/shapefile/export-shapefile?type=" + this.code + "&hierarchyType=" + this.hierarchy;
        } else if (this.format === "EXCEL") {
            window.location.href = environment.apiUrl + "/api/excel/export-spreadsheet?type=" + this.code + "&hierarchyType=" + this.hierarchy;
        }
    }

    public error(err: HttpErrorResponse): void {
        this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
