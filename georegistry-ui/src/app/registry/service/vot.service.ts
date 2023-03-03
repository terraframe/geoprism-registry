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
import { ValueOverTimeCREditor } from "@registry/component/geoobject-shared-attribute-editor/ValueOverTimeCREditor";
import { AttributeType } from "@registry/model/registry";
import Utils from "@registry/utility/Utils";
import { TimeRangeEntry } from "@shared/model/message";
import { DateService } from "@shared/service";

@Injectable()
export class VotService {

    constructor(private service: DateService) { }

    checkRanges(attributeType: AttributeType, ranges: ValueOverTimeCREditor[]): boolean {
        let hasConflict = false;

        // clear all messages
        ranges.forEach(range => {
            if (!range.conflictMessages) {
                range.conflictMessages = new Set();
            }

            range.conflictMessages.delete(this.service.overlapMessage);
            range.conflictMessages.delete(this.service.mergeContiguousMessage);
            range.conflictMessages.delete(this.service.gapMessage);
            range.conflictMessages.delete(this.service.startDateLaterEndDate);
        });

        // Filter DELETE entries from consideration
        const filtered: ValueOverTimeCREditor[] = ranges.filter(range => !range.isDelete());

        // Check for overlaps
        for (let j = 0; j < filtered.length; j++) {
            const h1: ValueOverTimeCREditor = filtered[j];

            if (h1.startDate && h1.endDate) {
                let s1: any = this.service.getDateFromDateString(h1.startDate);
                let e1: any = this.service.getDateFromDateString(h1.endDate);

                if (Utils.dateEndBeforeStart(s1, e1)) {
                    h1.conflictMessages.add(this.service.startDateLaterEndDate);

                    hasConflict = true;
                }

                for (let i = 0; i < filtered.length; i++) {
                    if (j !== i) {
                        const h2: ValueOverTimeCREditor = filtered[i];

                        // If all dates set
                        if (h2.startDate && h2.endDate) {
                            let s2: Date = this.service.getDateFromDateString(h2.startDate);
                            let e2: Date = this.service.getDateFromDateString(h2.endDate);

                            // Determine if there is an overlap
                            if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                                h1.conflictMessages.add(this.service.overlapMessage);

                                if (s2.getTime() === e2.getTime()) {
                                    h2.conflictMessages.add(this.service.overlapMessage);
                                }

                                hasConflict = true;
                            } else if (this.service.addDay(1, h1.endDate) === h2.startDate && Utils.areValuesEqual(attributeType, h1.value, h2.value)) {
                                h1.conflictMessages.add(this.service.mergeContiguousMessage);
                                h2.conflictMessages.add(this.service.mergeContiguousMessage);
                                hasConflict = true;
                            }
                        } else if ((i === j - 1 || i === j + 1) && e1 && h2.startDate) {
                            // If 1st end date and current start date

                            let s2: Date = this.service.getDateFromDateString(h2.startDate);

                            if (s2.getTime() <= e1) {
                                h1.conflictMessages.add(this.service.overlapMessage);
                                h2.conflictMessages.add(this.service.overlapMessage);

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
                        next.conflictMessages.add(this.service.gapMessage);

                        current.conflictMessages.add(this.service.gapMessage);
                    }
                }
            }

            current = next;
        }

        this.sort(ranges);

        return hasConflict;
    }


    public sort(votArr: TimeRangeEntry[]): void {
        // Sort the data by start date
        votArr.sort(function (a, b) {
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
            if (!range.conflictMessages) {
                range.conflictMessages = new Set();
            }

            range.conflictMessages.delete(this.service.outsideExistsMessage);
        });

        // Filter DELETE entries from consideration
        const filtered: ValueOverTimeCREditor[] = ranges.filter(range => !range.isDelete());

        const filteredExists = existEntries.filter(range => !range.isDelete());

        // Check for outside exists range
        for (let j = 0; j < filtered.length; j++) {
            const h1 = filtered[j];

            if (h1.startDate && h1.endDate) {
                let s1: any = this.service.getDateFromDateString(h1.startDate);
                let e1: any = this.service.getDateFromDateString(h1.endDate);

                let inRange = false;

                for (let i = 0; i < filteredExists.length; i++) {
                    const h2 = filteredExists[i];

                    // If all dates set
                    if (h2.value && h2.startDate && h2.endDate) {
                        let s2: Date = this.service.getDateFromDateString(h2.startDate);
                        let e2: Date = this.service.getDateFromDateString(h2.endDate);

                        if (!Utils.dateRangeOutside(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                            inRange = true;
                        }
                    }
                }

                if (!inRange) {
                    h1.conflictMessages.add(this.service.outsideExistsMessage);
                    hasConflict = true;
                }
            }
        }

        return hasConflict;
    }

}
