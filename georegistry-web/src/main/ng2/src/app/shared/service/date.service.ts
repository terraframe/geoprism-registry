import { Injectable } from "@angular/core";
import { LocalizationService } from "./localization.service";
import Utils from "../../registry/utility/Utils";
import { PRESENT, ConflictMessage, TimeRangeEntry, AttributeType } from "@registry/model/registry";
import { SummaryKey } from "@registry/model/crtable";
import { ConflictType } from "@registry/model/constants";
import { ValueOverTimeCREditor } from "@registry/component/geoobject-shared-attribute-editor/ValueOverTimeCREditor";

@Injectable()
export class DateService {

    overlapMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.overlap.message"),
        type: ConflictType.TIME_RANGE
    }

    mergeContiguousMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.mergeContiguousRanges.message"),
        type: ConflictType.TIME_RANGE
    }

    gapMessage: ConflictMessage = {
        severity: "WARNING",
        message: this.localizationService.decode("manage.versions.gap.message"),
        type: ConflictType.TIME_RANGE
    }

    outsideExistsMessage: ConflictMessage = {
        severity: "ERROR",
        message: this.localizationService.decode("manage.versions.outsideExists.message"),
        type: ConflictType.OUTSIDE_EXISTS
    }

    // eslint-disable-next-line no-useless-constructor
    constructor(private localizationService: LocalizationService) { }

    // Get infinity date (called 'present' in the UI)
    public getPresentDate(): Date {
        // NOTE: Month is 0 indexed so 11 = December
        let dt = new Date(5000, 11, 31, 0, 0, 0);

        return dt;
    }

    public formatDateForDisplay(date: string | Date): string {
        if (!date) {
            return "";
        }

        if (date === PRESENT) {
            return this.localizationService.localize("changeovertime", "present");
        }

        if (date instanceof Date) {
            return this.getDateString(date);
        } else {
            return date.split("T")[0];
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

    checkRanges(attributeType: AttributeType, ranges: ValueOverTimeCREditor[]): boolean {
        let hasConflict = false;

        // clear all messages
        ranges.forEach(attr => {
            if (!attr.conflictMessage) {
                attr.conflictMessage = [];
            }

            for (let i = attr.conflictMessage.length - 1; i >= 0; --i) {
                if (attr.conflictMessage[i].type === ConflictType.TIME_RANGE) {
                    attr.conflictMessage.splice(i, 1);
                }
            }
        });

        // Filter DELETE entries from consideration
        const filtered: ValueOverTimeCREditor[] = ranges.filter(range => !range.isDelete());

        // Check for overlaps
        for (let j = 0; j < filtered.length; j++) {
            const h1: ValueOverTimeCREditor = filtered[j];

            if (h1.startDate && h1.endDate) {
                let s1: any = this.getDateFromDateString(h1.startDate);
                let e1: any = this.getDateFromDateString(h1.endDate);

                if (Utils.dateEndBeforeStart(s1, e1)) {
                    h1.conflictMessage.push({
                        severity: "ERROR",
                        message: this.localizationService.decode("manage.versions.startdate.later.enddate.message"),
                        type: ConflictType.TIME_RANGE
                    } as ConflictMessage);

                    hasConflict = true;
                }

                for (let i = 0; i < filtered.length; i++) {
                    if (j !== i) {
                        const h2: ValueOverTimeCREditor = filtered[i];

                        // If all dates set
                        if (h2.startDate && h2.endDate) {
                            let s2: Date = this.getDateFromDateString(h2.startDate);
                            let e2: Date = this.getDateFromDateString(h2.endDate);

                            // Determine if there is an overlap
                            if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                                h1.conflictMessage.push(this.overlapMessage);

                                if (s2.getTime() === e2.getTime()) {
                                    h2.conflictMessage.push(this.overlapMessage);
                                }

                                hasConflict = true;
                            } else if (this.addDay(1, h1.endDate) === h2.startDate && Utils.areValuesEqual(attributeType, h1.value, h2.value)) {
                                h1.conflictMessage.push(this.mergeContiguousMessage);
                                h2.conflictMessage.push(this.mergeContiguousMessage);
                                hasConflict = true;
                            }
                        } else if ((i === j - 1 || i === j + 1) && e1 && h2.startDate) {
                            // If 1st end date and current start date

                            let s2: Date = this.getDateFromDateString(h2.startDate);

                            if (s2.getTime() <= e1) {
                                h1.conflictMessage.push(this.overlapMessage);
                                h2.conflictMessage.push(this.overlapMessage);

                                hasConflict = true;
                            }
                        }
                    }
                }
            }
        }

        this.sort(filtered);

        // Check for gaps
        let current = null;
        for (let j = 0; j < filtered.length; j++) {
            let next = filtered[j];

            if (j > 0) {
                if (current.endDate && next.startDate) {
                    let e1: any = new Date(current.endDate);
                    let s2: any = new Date(next.startDate);

                    if (Utils.hasGap(e1.getTime(), s2.getTime())) {
                        next.conflictMessage.push(this.gapMessage);

                        current.conflictMessage.push(this.gapMessage);
                    }
                }
            }

            current = next;
        }

        this.sort(ranges);

        return hasConflict;
    }

    public between(test: string | Date, startDate: string, endDate: string) {
        if (test == null || startDate == null || endDate == null) {
            return false;
        }

        let dTest: Date = test instanceof Date ? test : this.getDateFromDateString(test);
        let dStart: Date = this.getDateFromDateString(startDate);
        let dEnd: Date = this.getDateFromDateString(endDate);

        return dTest >= dStart && dTest <= dEnd;
    }

    public addDay(amount: number, date: string): string {
        var plus1: Date = this.getDateFromDateString(date);
        plus1.setDate(plus1.getDate() + amount);
        let splus1: string = this.getDateString(plus1);
        return splus1;
    }

    public sort(votArr: TimeRangeEntry[]): void {
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

    checkExistRanges(ranges: ValueOverTimeCREditor[], existEntries: ValueOverTimeCREditor[]): boolean {
        let hasConflict = false;

        // clear all messages
        ranges.forEach(range => {
            if (!range.conflictMessage) {
                range.conflictMessage = [];
            }

            for (let i = range.conflictMessage.length - 1; i >= 0; --i) {
                if (range.conflictMessage[i].type === ConflictType.OUTSIDE_EXISTS) {
                    range.conflictMessage.splice(i, 1);
                }
            }
        });

        // Filter DELETE entries from consideration
        const filtered: ValueOverTimeCREditor[] = ranges.filter(range => !range.isDelete());

        const filteredExists = existEntries.filter(range => !range.isDelete());

        // Check for outside exists range
        for (let j = 0; j < filtered.length; j++) {
            const h1 = filtered[j];

            if (h1.startDate && h1.endDate) {
                let s1: any = this.getDateFromDateString(h1.startDate);
                let e1: any = this.getDateFromDateString(h1.endDate);

                let inRange = false;

                for (let i = 0; i < filteredExists.length; i++) {
                    const h2 = filteredExists[i];

                    // If all dates set
                    if (h2.value && h2.startDate && h2.endDate) {
                        let s2: Date = this.getDateFromDateString(h2.startDate);
                        let e2: Date = this.getDateFromDateString(h2.endDate);

                        if (!Utils.dateRangeOutside(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                            inRange = true;
                        }
                    }
                }

                if (!inRange) {
                    h1.conflictMessage.push(this.outsideExistsMessage);
                    hasConflict = true;
                }
            }
        }

        return hasConflict;
    }

    validateDate(date: Date, required: boolean, allowFutureDates: boolean): {message: string, valid: boolean} {
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
