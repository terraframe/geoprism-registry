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

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { NgIf, NgFor } from "@angular/common";
import { LocalizeComponent } from "../../../shared/component/localize/localize.component";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";
import { ModalTypes } from "@shared/model/modal";
import { SourceAuthority } from "@registry/model/source";
import { SourceAuthorityService } from "@registry/service/source-authority.service";
import { ManageSourceAuthorityModalComponent } from "./modals/manage-source-authority-modal.component";

@Component({
    selector: "source-authority-manager",
    templateUrl: "./source-authority-manager.component.html",
    styleUrls: [],
    standalone: true,
    imports: [PageContainerComponent, LocalizeComponent, NgIf, NgFor]
})
export class SourceAuthorityManagerComponent implements OnInit {

    message: string = null;
    authorities: SourceAuthority[];

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(public service: SourceAuthorityService, private modalService: BsModalService, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.service.getAll().then(authorities => {
            this.authorities = authorities;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(): void {

        const authority: SourceAuthority = {
            code: '',
            label: this.localizeService.create(),
            description: this.localizeService.create(),
            authorityType: null
        };

        this.bsModalRef = this.modalService.show(ManageSourceAuthorityModalComponent, {
            animated: false,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(authority, false);
        this.bsModalRef.content.onSourceChange.subscribe((authority: SourceAuthority) => {
            this.authorities.push(authority);
        });
    }

    onEdit(authority: SourceAuthority, readOnly: boolean): void {
        this.bsModalRef = this.modalService.show(ManageSourceAuthorityModalComponent, {
            animated: false,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init({ ...authority }, readOnly);

        this.bsModalRef.content.onSourceChange.subscribe(t => {
            const index = this.authorities.findIndex((tt) => authority.code === tt.code);

            if (index !== -1) {
                this.authorities[index] = t;
            }
        });
    }

    onDelete(authority: SourceAuthority): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + authority.code + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = ModalTypes.danger;

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(authority).then(() => {
                this.authorities = this.authorities.filter((t) => {
                    return t.code !== authority.code;
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
