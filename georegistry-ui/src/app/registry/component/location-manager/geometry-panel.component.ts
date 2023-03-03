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

import {
    Component,
    OnInit,
    Input,
    Output,
    ChangeDetectorRef,
    EventEmitter,
    ViewChildren,
    QueryList
} from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { GeoObjectType, AttributeType, ValueOverTime, GeoObjectOverTime } from "@registry/model/registry";
import { DateService } from "@shared/service/date.service";
import * as moment from "moment";
import { VotService } from "@registry/service/vot.service";
import { PRESENT } from "@shared/model/date";
import { DateFieldComponent } from "@shared/component";

@Component({
    selector: "geometry-panel",
    templateUrl: "./geometry-panel.component.html",
    styleUrls: ["./geometry-panel.css"],
    host: { "[@fadeInOut]": "true" },
    animations: [
        [
            trigger("fadeInOut", [
                transition("void => *", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ]),
                transition(":leave",
                    animate("500ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]]
})
export class GeometryPanelComponent implements OnInit {

    @ViewChildren("dateFieldComponents") dateFieldComponentsArray: QueryList<DateFieldComponent>;

    currentDate: Date = new Date();

    isValid: boolean = true;

    isVersionForHighlight: number;

    message: string = null;

    readonly: boolean = false;

    hasConflict: boolean = false;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful
     */
    @Output() onChange = new EventEmitter<GeoObjectOverTime>();

    @Output() onCloneGeometry = new EventEmitter<any>();

    @Output() onEdit = new EventEmitter<ValueOverTime>();

    @Input() geoObjectType: GeoObjectType;

    originalGeoObjectOverTime: GeoObjectOverTime;
    geoObjectOverTime: GeoObjectOverTime;

    // eslint-disable-next-line accessor-pairs
    @Input() set geoObjectOverTimeInput(value: GeoObjectOverTime) {
        this.originalGeoObjectOverTime = JSON.parse(JSON.stringify(value));
        this.geoObjectOverTime = value;
    }

    @Input() isNewGeoObject: boolean = false;

    goGeometries: GeoObjectOverTime;

    newVersion: ValueOverTime;

    hasDuplicateDate: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(public changeDetectorRef: ChangeDetectorRef, private dateService: DateService, private votService: VotService) { }

    ngOnInit(): void {
    }

    checkDateFieldValidity(): boolean {
        let dateFields = this.dateFieldComponentsArray.toArray();

        for (let i = 0; i < dateFields.length; i++) {
            let field = dateFields[i];
            if (!field.valid) {
                return false;
            }
        }

        return true;
    }

    onDateChange(): any {
        this.hasConflict = false;

        this.isValid = this.checkDateFieldValidity();

        let vAttributes = this.geoObjectOverTime.attributes["geometry"].values;

        this.hasConflict = this.votService.checkRanges(null, vAttributes);
    }

    edit(vot: ValueOverTime, isVersionForHighlight: number): void {
        this.onEdit.emit(vot);

        this.isVersionForHighlight = isVersionForHighlight;
    }

    onAddNewVersion(geometry: ValueOverTime): void {
        let votArr: ValueOverTime[] = this.geoObjectOverTime.attributes["geometry"].values;

        let vot: ValueOverTime = new ValueOverTime();
        vot.startDate = null; // Utils.formatDateString(new Date());
        vot.endDate = null; // Utils.formatDateString(new Date());

        if (geometry && geometry.value) {
            vot.value = geometry.value;
        } else {
            vot.value = { type: this.geoObjectType.geometryType, coordinates: [] };
        }

        if (this.geoObjectType.geometryType === "MULTIPOLYGON") {
            vot.value.type = "MultiPolygon";
        } else if (this.geoObjectType.geometryType === "POLYGON") {
            vot.value.type = "Polygon";
        } else if (this.geoObjectType.geometryType === "POINT") {
            vot.value.type = "Point";
        } else if (this.geoObjectType.geometryType === "MULTIPOINT") {
            vot.value.type = "MultiPoint";
        } else if (this.geoObjectType.geometryType === "LINE") {
            vot.value.type = "Line";
        } else if (this.geoObjectType.geometryType === "MULTILINE") {
            vot.value.type = "MultiLine";
        } else if (this.geoObjectType.geometryType === "MIXED") {
            vot.value.type = "Mixed";
        }

        votArr.push(vot);

        this.changeDetectorRef.detectChanges();
    }

    getVersionData(attribute: AttributeType) {
        let versions: ValueOverTime[] = [];

        this.geoObjectOverTime.attributes[attribute.code].values.forEach(vAttribute => {
            vAttribute.value.localeValues.forEach(val => {
                versions.push(val);
            });
        });

        return versions;
    }

    getDefaultLocaleVal(locale: any): string {
        let defVal = null;

        locale.localeValues.forEach(locVal => {
            if (locVal.locale === "defaultLocale") {
                defVal = locVal.value;
            }
        });

        return defVal;
    }

    setDateAttribute(vot: ValueOverTime, val: string): void {
        vot.value = new Date(val).getTime().toString();
    }

    remove(version: any): void {
        let val = this.geoObjectOverTime.attributes["geometry"];

        let position = -1;
        for (let i = 0; i < val.values.length; i++) {
            let vals = val.values[i];

            if (vals.startDate === version.startDate) {
                position = i;
            }
        }

        if (position > -1) {
            val.values.splice(position, 1);
        }
    }

    formatDate(date: string) {
        let localeData = moment.localeData(date);
        let format = localeData.longDateFormat("L");
        return moment().format(format);
    }

    setInfinity(vAttribute, attributes): void {
        if (vAttribute.endDate === PRESENT) {
            vAttribute.endDate = new Date();
        } else {
            vAttribute.endDate = PRESENT;
        }

        this.onDateChange();
    }

    sort(votArr: ValueOverTime[]): void {
        // Sort the data by start date
        votArr.sort(function(a, b) {
            if (a.startDate == null || a.startDate === "") {
                return 1;
            } else if (b.startDate == null || b.startDate === "") {
                return -1;
            }

            let first: any = new Date(a.startDate);
            let next: any = new Date(b.startDate);
            return first - next;
        });
    }

    onCloneGeometryToNewVersion(geometry: ValueOverTime): void {
        this.onAddNewVersion(geometry);
    }

    onSubmit(): void {
        this.onChange.emit(this.geoObjectOverTime);
    }

    onCancel(): void {
        this.onChange.emit(this.originalGeoObjectOverTime);
    }

}
