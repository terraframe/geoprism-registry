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

import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { HierarchyType } from "@registry/model/hierarchy";
import { RegistryService, HierarchyService } from "@registry/service";

import { AuthService } from "@shared/service";
import { NgForm } from "@angular/forms";

@Component({
    selector: "hierarchy-type",
    templateUrl: "./hierarchy-type.component.html",
    styleUrls: ["./hierarchy-type.css"]
})
export class HierarchyTypeComponent implements OnInit, AfterViewInit {

    @Input() hierarchyType: HierarchyType;

    @Input() edit: boolean = false; // if true, we are updating an existing. If false, we are creating new

    @Input() readOnly: boolean = false;


    @Output() onClose: EventEmitter<{ edit: boolean, hierarchy: HierarchyType }> = new EventEmitter<{ edit: boolean, hierarchy: HierarchyType }>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    // @Output() hierarchyTypeChange: EventEmitter<HierarchyType> = new EventEmitter<HierarchyType>()

    @ViewChild('form') myForm!: NgForm;

    organizations: any = [];

    organizationLabel: string;

    // eslint-disable-next-line no-useless-constructor
    constructor(private auth: AuthService, private registryService: RegistryService, private hierarchyService: HierarchyService) { }

    ngOnInit(): void {

        this.registryService.getOrganizations().then(orgs => {
            // Filter out organizations they're not RA's of, unless we're readOnly.
            if (!this.readOnly) {
                this.organizations = [];

                for (var i = 0; i < orgs.length; ++i) {
                    if (this.auth.isOrganizationRA(orgs[i].code)) {
                        this.organizations.push(orgs[i]);
                    }
                }
            } else {
                this.organizations = orgs;
            }

            if (!this.edit && this.organizations.length === 1) {
                this.hierarchyType.organizationCode = this.organizations[0].code;
                this.organizationLabel = this.organizations[0].label.localizedValue;
            } else if (this.edit || this.readOnly) {
                this.organizationLabel = this.organizations.find(o => o.code === this.hierarchyType.organizationCode).label.localizedValue;
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngAfterViewInit(): void {
    }

    handleClose(): void {
        if (this.edit && this.myForm.dirty
        ) {
            this.onClose.emit({ edit: this.edit, hierarchy: this.hierarchyType });
        }

        this.onClose.emit();
    }


    // handleOnSubmit(): void {

    //     if (this.readOnly) {
    //         this.onCancel.emit();
    //         return;
    //     }

    //     if (this.edit) {
    //         this.hierarchyService.updateHierarchyType(this.hierarchyType).then(data => {
    //             this.hierarchyTypeChange.emit(data);
    //         }).catch((err: HttpErrorResponse) => {
    //             this.error(err);
    //         });
    //     } else {
    //         this.hierarchyService.createHierarchyType(this.hierarchyType).then(data => {
    //             this.hierarchyTypeChange.emit(data);
    //         }).catch((err: HttpErrorResponse) => {
    //             this.error(err);
    //         });
    //     }
    // }

    // handleCancel(): void {
    //     this.onCancel.emit();
    // }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
