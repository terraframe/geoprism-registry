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
import { Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { BusinessType, BusinessTypeByOrg } from "@registry/model/business-type";
import { CreateBusinessTypeModalComponent } from "./modals/create-business-type-modal.component";
import { ManageBusinessTypeModalComponent } from "./modals/manage-business-type-modal.component";

@Component({
    selector: "business-type-manager",
    templateUrl: "./business-type-manager.component.html",
    styleUrls: ["./business-type-manager.css"]
})
export class BusinessTypeManagerComponent implements OnInit {

    message: string = null;
    orgs: BusinessTypeByOrg[];

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(public service: BusinessTypeService, private modalService: BsModalService, private router: Router, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.service.getByOrganization().then(orgs => {
            this.orgs = orgs;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(org: BusinessTypeByOrg): void {

        this.bsModalRef = this.modalService.show(CreateBusinessTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(org);
        this.bsModalRef.content.onBusinessTypeChange.subscribe((type: BusinessType) => {
            org.types.push(type);
        });
    }

    onView(type: BusinessType): void {
        this.service.get(type.oid).then(t => {
            this.bsModalRef = this.modalService.show(ManageBusinessTypeModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(t, true);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onEdit(org: BusinessTypeByOrg, type: BusinessType): void {
        this.service.edit(type.oid).then(t => {
            this.bsModalRef = this.modalService.show(ManageBusinessTypeModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(t, false);

            this.bsModalRef.content.onBusinessTypeChange.subscribe(t => {
                const index = org.types.findIndex((tt) => type.code === tt.code);

                if (index !== -1) {
                    org.types[index] = t;
                }
            });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onDelete(org: BusinessTypeByOrg, type: BusinessType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.displayLabel.localizedValue + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(type).then(() => {
                org.types = org.types.filter((t) => {
                    return t.code !== type.code;
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
