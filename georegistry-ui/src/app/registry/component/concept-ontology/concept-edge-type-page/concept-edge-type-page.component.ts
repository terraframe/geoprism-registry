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

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import * as lodash from 'lodash';

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { AuthService } from "@shared/service";
import { ManageConceptEdgeTypeComponent } from "./manage-concept-edge-type.component";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { NgIf, NgFor, NgClass } from "@angular/common";
import { AccordionModule } from "ngx-bootstrap/accordion";
import { ModalTypes } from "@shared/model/modal";
import { ConceptEdgeTypeService } from "@registry/service/concept-edge-type.service";
import { Organization } from "@shared/model/core";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";
import { RegistryService } from "@registry/service";
import { ConceptEdgeType, ConceptClass } from "@registry/model/object-class";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action

    // params for editing
    type?: ConceptEdgeType;
    readOnly?: boolean;
    isNew?: boolean;
}


@Component({
    selector: "concept-edge-type-page",
    templateUrl: "./concept-edge-type-page.component.html",
    styleUrls: ["./concept-edge-type-page.css"],
    standalone: true,
    imports: [AccordionModule, NgIf, LocalizeComponent, NgFor, NgClass, BsDropdownModule, ManageConceptEdgeTypeComponent]
})
export class ConceptEdgeTypePageComponent implements OnInit, OnDestroy, OnChanges {
    Action = Action;

    @Input() organizations: Organization[] = [];
    @Input() conceptTypes: ConceptClass[] = [];

    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()

    types: ConceptEdgeType[] = [];
    typesByOrg: { org: Organization, write: boolean, types: ConceptEdgeType[] }[] = [];

    selection: Selection;
    isSRA: boolean;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        public service: ConceptEdgeTypeService,
        private registryService: RegistryService,
        private authService: AuthService,
        private modalService: BsModalService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.isSRA = this.authService.isSRA();

        this.service.getAll().then(types => {
            this.setTypes(types);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngOnDestroy(): void {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes["organizations"]) {
            this.refreshTypesByOrg();
        }
    }

    setTypes(types: ConceptEdgeType[]): void {
        this.types = types;

        this.refreshTypesByOrg();
    }

    refreshTypesByOrg(): void {
        this.typesByOrg = [];

        for (let i = 0; i < this.organizations.length; ++i) {
            let org: Organization = this.organizations[i];

            this.typesByOrg.push({
                org: org,
                write: this.authService.isSRA() || this.authService.isOrganizationRA(org.code),
                types: this.types.filter(t => t.organizationCode === org.code)
            });
        }
    }

    onCreate(organization: Organization): void {

        this.selection = {
            action: Action.CREATE,
            type: {
                code: "",
                childType: "",
                parentType: "",
                label: this.localizeService.create(),
                description: this.localizeService.create(),
                organizationCode: organization.code,
                discreteType: ""
            },
            readOnly: false,
            isNew: true
        };
    }

    onEdit(type: ConceptEdgeType): void {
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

    handleTypeView(type: ConceptEdgeType): void {

        this.selection = {
            action: Action.VIEW,
            type: type,
            readOnly: true,
            isNew: false
        };
    }

    handleTypeChange(type: ConceptEdgeType): void {
        this.selection = null;

        const edgeTypes = [...this.types];
        const index = edgeTypes.findIndex(t => t.code === type.code);

        if (index !== -1) {
            edgeTypes[index] = type;

            this.selection = {
                action: Action.VIEW,
                type: type,
                readOnly: true,
                isNew: false
            };
        }
        else {
            edgeTypes.push(type);

            this.selection = {
                action: Action.EDIT,
                type: lodash.cloneDeep(type),
                readOnly: !this.isSRA,
                isNew: false
            };

        }

        this.setTypes(edgeTypes);
    }


    onDelete(type: ConceptEdgeType): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.label.localizedValue + "]";
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = ModalTypes.danger;

        bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(type).then(() => {
                const types = [...this.types].filter((t) => {
                    return t.code !== type.code;
                });

                this.setTypes(types);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onImportHistory(type: ConceptEdgeType): void {
        this.registryService.getImportHistory('ConceptEdgeType', type.code).then(histories => {
            const bsModalRef = this.modalService.show(ImportHistoryModalComponent, {
                animated: false,
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
