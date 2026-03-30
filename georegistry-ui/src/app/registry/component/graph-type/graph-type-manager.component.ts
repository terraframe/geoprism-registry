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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { GraphTypeService } from "@registry/service/graph-type.service";
import { CreateGraphTypeModalComponent } from "./modals/create-graph-type-modal.component";
import { ManageGraphTypeModalComponent } from "./modals/manage-graph-type-modal.component";
import { GraphType } from "@registry/model/registry";
import { Subscription } from "rxjs";
import { ImportHistoryModalComponent } from "../import-history/modals/import-history-modal.component";
import { RegistryService } from "@registry/service";

@Component({
    selector: "graph-type-manager",
    templateUrl: "./graph-type-manager.component.html",
    styleUrls: ["./graph-type-manager.css"]
})
export class GraphTypeManagerComponent implements OnInit, OnDestroy {

    message: string = null;

    private paramsSub!: Subscription;
    types: GraphType[];
    typeCode: string;

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        public service: GraphTypeService,
        private registryService: RegistryService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private route: ActivatedRoute) { }

    ngOnInit(): void {
        this.paramsSub = this.route.paramMap.subscribe(paramMap => {
            this.typeCode = paramMap.get('typeCode');

            this.service.getAllForType(this.typeCode).then(types => {
                this.types = types;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });

    }

    ngOnDestroy(): void {
        this.paramsSub.unsubscribe();
    }

    onCreate(): void {

        this.bsModalRef = this.modalService.show(CreateGraphTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init();
        this.bsModalRef.content.onGraphTypeChange.subscribe((type: GraphType) => {
            this.types.push(type);
        });
    }


    onEdit(type: GraphType): void {
        this.service.get(this.typeCode, type.code).then(t => {
            this.bsModalRef = this.modalService.show(ManageGraphTypeModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(this.typeCode, t, false);

            this.bsModalRef.content.onGraphTypeChange.subscribe(t => {
                const index = this.types.findIndex((tt) => type.code === tt.code);

                if (index !== -1) {
                    this.types[index] = t;
                }
            });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onDelete(type: GraphType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.label.localizedValue + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(this.typeCode, type).then(() => {
                this.types = this.types.filter((t) => {
                    return t.code !== type.code;
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onImportHistory(type: GraphType): void {
        this.registryService.getImportHistory(this.typeCode, type.code).then(histories => {
            this.bsModalRef = this.modalService.show(ImportHistoryModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            this.bsModalRef.content.init(type.label, histories);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
