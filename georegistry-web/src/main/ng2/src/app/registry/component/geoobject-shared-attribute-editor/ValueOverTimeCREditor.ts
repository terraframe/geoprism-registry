import { ValueOverTime, AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { CreateGeoObjectAction, UpdateAttributeOverTimeAction, AbstractAction, ValueOverTimeDiff } from "@registry/model/crtable";
import { v4 as uuid } from "uuid";
// eslint-disable-next-line camelcase
import turf_booleanequal from "@turf/boolean-equal";
import { LocalizedValue } from "@shared/model/core";
import { GeometryService } from "@registry/service";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { Subject } from "rxjs";

export class ValueOverTimeCREditor implements TimeRangeEntry {

    diff?: ValueOverTimeDiff; // Any existing diff which may be associated with this object.
    valueOverTime?: ValueOverTime; // Represents a vot on an existing GeoObject. If this is set and the action is UpdateAttribute, we must be doing an UPDATE, and valueOverTime represents the original value in the DB.
    action: AbstractAction;
    changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor;
    attr: AttributeType;
    conflictMessage: any;

    onChangeSubject : Subject<any> = new Subject<any>();

    _isValid: boolean = true;

    constructor(changeRequestAttributeEditor: ChangeRequestChangeOverTimeAttributeEditor, attr: AttributeType, action: AbstractAction) {
        this.attr = attr;
        this.changeRequestAttributeEditor = changeRequestAttributeEditor;
        this.action = action;
    }

    getGeoObjectTimeRangeStorage(): TimeRangeEntry {
        return this.valueOverTime;
    }

    getValueFromGeoObjectForDiff(): any {
        return this.valueOverTime.value;
    }

    validate(): boolean {
        let dateService = this.changeRequestAttributeEditor.changeRequestEditor.dateService;
        let start = dateService.validateDate(this.startDate == null ? null : dateService.getDateFromDateString(this.startDate), true, true);
        let end = dateService.validateDate(this.endDate == null ? null : dateService.getDateFromDateString(this.endDate), true, true);
        this._isValid = true;

        if (!start.valid || !end.valid) {
            this._isValid = false;
        }

        this.validateUpdateReference();

        return this._isValid;
    }

    /**
     * If we're referencing an existing value over time, that object should exist on our GeoObject (which represents the current state of the database)
     */
    validateUpdateReference() {
        if (this.changeRequestAttributeEditor.changeRequestEditor.changeRequest.type === "UpdateGeoObject" && this.diff != null && this.diff.action !== "CREATE") {
            let existingVot = this.findExistingValueOverTimeByOid(this.diff.oid, this.attr.code);

            if (existingVot == null) {
                this._isValid = false;
            }
        }
    }

    findExistingValueOverTimeByOid(oid: string, attributeCode: string) {
        if (this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[attributeCode]) {
            let index = this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[attributeCode].values.findIndex((vot: ValueOverTime) => vot.oid === oid);

            if (index !== -1) {
                return this.changeRequestAttributeEditor.changeRequestEditor.geoObject.attributes[attributeCode].values[index];
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
        if (this.diff != null && this.diff.newStartDate !== undefined) {
            return this.diff.newStartDate;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().startDate;
        }

        return null;
    }

    constructNewDiff(action: string): void {
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
        if (this.diff != null && this.diff.newEndDate !== undefined) {
            return this.diff.newEndDate;
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().endDate;
        }

        return null;
    }

    set startDate(startDate: string) {
        if (this.diff != null && this.diff.action === "DELETE") {
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

        this.changeRequestAttributeEditor.onChange();
        this.onChangeSubject.next("startDate");
    }

    set endDate(endDate: string) {
        if (this.diff != null && this.diff.action === "DELETE") {
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

        this.changeRequestAttributeEditor.onChange();
        this.onChangeSubject.next("endDate");
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
        } else if (this.getGeoObjectTimeRangeStorage() != null) {
            return this.getGeoObjectTimeRangeStorage().value;
        }

        return null;
    }

    set value(value: any) {
        if (this.diff != null && this.diff.action === "DELETE") {
            return; // There are various view components (like the date widgets) which will invoke this method
        }

        if (value != null) {
            if (this.attr.type === "term") {
                value = [value];
            } else if (this.attr.type === "date") {
                value = new Date(value).getTime();
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

        this.changeRequestAttributeEditor.onChange();
        this.onChangeSubject.next("value");
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
                this.changeRequestAttributeEditor.onChange();
                this.onChangeSubject.next("remove");
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

        this.changeRequestAttributeEditor.onChange();
        this.onChangeSubject.next("remove");
    }

}
