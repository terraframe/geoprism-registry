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
import { HierarchyType } from "@registry/model/hierarchy";
import { RegistryService, HierarchyService } from "@registry/service";

import { LocalizationService, AuthService } from "@shared/service";

@Component({
    selector: "create-hierarchy-type-modal",
    templateUrl: "./create-hierarchy-type-modal.component.html",
    styleUrls: []
})
export class CreateHierarchyTypeModalComponent implements OnInit {

    hierarchyType: HierarchyType;
    organizations: any = [];
    message: string = null;

    edit: boolean = false; // if true, we are updating an existing. If false, we are creating new

    readOnly: boolean = false;

    organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onHierarchytTypeCreate: Subject<HierarchyType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(private lService: LocalizationService, private auth: AuthService, private registryService: RegistryService, private hierarchyService: HierarchyService, public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onHierarchytTypeCreate = new Subject();

        this.hierarchyType = {
            code: "",
            label: this.lService.create(),
            description: this.lService.create(),
            rootGeoObjectTypes: [],
            organizationCode: ""
        };

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
                this.organizationLabel = this.getOrganizationLabelFromCode(this.hierarchyType.organizationCode);
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    getOrganizationLabelFromCode(orgCode: string) {
        for (var i = 0; i < this.organizations.length; ++i) {
            if (this.organizations[i].code === orgCode) {
                return this.organizations[i].label.localizedValue;
            }
        }

        console.log("Did not find org with code [" + orgCode + "]");
        return orgCode;
    }

    handleOnSubmit(): void {
        this.message = null;

        if (this.readOnly) {
            this.bsModalRef.hide();
            return;
        }

        if (this.edit) {
            this.hierarchyService.updateHierarchyType(this.hierarchyType).then(data => {
                this.onHierarchytTypeCreate.next(data);
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.hierarchyService.createHierarchyType(this.hierarchyType).then(data => {
                this.onHierarchytTypeCreate.next(data);
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
