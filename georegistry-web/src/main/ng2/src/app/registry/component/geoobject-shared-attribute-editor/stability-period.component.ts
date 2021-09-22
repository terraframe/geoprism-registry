import { Component, OnInit, Input } from "@angular/core";

import { AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { LocalizationService } from "@shared/service";
import { DateService } from "@shared/service/date.service";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { ChangeRequestEditor } from "./change-request-editor";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";

export interface DateBoundary {

    date: string;
    isStart: boolean;

}

/*
 * This component is shared between:
 * -
 *
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
@Component({
    selector: "stability-period",
    templateUrl: "./stability-period.component.html"
})
export class StabilityPeriodComponent implements OnInit {

    @Input() changeRequestEditor: ChangeRequestEditor;

    periods: TimeRangeEntry[] = [];

    timelines: [[{ width: number, x: number, period: TimeRangeEntry }]];

    constructor(private lService: LocalizationService, private dateService: DateService) {}

    ngOnInit(): void {
        this.generate();

        this.changeRequestEditor.onChangeSubject.subscribe(() => {
            this.generate();
        });
    }

    generate() {
        this.generatePeriods();
        this.generateTimelines();
    }

    calculateDataTimeSpan(): {startDay: number, endDay: number, span: number} {
        let startDay: number = null;
        let endDay: number = null;

        if (this.periods.length === 1) {
            startDay = this.dateService.getDateFromDateString(this.periods[0].startDate).getTime() / (1000 * 60 * 60 * 24);
            endDay = this.dateService.getDateFromDateString(this.periods[0].endDate).getTime() / (1000 * 60 * 60 * 24);
        } else {
            startDay = this.dateService.getDateFromDateString(this.periods[0].startDate).getTime() / (1000 * 60 * 60 * 24);

            if (this.periods[this.periods.length - 1].endDate === "5000-12-31") {
                endDay = this.dateService.getDateFromDateString(this.periods[this.periods.length - 1].startDate).getTime() / (1000 * 60 * 60 * 24) + 15;
            } else {
                endDay = this.dateService.getDateFromDateString(this.periods[this.periods.length - 1].endDate).getTime() / (1000 * 60 * 60 * 24);
            }
        }

        return { startDay: startDay, endDay: endDay, span: (endDay - startDay) };
    }

    generateTimelines() {
        this.timelines = [] as any;

        if (this.periods.length === 0) {
            return;
        }

        let dataSpan: {startDay: number, endDay: number, span: number} = this.calculateDataTimeSpan();

        let currentTimeline: any = [];
        this.timelines.push(currentTimeline);
        let daysLeft = dataSpan.span;

        let len = this.periods.length;
        for (let i = 0; i < len; ++i) {
            let period = this.periods[i];

            let start: Date = this.dateService.getDateFromDateString(period.startDate);
            let end: Date = this.dateService.getDateFromDateString(period.endDate);

            let startDay = start.getTime() / (1000 * 60 * 60 * 24);
            let endDay = end.getTime() / (1000 * 60 * 60 * 24);
            if (period.endDate === "5000-12-31") {
                endDay = startDay + 15;
            }

            let daysInPeriod: number = (endDay - startDay);
            if (daysLeft - daysInPeriod < 0) {
                let daysInFirstEntry = daysLeft;
                let timelineEntry1 = { width: (daysInFirstEntry / dataSpan.span) * 100, x: ((startDay - dataSpan.startDay) / dataSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry1);

                currentTimeline = [];
                this.timelines.push(currentTimeline);
                daysLeft = dataSpan.span;

                let timelineEntry2 = { width: ((daysInPeriod - daysInFirstEntry) / dataSpan.span) * 100, x: ((startDay - dataSpan.startDay) / dataSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry2);
            } else {
                let timelineEntry = { width: (daysInPeriod / dataSpan.span) * 100, x: ((startDay - dataSpan.startDay) / dataSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry);
                daysLeft = daysLeft - daysInPeriod;

                if (daysLeft === 0) {
                    currentTimeline = [];
                    this.timelines.push(currentTimeline);
                    daysLeft = dataSpan.span;
                }
            }
        }

        console.log(this.timelines);
    }

    generatePeriods() {
        let boundaries: DateBoundary[] = [];

        // Create an array which contains all the unique start and end dates
        let editors: (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[] = this.changeRequestEditor.getEditors();
        let len = editors.length;

        for (let i = 0; i < len; ++i) {
            if (editors[i] instanceof ChangeRequestChangeOverTimeAttributeEditor) {
                let editor: ChangeRequestChangeOverTimeAttributeEditor = editors[i] as ChangeRequestChangeOverTimeAttributeEditor;
                let values = editor.getEditors();

                let valLen = values.length;
                for (let j = 0; j < valLen; ++j) {
                    let period: ValueOverTimeCREditor = values[j];

                    if (period.startDate != null && period.endDate != null && !period.isDelete()) {
                        if (boundaries.findIndex(boundary => period.startDate === boundary.date) === -1) {
                            boundaries.push({ date: period.startDate, isStart: true });
                        }
                        if (boundaries.findIndex(boundary => period.endDate === boundary.date) === -1) {
                            boundaries.push({ date: period.endDate, isStart: false });
                        }
                    }
                }
            }
        }

        // Sort the date boundaries
        boundaries.sort(function(a, b) {
            if (a.date == null || a.date === "") {
                return 1;
            } else if (b.date == null || b.date === "") {
                return -1;
            }

            let first: any = new Date(a.date);
            let next: any = new Date(b.date);
            return first - next;
        });

        // Loop over the boundaries and create versions between all the boundaries, but only if there is data between them
        this.periods = [];
        let dlen = boundaries.length - 1;
        for (let i = 0; i < dlen; ++i) {
            let current: DateBoundary = boundaries[i];
            let next: DateBoundary = boundaries[i + 1];

            // If there is data in this range
            if (current.isStart || this.hasDataAtDate(this.dateService.addDay(1, current.date))) {
                let startDate = (current.isStart ? current.date : this.dateService.addDay(1, current.date));
                let endDate = (!next.isStart ? next.date : this.dateService.addDay(-1, next.date));

                this.periods.push({ startDate: startDate, endDate: endDate });
            }
        }

        this.dateService.sort(this.periods);
    }

    hasDataAtDate(date: string): boolean {
        let editors: (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[] = this.changeRequestEditor.getEditors();
        let len = editors.length;

        for (let i = 0; i < len; ++i) {
            if (editors[i] instanceof ChangeRequestChangeOverTimeAttributeEditor) {
                let editor: ChangeRequestChangeOverTimeAttributeEditor = editors[i] as ChangeRequestChangeOverTimeAttributeEditor;

                let values = editor.getEditors();

                let valLen = values.length;
                for (let j = 0; j < valLen; ++j) {
                    let period: TimeRangeEntry = values[j];

                    if (period.startDate != null && period.endDate != null && this.dateService.between(date, period.startDate, period.endDate)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

}
