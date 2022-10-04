import { Component, OnInit, Input, OnDestroy } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";

import { TimeRangeEntry } from "@registry/model/registry";
import { LocalizationService } from "@shared/service";
import { DateService } from "@shared/service/date.service";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { ChangeRequestEditor } from "./change-request-editor";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";
import { ChangeType } from "@registry/model/constants";
import { Subscription } from "rxjs";
import { LocationManagerService } from "@registry/service/location-manager.service";
import { LocationManagerState } from "../location-manager/location-manager.component";

export interface DateBoundary { date: string; isStart: boolean; isEnd: boolean }

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
    styleUrls: ["./stability-period.component.css"],
    animations: [
        [
            trigger("fadeInOut", [
                transition(":enter", [
                    style({
                        opacity: 0
                    }),
                    animate("1000ms")
                ]),
                transition(":leave",
                    animate("1000ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]]
})
export class StabilityPeriodComponent implements OnInit, OnDestroy {

    @Input() changeRequestEditor: ChangeRequestEditor;

    @Input() context: string;

    @Input() latestPeriodIsActive: boolean = false;

    periods: TimeRangeEntry[] = [];

    timelines: [TimelineEntry[]];

    activeEntry: TimelineEntry = null;

    forDateEntry: TimelineEntry = null;

    private infinityDayPadding: number = 15;

    dataTimeSpan: DataTimeSpan = null;

    private subscription: Subscription;

    private forDate: string;

    _showHint: boolean = false;
    // eslint-disable-next-line accessor-pairs
    @Input() set showHint(val: boolean) {
        this._showHint = val;

        setTimeout(() => {
            this.showHint = false;
        }, 10000);
    }

    // eslint-disable-next-line no-useless-constructor
    constructor(private lService: LocalizationService, public dateService: DateService, private locationManagerService: LocationManagerService) {}

    ngOnInit(): void {
        this.generate();

        this.changeRequestEditor.onChangeSubject.subscribe((type: ChangeType) => {
            if (type === ChangeType.END_DATE || type === ChangeType.START_DATE) {
                this.generate();
            }
        });

        this.subscription = this.locationManagerService.stateChange$.subscribe(state => this.handleStateChange(state));

        this.calculateActiveTimelineEntry();
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    handleStateChange(newState: LocationManagerState) {
        if (this.forDate !== newState.date) {
            this.forDate = newState.date;
            this.calculateActiveTimelineEntry();
        }
    }

    calculateActiveTimelineEntry() {
        if (this.timelines != null) {
            let timeline = this.timelines[0];
            if (timeline && timeline.length > 1) {
                if (this.forDate != null) {
                    let index = timeline.findIndex(entry => this.dateService.between(this.forDate, entry.period.startDate, entry.period.endDate));

                    if (index !== -1) {
                        this.activeEntry = timeline[index];
                    }
                } else if (this.latestPeriodIsActive) {
                    this.setActiveTimelineEntry(timeline[timeline.length - 1]);
                }

                if (this.forDate != null) {
                    let forDateIndex = timeline.findIndex(entry => this.dateService.between(this.forDate, entry.period.startDate, entry.period.endDate));

                    if (forDateIndex !== -1) {
                        this.forDateEntry = timeline[forDateIndex];
                    }
                }
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

    setActiveTimelineEntry(entry: TimelineEntry) {
        if (this.periods.length <= 1) {
            entry = null;
        }

        if (this.activeEntry && entry && entry.period.startDate === this.activeEntry.period.startDate) {
            entry = null;
        }

        this.activeEntry = entry;
        this.locationManagerService.setState({ date: entry == null ? null : entry.period.startDate }, false);
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
            endDate = this.periods[this.periods.length - 1].endDate;

            startDay = this.dateService.getDateFromDateString(startDate).getTime() / (1000 * 60 * 60 * 24);

            if (endDate === "5000-12-31") {
                if (this.periods.length > 1) {
                    endDay = this.dateService.getDateFromDateString(this.periods[this.periods.length - 1].startDate).getTime() / (1000 * 60 * 60 * 24);
                    this.infinityDayPadding = (endDay - startDay) * 0.05;
                    endDay = this.infinityDayPadding + endDay;
                } else {
                    endDay = startDay + this.infinityDayPadding;
                }
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
            this.setActiveTimelineEntry(null);
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
                        let startIndex = boundaries.findIndex(boundary => period.startDate === boundary.date);
                        if (startIndex !== -1) {
                            boundaries[startIndex].isStart = true;
                        } else {
                            boundaries.push({ date: period.startDate, isStart: true, isEnd: false });
                        }

                        let endIndex = boundaries.findIndex(boundary => period.endDate === boundary.date);
                        if (endIndex !== -1) {
                            boundaries[endIndex].isEnd = true;
                        } else {
                            boundaries.push({ date: period.endDate, isStart: false, isEnd: true });
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
        let dlen = boundaries.length;
        for (let i = 0; i < dlen; ++i) {
            let current: DateBoundary = boundaries[i];
            let next: DateBoundary = i + 1 > dlen ? null : boundaries[i + 1];

            if (current.isStart && current.isEnd) {
                this.periods.push({ startDate: current.date, endDate: current.date });
            }
            if (current.isEnd && (next != null && next.isStart && this.dateService.addDay(1, current.date) === next.date)) {
                continue;
            }

            let startDate = (current.isEnd ? this.dateService.addDay(1, current.date) : current.date);

            if (next != null && this.changeRequestEditor.existsAtDate(startDate)) {
                let endDate = (!next.isStart ? next.date : this.dateService.addDay(-1, next.date));

                this.periods.push({ startDate: startDate, endDate: endDate });
            }
        }

        this.dateService.sort(this.periods);
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

}
