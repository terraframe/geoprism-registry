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

import { ValueOverTime, AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { debounce } from 'lodash';
import { v4 as uuid } from "uuid";
// eslint-disable-next-line camelcase
import turf_booleanequal from "@turf/boolean-equal";
import bbox from "@turf/bbox";
import { AlternateId, LocalizedValue } from "@core/model/core";
import { GeometryService, RegistryService } from "@registry/service";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { Subject } from "rxjs";
import { ChangeType } from "@registry/model/constants";
import { ListTypeService } from "@registry/service/list-type.service";
import { LngLatBoundsLike } from "maplibre-gl";
import { GeoJsonLayer, Layer } from "@registry/service/layer-data-source";
import { ConflictMessage } from "@shared/model/message";
import { HttpErrorResponse } from "@angular/common/http";

export class ValueOverTimeCREditor implements TimeRangeEntry {

    diff?: ValueOverTimeDiff; // Any existing diff which may be associated with this object.
    valueOverTime?: ValueOverTime; // Represents a vot on an existing GeoObject. If this is set and the action is UpdateAttribute, we must be doing an UPDATE, and valueOverTime represents the original value in the DB.
    action: AbstractAction;
    changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor;
    attr: AttributeType;
    conflictMessages: Set<ConflictMessage>;

    onChangeSubject: Subject<any> = new Subject<any>();

    _isValid: boolean = true;

    // Flag denoting if the value has changed
    // Used to prevent multiple validation calls
    // to the server if the value is the same
    isValueChanged: boolean = false;

    constructor(changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor, attr: AttributeType, action: AbstractAction) {
        this.attr = attr;
        this.changeRequestAttributeEditor = changeRequestAttributeEditor;
        this.action = action;
        this.validateDisplayLabel = debounce(this.validateDisplayLabel, 1000)
    }

    public removeType(type): void {
        // Balk method needed for HTML template compiling 
    }

    onChange(type: ChangeType) {
        this.changeRequestAttributeEditor.onChange(type);
        this.onChangeSubject.next(type);

        if (type === ChangeType.VALUE) {
            this.isValueChanged = true;
        }
    }

    getGeoObjectTimeRangeStorage(): TimeRangeEntry {
        return this.valueOverTime;
    }

    getValueFromGeoObjectForDiff(): any {
        return this.valueOverTime.value;
    }

    validate(): boolean {
        if (!this.conflictMessages) {
            this.conflictMessages = new Set();
        }

        let dateService = this.changeRequestAttributeEditor.changeRequestEditor.dateService;
        let start = dateService.validateDate(this.startDate == null ? null : dateService.getDateFromDateString(this.startDate), true, true);
        let end = dateService.validateDate(this.endDate == null ? null : dateService.getDateFromDateString(this.endDate), true, true);
        this._isValid = true;

        if (!start.valid || !end.valid) {
            this._isValid = false;
        }

        this.validateUpdateReference();

        if (this._isValid && this.isValueChanged && this.attr.code === 'displayLabel') {
            this.isValueChanged = false;

            if (this.valueOverTime != null && this.valueOverTime.value != null) {
                this.validateDisplayLabel();
            }

        }

        return this._isValid;
    }

    validateDisplayLabel() : void {
        const service = this.changeRequestAttributeEditor.changeRequestEditor.registryService;
        const type = this.changeRequestAttributeEditor.changeRequestEditor.geoObjectType;
        const code = this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes.code || '';
        const value = this.diff != null ? this.diff.newValue : this.valueOverTime.value;

        service.hasDuplicateLabel(this.startDate, type.code, code, value).then(resp => {
            let message = this.changeRequestAttributeEditor.changeRequestEditor.dateService.duplicateLabelMessage;

            if (resp.labelInUse) {
                this.conflictMessages.add(message);
            }
            else {
                this.conflictMessages.delete(message);
            }
        }).catch((err: HttpErrorResponse) => {
            // eslint-disable-next-line no-console
            console.log(err);
        });
    }

    /**
     * If we're referencing an existing value over time, that object should exist on our GeoObject (which represents the current state of the database)
     */
    validateUpdateReference() {
        let missingReference = this.changeRequestAttributeEditor.changeRequestEditor.dateService.missingReference;

        this.conflictMessages.delete(missingReference);

        if (this.changeRequestAttributeEditor.changeRequestEditor.changeRequest.type === "UpdateGeoObject" && this.diff != null && this.diff.action !== "CREATE") {
            let existingVot = this.findExistingValueOverTimeByOid(this.diff.oid);

            if (existingVot == null) {
                this._isValid = false;

                this.conflictMessages.add(missingReference);
            }
        }
    }

    findExistingValueOverTimeByOid(oid: string) {
        if (this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[this.attr.code]) {
            let index = this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[this.attr.code].values.findIndex((vot: ValueOverTime) => vot.oid === oid);

            if (index !== -1) {
                return this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[this.attr.code].values[index];
            }
        }

        return null;
    }

    set oid(oid: string) {
        if (this.diff != null) {
            this.diff.oid = oid;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            this.getGeoObjectTimeRangeStorage().oid = oid;
        }
    }

    get oid(): string {
        if (this.diff != null) {
            return this.diff.oid;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().oid;
        }

        return null;
    }

    get startDate(): string {
        return this.getStartDate();
    }

    public getStartDate(): string {
        if (this.diff != null && this.diff.newStartDate !== undefined) {
            return this.diff.newStartDate;
        } else if (this.diff != null && this.diff.oldStartDate !== undefined) {
            return this.diff.oldStartDate;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().startDate;
        }

        return null;
    }

    constructNewDiff(action: "DELETE" | "UPDATE" | "CREATE"): void {
        this.diff = new ValueOverTimeDiff();
        this.diff.action = action;
        (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);

        if (action === "CREATE") {
            this.diff.oid = uuid();
        } else {
            let goRange = this.getGeoObjectTimeRangeStorage();

            this.diff.oid = goRange.oid;
            this.diff.oldStartDate = goRange.startDate;
            this.diff.oldEndDate = goRange.endDate;
        }
    }

    set oldStartDate(oldStartDate: string) {
        if (this.diff != null) {
            this.diff.oldStartDate = oldStartDate;
        }
    }

    get oldStartDate(): string {
        if (this.diff != null) {
            return this.diff.oldStartDate;
        }

        return null;
    }

    get endDate(): string {
        return this.getEndDate();
    }

    public getEndDate(): string {
        if (this.diff != null && this.diff.newEndDate !== undefined) {
            return this.diff.newEndDate;
        } else if (this.diff != null && this.diff.oldStartDate !== undefined) {
            return this.diff.oldEndDate;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().endDate;
        }

        return null;
    }

    set startDate(startDate: string) {
        this.setStartDate(startDate);
    }

    public setStartDate(startDate: string) {
        if (this.isDelete()) {
            return; // There are various view components (like the date widgets) which will invoke this method
        }

        let goRange = this.getGeoObjectTimeRangeStorage();

        if (this.action.actionType === "UpdateAttributeAction") {
            if (this.diff == null) {
                if (this.getGeoObjectTimeRangeStorage() == null) {
                    this.constructNewDiff("CREATE");
                } else {
                    if (goRange.startDate === startDate) {
                        return;
                    }

                    this.constructNewDiff("UPDATE");

                    this.diff.oldValue = this.getValueFromGeoObjectForDiff();
                }
            }

            if (startDate === this.diff.oldStartDate) {
                delete this.diff.newStartDate;
            } else {
                this.diff.newStartDate = startDate;
            }

            // If no changes have been made then remove the diff
            this.removeEmptyDiff();
        } else if (this.action.actionType === "CreateGeoObjectAction") {
            goRange.startDate = startDate;
        }

        this.onChange(ChangeType.START_DATE);
    }

    set endDate(endDate: string) {
        if (this.isDelete()) {
            return; // There are various view components (like the date widgets) which will invoke this method
        }

        let goRange = this.getGeoObjectTimeRangeStorage();

        if (this.action.actionType === "UpdateAttributeAction") {
            if (this.diff == null) {
                if (goRange == null) {
                    this.constructNewDiff("CREATE");
                } else {
                    if (goRange.endDate === endDate) {
                        return;
                    }

                    this.constructNewDiff("UPDATE");

                    this.diff.oldValue = this.getValueFromGeoObjectForDiff();
                }
            }

            if (endDate === this.diff.oldEndDate) {
                delete this.diff.newEndDate;
            } else {
                this.diff.newEndDate = endDate;
            }

            // If no changes have been made then remove the diff
            this.removeEmptyDiff();
        } else if (this.action.actionType === "CreateGeoObjectAction") {
            goRange.endDate = endDate;
        }

        this.onChange(ChangeType.END_DATE);
    }

    set oldEndDate(oldEndDate: string) {
        if (this.diff != null) {
            this.diff.oldEndDate = oldEndDate;
        }
    }

    get oldEndDate(): string {
        if (this.diff != null) {
            return this.diff.oldEndDate;
        }

        return null;
    }

    get value(): any {
        if (this.diff != null && this.diff.newValue !== undefined) {
            return this.diff.newValue;
        } else if (this.diff != null && this.diff.oldValue !== undefined) {
            return this.diff.oldValue;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().value;
        }

        return null;
    }

    set value(value: any) {
        if (this.isDelete()) {
            return; // There are various view components (like the date widgets) which will invoke this method
        }

        if (value != null) {
            if (this.attr.type === "term") {
                value = [value];
            } else if (this.attr.type === "date") {
                value = new Date(value).getTime();
            } else if (this.attr.type === "geometry") {
                // Limit max precision for point geometries
                let maxCoordinatePrecision = 6;

                if (value.type === "MultiPoint") {
                    for (let i = 0; i < value.coordinates.length; ++i) {
                        let coordinate: number[] = value.coordinates[i];

                        coordinate[0] = Number.parseFloat(coordinate[0].toFixed(maxCoordinatePrecision));
                        coordinate[1] = Number.parseFloat(coordinate[1].toFixed(maxCoordinatePrecision));
                    }
                } else if (value.type === "Point") {
                    value.coordinates = [Number.parseFloat(value.coordinates[0].toFixed(maxCoordinatePrecision)), Number.parseFloat(value.coordinates[1].toFixed(maxCoordinatePrecision))];
                }
            }
        } else if (value == null) {
            if (this.attr.type === "geometry") {
                value = GeometryService.createEmptyGeometryValue(this.changeRequestAttributeEditor.changeRequestEditor.geoObjectType.geometryType);
            } else if (this.attr.type === "character") {
                value = "";
            }
        }

        if (this.action.actionType === "UpdateAttributeAction") {
            if (this.diff == null) {
                if (this.getGeoObjectTimeRangeStorage() == null) {
                    this.diff = new ValueOverTimeDiff();
                    this.diff.oid = uuid();
                    this.diff.action = "CREATE";
                    (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
                } else {
                    if (this.areValuesEqual(this.valueOverTime.value, value)) {
                        return;
                    }

                    this.diff = new ValueOverTimeDiff();
                    this.diff.action = "UPDATE";
                    this.diff.oid = this.getGeoObjectTimeRangeStorage().oid;
                    this.diff.oldValue = this.valueOverTime.value;
                    this.diff.oldStartDate = this.getGeoObjectTimeRangeStorage().startDate;
                    this.diff.oldEndDate = this.getGeoObjectTimeRangeStorage().endDate;
                    (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
                }
            }

            if (this.diff.action !== "CREATE" && this.areValuesEqual(this.diff.oldValue, value)) {
                delete this.diff.newValue;
            } else {
                this.diff.newValue = JSON.parse(JSON.stringify(value));
            }

            // If no changes have been made then remove the diff
            this.removeEmptyDiff();
        } else if (this.action.actionType === "CreateGeoObjectAction") {
            this.valueOverTime.value = value;
        }

        this.onChange(ChangeType.VALUE);
    }

    set oldValue(oldValue: any) {
        if (this.diff != null) {
            this.diff.oldValue = oldValue;
        }
    }

    get oldValue(): any {
        if (this.diff != null) {
            return this.diff.oldValue;
        }

        return null;
    }

    public setLocalizedValue(localizedValue: LocalizedValue) {
        this.value = JSON.parse(JSON.stringify(localizedValue));
    }

    public setAlternateIds(ids: AlternateId[]) {
        this.value = JSON.parse(JSON.stringify(ids));
    }

    removeEmptyDiff(): void {
        if (this.diff != null && this.diff.newValue === undefined && this.diff.newStartDate === undefined && this.diff.newEndDate === undefined) {
            const diffs = (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime;

            const index = diffs.findIndex(d => d.oid === this.diff.oid);

            if (index !== -1) {
                diffs.splice(index, 1);
            }

            this.diff = null;
        }
    }

    areValuesEqual(val1: any, val2: any): boolean {
        if (this.attr.type === "boolean") {
            return val1 === val2;
        }

        if ((val1 === "" && val2 == null) || (val2 === "" && val1 == null)) {
            return true;
        }

        if (!val1 && !val2) {
            return true;
        } else if ((!val1 && val2) || (!val2 && val1)) {
            return false;
        }

        if (this.attr.type === "term") {
            if (val1 != null && val2 != null) {
                return val1.length === val2.length && val1[0] === val2[0];
            }
        } else if (this.attr.type === "geometry") {
            if (((val1 != null && val1.coordinates != null && val1.coordinates.length != null) && (val2 != null && val2.coordinates != null && val2.coordinates.length != null)) && val1.coordinates.length !== val2.coordinates.length) {
                return false;
            }
            return turf_booleanequal(val1, val2);
        } else if (this.attr.type === "date") {
            let casted1 = (typeof val1 === "string") ? parseInt(val1) : val1;
            let casted2 = (typeof val2 === "string") ? parseInt(val2) : val2;

            return casted1 === casted2;
        } else if (this.attr.type === "local") {
            if ((!val1.localeValues || !val2.localeValues) || val1.localeValues.length !== val2.localeValues.length) {
                return false;
            }

            let len = val1.localeValues.length;
            for (let i = 0; i < len; ++i) {
                let localeValue = val1.localeValues[i];

                let lv2 = this.getValueAtLocale(val2, localeValue.locale);
                let lv1 = localeValue.value;

                if ((lv1 === "" && lv2 == null) || (lv2 === "" && lv1 == null)) {
                    continue;
                } else if (lv1 !== lv2) {
                    return false;
                }
            }

            return true;
        }

        return val1 === val2;
    }

    getValueAtLocale(lv: LocalizedValue, locale: string) {
        return new LocalizedValue(lv.localizedValue, lv.localeValues).getValue(locale);
    }

    public remove(): void {
        if (this.action.actionType === "UpdateAttributeAction") {
            if (this.diff != null && this.diff.action === "CREATE") {
                // Its a new entry, just remove the diff from the diff array
                let updateAction: UpdateAttributeOverTimeAction = this.action as UpdateAttributeOverTimeAction;

                const index = updateAction.attributeDiff.valuesOverTime.findIndex(vot => vot.oid === this.diff.oid);

                if (index > -1) {
                    updateAction.attributeDiff.valuesOverTime.splice(index, 1);
                }
            } else if (this.diff != null) {
                delete this.diff.newValue;
                delete this.diff.newStartDate;
                delete this.diff.newEndDate;
                this.removeEmptyDiff();
                this.onChange(ChangeType.REMOVE);
                return;
            } else if (this.valueOverTime != null && this.diff == null) {
                this.diff = new ValueOverTimeDiff();
                this.diff.action = "DELETE";
                this.diff.oid = this.valueOverTime.oid;
                this.diff.oldValue = this.valueOverTime.value;
                this.diff.oldStartDate = this.valueOverTime.startDate;
                this.diff.oldEndDate = this.valueOverTime.endDate;
                (this.action as UpdateAttributeOverTimeAction).attributeDiff.valuesOverTime.push(this.diff);
            }
        } else if (this.action.actionType === "CreateGeoObjectAction") {
            let votc = (this.action as CreateGeoObjectAction).geoObjectJson.attributes[this.attr.code].values;

            let index = votc.findIndex((vot) => { return vot.oid === this.valueOverTime.oid; });

            if (index !== -1) {
                votc.splice(index, 1);
            }
        }

        this.onChange(ChangeType.REMOVE);
    }

    public isDelete() {
        return this.diff != null && this.diff.action === "DELETE";
    }

    buildDataSource(type: string) {
        let votEditor = this;
        return {
            setLayerData(data: any): void {
                if (type === "NEW") {
                    votEditor.value = data;
                } else {
                    // eslint-disable-next-line no-console
                    console.log("ERROR. Cannot edit old geometry");
                }
            },
            getLayerData() {
                if (type === "NEW") {
                    return votEditor.value;
                } else {
                    return votEditor.oldValue;
                }
            },
            buildMapboxSource() {
                let geojson = this.getLayerData();

                return {
                    type: "geojson",
                    data: geojson
                };
            },
            getGeometryType(): string {
                return votEditor.changeRequestAttributeEditor.changeRequestEditor.geoObjectType.geometryType;
            },
            getDataSourceId(): string {
                return type + "_" + votEditor.oid;
            },
            getDataSourceProviderId(): string {
                return votEditor.changeRequestAttributeEditor.changeRequestEditor.changeRequest.oid;
            },
            createLayer(oid: string, legendLabel: string, rendered: boolean, color: string): Layer {
                return new GeoJsonLayer(this, legendLabel, rendered, color);
            },
            getBounds(layer: Layer, registryService: RegistryService, listService: ListTypeService): Promise<LngLatBoundsLike> {
                return new Promise((resolve, reject) => {
                    resolve(bbox(this.getLayerData()) as LngLatBoundsLike);
                });
            }
        };
    }

}
