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

import { GeoObjectType } from "@registry/model/registry";
import { Organization } from "@shared/model/core";
import { RegistryService } from "@registry/service";
import { ImportHistoryModalComponent } from "@registry/component/import-history/modals/import-history-modal.component";

enum Action {
    VIEW = 0, CREATE = 1, EDIT = 2
}

interface Selection {
    action: Action
    // params for creating
    groupSuperType?: GeoObjectType;
    isAbstract?: boolean;
    org?: Organization;

    // params for editing
    type?: GeoObjectType;
    readOnly?: boolean
}

@Component({
    selector: "geo-object-type-page",
    templateUrl: "./geo-object-type-page.component.html",
    styleUrls: ["./geo-object-type-page.css"]
})
export class GeoObjectTypePageComponent implements OnInit, OnChanges {
    Action = Action;

    @Input() userOrganization: string = null;
    @Input() organizations: Organization[] = [];
    @Input() types: GeoObjectType[] = [];

    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typesChange: EventEmitter<GeoObjectType[]> = new EventEmitter<GeoObjectType[]>()

    filter: string = "";

    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];
    filteredTypesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];

    isSRA: boolean = false;

    selection: Selection;

    constructor(

        public localizeService: LocalizationService,
        private modalService: BsModalService,
        private registryService: RegistryService,
        private authService: AuthService) {
        this.isSRA = authService.isSRA();
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

                this.typesByOrg.push({ org: org, types: types.filter(t => t.organizationCode === org.code) });
            }

            this.onFilterChange();
        }
    }

    localize(key: string): string {
        return this.localizeService.decode(key);
    }

    public findGeoObjectTypeByCode(code: string): GeoObjectType {
        for (let i = 0; i < this.types.length; ++i) {
            let got: GeoObjectType = this.types[i];

            if (got.code === code) {
                return got;
            }
        }
    }

    public findOrganizationByCode(code: string): Organization {
        for (let i = 0; i < this.organizations.length; ++i) {
            let org: Organization = this.organizations[i];

            if (org.code === code) {
                return org;
            }
        }
    }

    isRA(): boolean {
        return this.authService.isRA();
    }

    isOrganizationRA(orgCode: string, dropZone: boolean = false): boolean {
        return this.isSRA || this.authService.isOrganizationRA(orgCode);
    }

    createGeoObjectType(groupSuperType: GeoObjectType, isAbstract: boolean, org: Organization): void {
        this.selection = {
            action: Action.CREATE,
            groupSuperType,
            isAbstract,
            org
        };
    }

    handleTypeView(type: GeoObjectType): void {
        type.attributes.sort((a, b) => {
            if (a.label.localizedValue < b.label.localizedValue) return -1;
            else if (a.label.localizedValue > b.label.localizedValue) return 1;
            else return 0;
        });

        this.selection = {
            action: Action.VIEW,
            type: lodash.cloneDeep(type),
            readOnly: true
        };
    }


    deleteGeoObjectType(obj: GeoObjectType): void {
        const bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + obj.label.localizedValue + "]";
        bsModalRef.content.data = obj.code;
        bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        bsModalRef.content.type = ModalTypes.danger;

        bsModalRef.content.onConfirm.subscribe(data => {
            this.removeGeoObjectType(data);
        });
    }

    removeGeoObjectType(code: string, errCallback: (err: HttpErrorResponse) => void = null): void {
        this.registryService.deleteGeoObjectType(code).then(() => {
            const types = [...this.types];
            const index = types.findIndex(type => type.code === code);

            if (index !== -1) {
                types.splice(index, 1);

                this.typesChange.emit(types);
            }

        }).catch((err: HttpErrorResponse) => {
            if (errCallback != null) {
                errCallback(err);
            }
            this.error(err);
        });
    }

    manageGeoObjectType(type: GeoObjectType, readOnly: boolean): void {
        type.attributes.sort((a, b) => {
            if (a.label.localizedValue < b.label.localizedValue) return -1;
            else if (a.label.localizedValue > b.label.localizedValue) return 1;
            else return 0;
        });

        this.selection = {
            action: Action.EDIT,
            type: lodash.cloneDeep(type),
            readOnly
        };
    }

    handleTypeChange(type: GeoObjectType): void {
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
                readOnly: false
            };

        }

        this.typesChange.emit(types);

    }

    onFilterChange(): void {
        const label = this.filter.toLowerCase();

        this.filteredTypesByOrg = [];


        this.typesByOrg.forEach((item: { org: Organization, types: GeoObjectType[] }) => {
            const filtered = item.types.filter((type: GeoObjectType) => {
                const index = type.label.localizedValue.toLowerCase().indexOf(label);

                return (index !== -1);
            });

            this.filteredTypesByOrg.push({ org: item.org, types: filtered });
        });
    }


    onImportHistory(type: GeoObjectType): void {
        this.registryService.getImportHistory('GEO_OBJECT', type.code).then(histories => {
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


    public error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
