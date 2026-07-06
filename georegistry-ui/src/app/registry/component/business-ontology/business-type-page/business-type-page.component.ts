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

import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { BsModalService } from "ngx-bootstrap/modal";

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";
import * as lodash from 'lodash';

import { Organization } from "@shared/model/core";
import { RegistryService } from "@registry/service";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";
import { ManageBusinessTypeComponent } from "./manage-business-type.component";
import { CreateBusinessTypeComponent } from "./create-business-type.component";
import { BsDropdownModule } from "ngx-bootstrap/dropdown";
import { NgFor, NgIf, NgClass } from "@angular/common";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { AccordionModule } from "ngx-bootstrap/accordion";
import { FormsModule } from "@angular/forms";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { RouterLink } from "@angular/router";
import { BusinessType } from "@registry/model/object-class";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action
    // params for creating
    org?: Organization;

    // params for editing
    type?: BusinessType;
    readOnly?: boolean
}

@Component({
    selector: "business-type-page",
    templateUrl: "./business-type-page.component.html",
    styleUrls: ["./business-type-page.css"],
    standalone: true,
    imports: [FormsModule, AccordionModule, LocalizeComponent, NgFor, NgIf, NgClass, BsDropdownModule, CreateBusinessTypeComponent, ManageBusinessTypeComponent, RouterLink]
})
export class BusinessTypePageComponent implements OnInit, OnChanges {
    Action = Action;

    @Input() organizations: Organization[] = [];
    @Input() types: BusinessType[] = [];

    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typesChange: EventEmitter<BusinessType[]> = new EventEmitter<BusinessType[]>()

    filter: string = "";

    typesByOrg: { org: Organization, write: boolean, types: BusinessType[] }[] = [];

    selection: Selection;

    constructor(
        public localizeService: LocalizationService,
        private modalService: BsModalService,
        private registryService: RegistryService,
        private service: BusinessTypeService,
        private authService: AuthService) {
    }

    ngOnInit(): void {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['types'] || changes['organizations']) {
            const organizations = changes['organizations'] ? changes['organizations'].currentValue : this.organizations;
            const types = changes['types'] ? changes['types'].currentValue : this.types;

            this.typesByOrg = [];

            for (let i = 0; i < organizations.length; ++i) {
                let org: Organization = organizations[i];

                this.typesByOrg.push({
                    org: org,
                    write: this.authService.isSRA() || this.authService.isOrganizationRA(org.code),
                    types: types.filter(t => t.organization === org.code)
                });
            }
        }
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    findBusinessTypeByCode(code: string): BusinessType {
        return this.types.find(c => c.code === code);
    }

    findOrganizationByCode(code: string): Organization {
        return this.organizations.find(c => c.code === code);
    }

    createBusinessType(org: Organization): void {
        this.selection = {
            action: Action.CREATE,
            org
        };
    }

    handleTypeView(type: BusinessType): void {
        this.service.get(type.oid).then(t => {
            this.selection = {
                action: Action.VIEW,
                type: t,
                readOnly: true
            };
        }).catch(e => this.onError.emit(e))
    }

    handleEditBusinessType(type: BusinessType, readOnly: boolean): void {
        this.service.get(type.oid).then(t => {
            this.selection = {
                action: Action.EDIT,
                type: t,
                readOnly: readOnly
            };
        }).catch(e => this.onError.emit(e))
    }

    handleDeleteBusinessType(type: BusinessType): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + type.displayLabel.localizedValue + "]";
        bsModalRef.content.data = type.code;
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = ModalTypes.danger;

        bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(type).then(() => {
                const types = [...this.types];
                const index = types.findIndex(t => t.code === type.code);

                if (index !== -1) {
                    types.splice(index, 1);

                    this.typesChange.emit(types);
                }

            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }


    handleTypeChange(type: BusinessType): void {
        this.selection = null;

        const types = [...this.types];
        const index = types.findIndex(t => t.code === type.code);

        if (index !== -1) {
            types[index] = type;

            this.selection = {
                action: Action.VIEW,
                type: lodash.cloneDeep(type),
                readOnly: true
            };
        }
        else {
            types.push(type);

            this.selection = {
                action: Action.EDIT,
                type: lodash.cloneDeep(type),
                readOnly: false
            };

        }

        this.typesChange.emit(types);
    }

    onImportHistory(type: BusinessType): void {
        this.registryService.getImportHistory('BUSINESS_OBJECT', type.code).then(histories => {
            const bsModalRef = this.modalService.show(ImportHistoryModalComponent, {
                animated: false, backdrop: true,
                ignoreBackdropClick: true
            });
            bsModalRef.content.init(type.displayLabel, histories);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    public error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
