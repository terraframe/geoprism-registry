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
import { Source } from "@registry/model/source";
import { ManageSourceModalComponent } from "./modals/manage-source-modal.component";
import { SourceService } from "@registry/service/source.service";
import { read } from "@popperjs/core";

@Component({
    selector: "source-manager",
    templateUrl: "./source-manager.component.html",
    styleUrls: []
})
export class SourceManagerComponent implements OnInit {

    message: string = null;
    sources: Source[];

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(public service: SourceService, private modalService: BsModalService, private router: Router, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.service.getAll().then(sources => {
            this.sources = sources;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(): void {

        this.bsModalRef = this.modalService.show(ManageSourceModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init({ code: '' }, false);
        this.bsModalRef.content.onSourceChange.subscribe((source: Source) => {
            this.sources.push(source);
        });
    }

    onEdit(source: Source, readOnly: boolean): void {
        this.bsModalRef = this.modalService.show(ManageSourceModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(source, readOnly);

        this.bsModalRef.content.onSourceChange.subscribe(t => {
            const index = this.sources.findIndex((tt) => source.code === tt.code);

            if (index !== -1) {
                this.sources[index] = t;
            }
        });
    }

    onDelete(source: Source): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + source.code + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(source).then(() => {
                this.sources = this.sources.filter((t) => {
                    return t.code !== source.code;
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
