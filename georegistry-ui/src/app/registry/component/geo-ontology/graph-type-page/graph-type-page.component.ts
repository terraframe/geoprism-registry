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
import { RegistryService } from "@registry/service";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";
import { AuthService } from "@shared/service";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { NgIf, NgFor, NgClass } from "@angular/common";
import { AccordionModule } from "ngx-bootstrap/accordion";
import { ModalTypes } from "@shared/model/modal";
import { DagTypeService } from "@registry/service/dag-type.service";
import { EdgeClassService } from "@registry/service/edge-class.service";
import { GraphClass } from "@registry/model/object-class";
import { ManageGraphTypeComponent } from "./manage-graph-type.component";
import { UndirectedGraphTypeService } from "@registry/service/undirected-graph-type.service";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action

    // params for editing
    type?: GraphClass;
    readOnly?: boolean;
    isNew?: boolean;
}


@Component({
    selector: "graph-type-page",
    templateUrl: "./graph-type-page.component.html",
    styleUrls: ["./graph-type-page.css"],
    standalone: true,
    imports: [AccordionModule, NgIf, LocalizeComponent, NgFor, NgClass, BsDropdownModule, ManageGraphTypeComponent]
})
export class GraphTypePageComponent implements OnInit, OnDestroy {
    Action = Action;

    @Input() typeCode: string;
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()

    types: GraphClass[];

    selection: Selection;
    isSRA: boolean;
    service: EdgeClassService<GraphClass> = null

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private dagService: DagTypeService,
        private undirectedService: UndirectedGraphTypeService,
        private registryService: RegistryService,
        private authService: AuthService,
        private modalService: BsModalService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.isSRA = this.authService.isSRA();

        console.log(this.typeCode)

        this.service = this.typeCode === 'DirectedAcyclicGraphType' ? this.dagService : this.undirectedService;

        this.service.getAll().then(types => {
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
            readOnly: false,
            isNew: true
        };
    }

    onEdit(type: GraphClass): void {
        this.service.get(type.code).then(t => {
            this.selection = {
                action: Action.EDIT,
                type: type,
                readOnly: !this.isSRA,
                isNew: false
            };
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleTypeView(type: GraphClass): void {

        this.selection = {
            action: Action.VIEW,
            type: type,
            readOnly: true,
            isNew: false
        };
    }

    handleTypeChange(type: GraphClass): void {
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
                readOnly: !this.isSRA,
                isNew: false
            };

        }

        this.types = types;

        // this.typesChange.emit(types);

    }


    onDelete(type: GraphClass): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.label.localizedValue + "]";
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = ModalTypes.danger;

        bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(type).then(() => {
                this.types = this.types.filter((t) => {
                    return t.code !== type.code;
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onImportHistory(type: GraphClass): void {
        this.registryService.getImportHistory(this.typeCode, type.code).then(histories => {
            const bsModalRef = this.modalService.show(ImportHistoryModalComponent, {

                animated: false, backdrop: true,
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
