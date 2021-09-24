import { Component, OnInit, Input } from "@angular/core";

import { TimeRangeEntry } from "@registry/model/registry";
import { LocalizationService } from "@shared/service";
import { DateService } from "@shared/service/date.service";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { ChangeRequestEditor } from "./change-request-editor";
import { GeoObjectSharedAttributeEditorComponent } from "./geoobject-shared-attribute-editor.component";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";

export interface DateBoundary { date: string; isStart: boolean; }

export interface TimelineEntry { width: number, x: number, period: TimeRangeEntry }

export interface DataTimeSpan {startDay: number, startDate: string, displayStartDate: string, endDay: number, endDate: string, displayEndDate: string, span: number}

/*
 * This component is shared between:
 * -
 *
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
@Component({
    selector: "stability-period",
    templateUrl: "./stability-period.component.html",
    styleUrls: ["./stability-period.component.css"]
})
export class StabilityPeriodComponent implements OnInit {

    @Input() changeRequestEditor: ChangeRequestEditor;

    @Input() sharedAttributeEditor: GeoObjectSharedAttributeEditorComponent;

    @Input() filterDate: string;

    @Input() latestPeriodIsActive: boolean = false;

    periods: TimeRangeEntry[] = [];

    timelines: [TimelineEntry[]];

    activeEntry: TimelineEntry = null;

    private infinityDayPadding: number = 15;

    dataTimeSpan: DataTimeSpan = null;

    constructor(private lService: LocalizationService, public dateService: DateService) {}

    ngOnInit(): void {
        this.generate();

        this.changeRequestEditor.onChangeSubject.subscribe(() => {
            this.generate();
        });

        let timeline = this.timelines[0];
        if (timeline && timeline.length > 0) {
            if (this.filterDate != null) {
                let index = timeline.findIndex(entry => this.dateService.between(this.filterDate, entry.period.startDate, entry.period.endDate));

                if (index !== -1) {
                    this.activeEntry = timeline[index];
                }
            } else if (this.latestPeriodIsActive) {
                this.setActiveTimelineEntry(timeline[timeline.length - 1]);
            }
        }
    }

    navigateRelative(amount: number) {
        let timeline: TimelineEntry[] = this.timelines[0];
        if (timeline == null || timeline.length === 0) { return; }

        if (this.activeEntry == null) {
            this.setActiveTimelineEntry(timeline[0]);
            return;
        }

        let index = timeline.findIndex(entry => entry.period.startDate === this.activeEntry.period.startDate);

        if (index !== -1) {
            let nextIndex = index + amount;

            if (nextIndex < 0) {
                nextIndex = timeline.length - 1;
            } else if (nextIndex >= timeline.length) {
                nextIndex = 0;
            }

            this.setActiveTimelineEntry(timeline[nextIndex]);
        }
    }

    setActiveTimelineEntry(entry: TimelineEntry, refresh: boolean = true) {
        if (this.activeEntry != null && entry != null && entry.period.startDate === this.activeEntry.period.startDate) {
            entry = null;
        }

        this.activeEntry = entry;
        this.sharedAttributeEditor.setFilterDate(entry == null ? null : entry.period.startDate, refresh);
    }

    generate() {
        this.generatePeriods();
        this.generateTimelines();
    }

    calculateDataTimeSpan(): void {
        let startDate: string = null;
        let endDate: string = null;
        let endDay: number = null;
        let startDay: number = null;

        if (this.periods.length > 0) {
            startDate = this.periods[0].startDate;
            endDate = this.dateService.formatDateForDisplay(this.periods[this.periods.length - 1].endDate);

            startDay = this.dateService.getDateFromDateString(startDate).getTime() / (1000 * 60 * 60 * 24);

            if (endDate === "5000-12-31") {
                endDay = this.dateService.getDateFromDateString(this.periods[this.periods.length - 1].startDate).getTime() / (1000 * 60 * 60 * 24);
                this.infinityDayPadding = (endDay - startDay) * 0.05;
                endDay = this.infinityDayPadding + endDay;
            } else {
                endDay = this.dateService.getDateFromDateString(endDate).getTime() / (1000 * 60 * 60 * 24);
            }
        }

        this.dataTimeSpan = { startDay: startDay, startDate: startDate, displayStartDate: this.dateService.formatDateForDisplay(startDate), endDay: endDay, endDate: endDate, displayEndDate: this.dateService.formatDateForDisplay(endDate), span: (endDay - startDay) };
    }

    generateTimelines() {
        this.timelines = [] as any;

        if (this.periods.length === 0) {
            return;
        } else if (this.periods.length === 1) {
            this.setActiveTimelineEntry(null, false);
            return;
        }

        this.calculateDataTimeSpan();

        let currentTimeline: any = [];
        this.timelines.push(currentTimeline);
        let daysLeft = this.dataTimeSpan.span;

        let len = this.periods.length;
        for (let i = 0; i < len; ++i) {
            let period = this.periods[i];

            let start: Date = this.dateService.getDateFromDateString(period.startDate);
            let end: Date = this.dateService.getDateFromDateString(period.endDate);

            let startDay = start.getTime() / (1000 * 60 * 60 * 24);
            let endDay = end.getTime() / (1000 * 60 * 60 * 24);
            if (period.endDate === "5000-12-31") {
                endDay = startDay + this.infinityDayPadding;
            }

            let daysInPeriod: number = (endDay - startDay);
            if (daysLeft - daysInPeriod < 0) {
                let daysInFirstEntry = daysLeft;
                let timelineEntry1: TimelineEntry = { width: (daysInFirstEntry / this.dataTimeSpan.span) * 100, x: ((startDay - this.dataTimeSpan.startDay) / this.dataTimeSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry1);

                currentTimeline = [];
                this.timelines.push(currentTimeline);
                daysLeft = this.dataTimeSpan.span;

                let timelineEntry2: TimelineEntry = { width: ((daysInPeriod - daysInFirstEntry) / this.dataTimeSpan.span) * 100, x: ((startDay - this.dataTimeSpan.startDay) / this.dataTimeSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry2);
            } else {
                let timelineEntry: TimelineEntry = { width: (daysInPeriod / this.dataTimeSpan.span) * 100, x: ((startDay - this.dataTimeSpan.startDay) / this.dataTimeSpan.span) * 100, period: period };
                currentTimeline.push(timelineEntry);
                daysLeft = daysLeft - daysInPeriod;

                if (daysLeft === 0) {
                    currentTimeline = [];
                    this.timelines.push(currentTimeline);
                    daysLeft = this.dataTimeSpan.span;
                }
            }
        }

        // console.log(this.timelines);
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
