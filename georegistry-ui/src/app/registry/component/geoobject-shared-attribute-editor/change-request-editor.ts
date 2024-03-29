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

import { ChangeType } from "@registry/model/constants";
import { ChangeRequest } from "@registry/model/crtable";
import { AttributeType, GeoObjectOverTime, GeoObjectType, HierarchyOverTime, ValueOverTime } from "@registry/model/registry";
import { GeometryService, RegistryService } from "@registry/service";
import { VotService } from "@registry/service/vot.service";
import { DateService, LocalizationService } from "@shared/service";
import { Subject } from "rxjs";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";

export class ChangeRequestEditor {

    changeRequest: ChangeRequest;

    // eslint-disable-next-line no-undef
    attributeEditors: (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[];

    geometryAttributeType: AttributeType;

    parentAttributeType: AttributeType;

    geoObject: GeoObjectOverTime;

    geoObjectType: GeoObjectType;

    hierarchies: HierarchyOverTime[];

    private _isValid: boolean;

    onChangeSubject : Subject<ChangeType> = new Subject<ChangeType>();

    localizationService: LocalizationService;

    dateService: DateService;

    registryService: RegistryService;

    geomService: GeometryService;

    votService: VotService;

    constructor(changeRequest: ChangeRequest, geoObject: GeoObjectOverTime, geoObjectType: GeoObjectType, hierarchies: HierarchyOverTime[], geometryAttributeType: AttributeType, parentAttributeType: AttributeType, localizationService: LocalizationService, dateService: DateService, registryService: RegistryService, geomService: GeometryService, votService: VotService) {
        this.changeRequest = changeRequest;
        this.geoObject = geoObject;
        this.geoObjectType = geoObjectType;
        this.geometryAttributeType = geometryAttributeType;
        this.parentAttributeType = parentAttributeType;
        this.hierarchies = hierarchies;
        this.localizationService = localizationService;
        this.dateService = dateService;
        this.registryService = registryService;
        this.geomService = geomService;
        this.votService = votService;

        this.attributeEditors = this.generateAttributeEditors();
        this.validate();
    }

    private generateAttributeEditors() {
        let geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

        let editors = [];

        let attrs = this.geoObjectType.attributes.slice(); // intentionally a shallow copy
        attrs = attrs.filter(attr => geoObjectAttributeExcludes.indexOf(attr.code) === -1);
        attrs.push(this.geometryAttributeType);
        attrs.push(this.parentAttributeType);

        attrs.forEach(attr => {
            if (attr.code !== "_PARENT_") {
                if (attr.isChangeOverTime) {
                    let editor = new ChangeRequestChangeOverTimeAttributeEditor(this, attr, null);

                    editors.push(editor);
                } else {
                    let editor = new StandardAttributeCRModel(attr, this.geoObject, this.changeRequest);

                    editors.push(editor);
                }
            } else {
                this.hierarchies.forEach(hierarchy => {
                    let editor = new ChangeRequestChangeOverTimeAttributeEditor(this, attr, hierarchy);

                    editors.push(editor);
                });
            }
        });

        return editors;
    }

    public hasChanges(): boolean {
        let hasChanges = false;

        this.attributeEditors.forEach(attributeEditor => {
            if (attributeEditor.hasChanges()) {
                hasChanges = true;
            }
        });

        return hasChanges;
    }

    public isValid(): boolean {
        return this._isValid;
    }

    public validate(skipExists: boolean = false): boolean {
        this._isValid = true;

        this.attributeEditors.forEach(attributeEditor => {
            if (!skipExists || attributeEditor.attribute.code !== "exists") {
                attributeEditor.validate(true);

                if (!attributeEditor.isValid()) {
                    this._isValid = false;
                }
            }
        });

        return this._isValid;
    }

    findExistingValueOverTimeByOid(oid: string, attributeCode: string) {
        if (this.geoObject.attributes[attributeCode]) {
            let index = this.geoObject.attributes[attributeCode].values.findIndex((vot: ValueOverTime) => vot.oid === oid);

            if (index !== -1) {
                return this.geoObject.attributes[attributeCode].values[index];
            }
        }

        return null;
    }

    findEditorForValueOverTime(oid: string): ChangeRequestChangeOverTimeAttributeEditor {
        for (let i = 0; i < this.attributeEditors.length; ++i) {
            let editor = this.attributeEditors[i];

            if (editor instanceof ChangeRequestChangeOverTimeAttributeEditor && editor.getEditor(oid) != null) {
                return editor;
            }
        }
    }

    public getEditorForAttributeByCode(attributeCode: string, hierarchyCode: string = null) {
        let indexOf = this.attributeEditors.findIndex(editor => (!editor.attribute.isChangeOverTime || (hierarchyCode == null && (editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy == null) || ((editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy != null && (editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy.code === hierarchyCode)) && editor.attribute.code === attributeCode);

        if (indexOf === -1) {
            return null;
        } else {
            return this.attributeEditors[indexOf];
        }
    }

    public getEditorForAttribute(attribute: AttributeType, hierarchy: HierarchyOverTime = null): ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel {
        return this.getEditorForAttributeByCode(attribute.code, hierarchy == null ? null : hierarchy.code);
    }

    public getEditors(): (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[] {
        return this.attributeEditors;
    }

    public onChange(type: ChangeType) {
        this.onChangeSubject.next(type);
    }

    public existsAtDate(date: string) {
        let existsAttribute: AttributeType = GeoObjectType.getAttribute(this.geoObjectType, "exists");
        let existEditors = (this.getEditorForAttribute(existsAttribute) as ChangeRequestChangeOverTimeAttributeEditor).getEditors();

        let valLen = existEditors.length;
        for (let j = 0; j < valLen; ++j) {
            let editor: ValueOverTimeCREditor = existEditors[j];

            if (editor.startDate != null && editor.endDate != null && !editor.isDelete() && editor.value === true && this.dateService.between(date, editor.startDate, editor.endDate)) {
                return true;
            }
        }

        return false;
    }

}
