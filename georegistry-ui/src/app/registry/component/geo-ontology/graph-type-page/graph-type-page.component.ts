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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import * as lodash from 'lodash';

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { GraphTypeService } from "@registry/service/graph-type.service";
import { GraphType } from "@registry/model/registry";
import { RegistryService } from "@registry/service";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";
import { AuthService } from "@shared/service";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action

    // params for editing
    type?: GraphType;
    readOnly?: boolean
}


@Component({
    selector: "graph-type-page",
    templateUrl: "./graph-type-page.component.html",
    styleUrls: ["./graph-type-page.css"]
})
export class GraphTypePageComponent implements OnInit, OnDestroy {
    Action = Action;

    @Input() typeCode: string;
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()

    types: GraphType[];

    selection: Selection;
    isSRA: boolean;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        public service: GraphTypeService,
        private registryService: RegistryService,
        private authService: AuthService,
        private modalService: BsModalService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.isSRA = this.authService.isSRA();

        this.service.getAllForType(this.typeCode).then(types => {
            this.types = types;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngOnDestroy(): void {
    }

    onCreate(): void {

        this.selection = {
            action: Action.CREATE,
            type: {
                code: "",
                typeCode: this.typeCode,
                label: this.localizeService.create(),
                description: this.localizeService.create(),
            },
            readOnly: false
        };
    }

    onEdit(type: GraphType): void {
        this.service.get(this.typeCode, type.code).then(t => {
            this.selection = {
                action: Action.EDIT,
                type: type,
                readOnly: !this.isSRA
            };
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleTypeView(type: GraphType): void {

        this.selection = {
            action: Action.VIEW,
            type: type,
            readOnly: true
        };
    }

    handleTypeChange(type: GraphType): void {
        this.selection = null;

        const types = [...this.types];
        const index = types.findIndex(t => t.code === type.code);

        if (index !== -1) {
            types[index] = type;
        }
        else {
            types.push(type);

            this.selection = {
                action: Action.EDIT,
                type: lodash.cloneDeep(type),
                readOnly: !this.isSRA
            };

        }

        this.types = types;

        // this.typesChange.emit(types);

    }


    onDelete(type: GraphType): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.label.localizedValue + "]";
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = "danger";

        bsModalRef.content.onConfirm.subscribe(data => {
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
            const bsModalRef = this.modalService.show(ImportHistoryModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });
            bsModalRef.content.init(type.label, histories);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
