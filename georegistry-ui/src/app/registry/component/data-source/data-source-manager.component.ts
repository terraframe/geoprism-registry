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
import { DataSource } from "@registry/model/source";
import { DataSourceService } from "@registry/service/data-source.service";
import { NgIf, NgFor } from "@angular/common";
import { LocalizeComponent } from "../../../shared/component/localize/localize.component";
import { PageContainerComponent } from "../../../shared/component/page-container/page-container.component";
import { ModalTypes } from "@shared/model/modal";
import { ManageDataSourceModalComponent } from "./modals/manage-data-source-modal.component";

@Component({
    selector: "data-source-manager",
    templateUrl: "./data-source-manager.component.html",
    styleUrls: [],
    standalone: true,
    imports: [PageContainerComponent, LocalizeComponent, NgIf, NgFor]
})
export class DataSourceManagerComponent implements OnInit {

    message: string = null;
    sources: DataSource[];

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: DataSourceService,
        private modalService: BsModalService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {

        this.service.getAll().then(sources => {
            this.sources = sources;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(): void {

        const source: DataSource = {
            code: '',
            label: this.localizeService.create(),
            description: this.localizeService.create(),
            authority: null,
            governanceLevel: null,
            metadataProfile: null,
            uri: null
        };

        this.bsModalRef = this.modalService.show(ManageDataSourceModalComponent, {
            animated: false,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(source, false);
        this.bsModalRef.content.onSourceChange.subscribe((source: DataSource) => {
            this.sources.push(source);
        });
    }

    onEdit(source: DataSource, readOnly: boolean): void {

        console.log(source);

        this.bsModalRef = this.modalService.show(ManageDataSourceModalComponent, {
            animated: false,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init({ ...source }, readOnly);

        this.bsModalRef.content.onSourceChange.subscribe(t => {
            const index = this.sources.findIndex((tt) => source.code === tt.code);

            if (index !== -1) {
                this.sources[index] = t;
            }
        });
    }

    onDelete(source: DataSource): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + source.code + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = ModalTypes.danger;

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
