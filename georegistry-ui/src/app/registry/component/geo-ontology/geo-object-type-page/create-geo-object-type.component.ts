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

import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { GeoObjectType } from "@registry/model/registry";
import * as lodash from 'lodash';

import { RegistryService } from "@registry/service";
import { LocalizationService } from "@shared/service";
import { Organization } from "@shared/model/core";
import { BooleanFieldComponent } from "@shared/component/form-fields/boolean-field/boolean-field.component";
import { LocalizedTextComponent } from "../../form-fields/localized-text/localized-text.component";
import { ConvertKeyLabel } from "@shared/component/localize/convert-key-label.component";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { NgIf, NgFor, NgClass } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ConceptSetService } from "@registry/service/concept-set.service";
import { ConceptSet, ObjectOverTime } from "@registry/model/object-class";
import { DateFieldComponent } from "@shared/component";
import { Observable, Observer } from "rxjs";
import { TypeaheadMatch, TypeaheadModule } from "ngx-bootstrap/typeahead";
import { ConceptObjectService } from "@registry/service/concept-object.service";

@Component({
    selector: "create-geo-object-type",
    templateUrl: "./create-geo-object-type.component.html",
    styleUrls: [],
    standalone: true,
    imports: [NgIf, NgFor, NgClass, FormsModule, LocalizeComponent, ConvertKeyLabel, LocalizedTextComponent, BooleanFieldComponent, DateFieldComponent, TypeaheadModule]
})
export class CreateGeoObjectTypeComponent implements OnInit {

    @Input() organization: Organization = null;
    @Input() parents: GeoObjectType[] = [];
    @Input() groupSuperType: GeoObjectType = null;
    @Input() isAbstract: boolean;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>()

    geoObjectType: GeoObjectType = null;

    organizationLabel: string;

    sets: ConceptSet[] = [];
    typeahead: Observable<ObjectOverTime[]> = null;
    text: string;
    loading: boolean;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private lService: LocalizationService,
        private registryService: RegistryService,
        private setService: ConceptSetService,
        private cObjectService: ConceptObjectService,
    ) { }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<ObjectOverTime[]>) => {
            if (this.geoObjectType.classification != null
                && this.geoObjectType.classification.conceptSet != null
                && this.geoObjectType.classification.conceptSet.length > 0
                && this.geoObjectType.classification.startDate != null
                && this.geoObjectType.classification.startDate.length > 0

            ) {
                this.cObjectService.searchSet(this.geoObjectType.classification.conceptSet, this.geoObjectType.classification.startDate, this.text).then(results => {
                    observer.next(results);
                });
            }
        });

        this.geoObjectType = {
            code: "",
            label: this.lService.create(),
            description: this.lService.create(),
            geometryType: "MULTIPOINT",
            isLeaf: false,
            isGeometryEditable: true,
            organizationCode: this.organization.code,
            isAbstract: (this.isAbstract || false),
            attributes: [],
            classification: {
                code: "classification",
                type: "classification",
                label: this.lService.create("Classification"),
                description: this.lService.create("Classification"),
                isDefault: true,
                required: false,
                unique: false,
                conceptSet: "",
                startDate: "",
                endDate: "",
            }
        };

        if (this.groupSuperType) {
            this.geoObjectType.superTypeCode = this.groupSuperType.code;
            this.geoObjectType.geometryType = this.groupSuperType.geometryType;
            this.geoObjectType.isPrivate = this.groupSuperType.isPrivate;
        }

        this.organizationLabel = this.organization.label.localizedValue;



        // Filter out parents that are not abstract
        this.parents = this.parents.filter(parent => parent.isAbstract);

        this.setService.getAll().then(sets => this.sets = sets);
    }

    handleCancel(): void {
        this.onCancel.emit();
    }

    handleOnSubmit(): void {
        const type = lodash.cloneDeep(this.geoObjectType)

        if (type.classification.conceptSet == null ||
            type.classification.conceptSet.trim().length == 0) {
            delete type.classification;
        }

        this.registryService.createGeoObjectType(type).then(data => {
            this.typeChange.emit(data);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    toggleIsLeaf(): void {
        this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
    }

    toggleIsGeometryEditable(): void {
        this.geoObjectType.isGeometryEditable = !this.geoObjectType.isGeometryEditable;
    }

    toggleIsAbstract(): void {
        this.geoObjectType.isAbstract = !this.geoObjectType.isAbstract;
    }

    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: ObjectOverTime = match.item;
            this.text = item.code;

            if (this.geoObjectType.classification.rootTerm == null || this.geoObjectType.classification.rootTerm.code !== item.code) {
                this.geoObjectType.classification.rootTerm = { code: item.code, type: item.type.typeCode };
            }
        } else if (this.geoObjectType.classification.rootTerm != null) {
            this.geoObjectType.classification.rootTerm = null;
        }
    }

    onTextChange(): void {
        if (this.geoObjectType.classification.rootTerm != null && (this.text == null || this.text.length === 0)) {
            this.geoObjectType.classification.rootTerm = null;
        }
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
