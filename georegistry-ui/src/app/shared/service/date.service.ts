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

import { Injectable } from "@angular/core";
import { LocalizationService } from "@shared/service/localization.service";
import { PRESENT } from "../model/date";
import { ConflictMessage, ConflictType } from "../model/message";

@Injectable()
export class DateService {

    public overlapMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.overlap.message"),
        type: ConflictType.TIME_RANGE
    }

    public mergeContiguousMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.mergeContiguousRanges.message"),
        type: ConflictType.TIME_RANGE
    }

    public gapMessage: ConflictMessage = {
        severity: "WARNING",
        message: this.localizationService.decode("manage.versions.gap.message"),
        type: ConflictType.TIME_RANGE
    }

    public outsideExistsMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.outsideExists.message"),
        type: ConflictType.OUTSIDE_EXISTS
    }

    public missingReference: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("changeovertime.manageVersions.missingReference"),
        type: ConflictType.MISSING_REFERENCE
    }

    public startDateLaterEndDate: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.startdate.later.enddate.message"),
        type: ConflictType.TIME_RANGE
    };

    public parentDoesNotExist: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("changeovertime.manageVersions.parentDoesNotExist"),
        type: ConflictType.MISSING_REFERENCE
    }

    public invalidParent: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("changeovertime.manageVersions.invalidParent"),
        type: ConflictType.MISSING_REFERENCE
    }

    // eslint-disable-next-line no-useless-constructor
    constructor(private localizationService: LocalizationService) { }

    // Get infinity date (called 'present' in the UI)
    public getPresentDate(): Date {
        // NOTE: Month is 0 indexed so 11 = December
        let dt = new Date(5000, 11, 31, 0, 0, 0);

        return dt;
    }

    public formatDateForDisplay(date: string | Date | number): string {
        if (!date) {
            return "";
        }

        if (date === PRESENT) {
            return this.localizationService.localize("changeovertime", "present");
        }

        if (date.valueOf() === 'number') {
            date = new Date(date);
        }

        if (date instanceof Date) {
            return this.getDateString(date);
        } else {
            return (date as string).split("T")[0];
        }
    }

    // @param value as yyyy-mm-dd
    getDateFromDateString(value: string) {
        return new Date(+value.split("-")[0], +value.split("-")[1] - 1, +value.split("-")[2]);
    }

    getDateString(date: Date): string {
        if (date instanceof Date) {
            let year = date.getFullYear();
            let month: number | string = date.getMonth() + 1;
            let dt: number | string = date.getDate();

            if (dt < 10) {
                dt = "0" + dt;
            }
            if (month < 10) {
                month = "0" + month;
            }

            return year + "-" + month + "-" + dt;
        }

        return null;
    }

    public between(test: string | Date, startDate: string, endDate: string) {
        if (startDate == null) {
            return false;
        }

        let dTest: Date = test == null ? this.getPresentDate() : (test instanceof Date ? test : this.getDateFromDateString(test));
        let dStart: Date = this.getDateFromDateString(startDate);
        let dEnd: Date = endDate == null ? this.getPresentDate() : this.getDateFromDateString(endDate);

        return dTest >= dStart && dTest <= dEnd;
    }

    public after(in1: string | Date, in2: string | Date) {
        if (in1 == null || in2 == null) {
            return false;
        }

        let date1: Date = in1 instanceof Date ? in1 : this.getDateFromDateString(in1);
        let date2: Date = in2 instanceof Date ? in2 : this.getDateFromDateString(in2);

        return date1 > date2;
    }

    public addDay(amount: number, date: string): string {
        var plus1: Date = this.getDateFromDateString(date);
        plus1.setDate(plus1.getDate() + amount);
        let splus1: string = this.getDateString(plus1);
        return splus1;
    }


    validateDate(date: Date, required: boolean, allowFutureDates: boolean): { message: string, valid: boolean } {
        let valid = { message: "", valid: true };
        let today: Date = new Date();

        if (date != null) {
            if (!(date instanceof Date) || (date instanceof Date && isNaN(date.getTime()))) {
                valid.valid = false;
                valid.message = this.localizationService.decode("date.inpu.data.invalid.error.message");
            } else if (!allowFutureDates && date > today) {
                valid.valid = false;
                valid.message = this.localizationService.decode("date.inpu.data.in.future.error.message");
            }
        } else if (required) {
            valid.valid = false;
            valid.message = this.localizationService.decode("manage.versions.date.required.message");
        }

        return valid;
    }

}
