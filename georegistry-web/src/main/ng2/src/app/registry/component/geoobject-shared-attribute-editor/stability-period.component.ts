import { Component, OnInit, Input } from "@angular/core";

import { GeoObjectOverTime, AttributeType, TimeRangeEntry } from "@registry/model/registry";
import { LocalizationService } from "@shared/service";
import { DateService } from "@shared/service/date.service";

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

    @Input() geoObjectOverTime: GeoObjectOverTime;

    periods: TimeRangeEntry[] = [];

    constructor(private lService: LocalizationService, private dateService: DateService) {}

    ngOnInit(): void {
        this.generatePeriods();
    }

    generatePeriods() {
        let len = this.geoObjectOverTime.geoObjectType.attributes.length;

        let boundaries: DateBoundary[] = [];

        // Create an array which contains all the unique start and end dates
        for (let i = 0; i < len; ++i) {
            let attr: AttributeType = this.geoObjectOverTime.geoObjectType.attributes[i];

            if (attr.isChangeOverTime) {
                let values = this.geoObjectOverTime.attributes[attr.code].values;

                let valLen = values.length;
                for (let j = 0; j < valLen; ++j) {
                    let period: TimeRangeEntry = values[j];

                    if (boundaries.findIndex(boundary => period.startDate === boundary.date) === -1) {
                        boundaries.push({ date: period.startDate, isStart: true });
                    }
                    if (boundaries.findIndex(boundary => period.endDate === boundary.date) === -1) {
                        boundaries.push({ date: period.endDate, isStart: false });
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
        let len = this.geoObjectOverTime.geoObjectType.attributes.length;

        for (let i = 0; i < len; ++i) {
            let attr: AttributeType = this.geoObjectOverTime.geoObjectType.attributes[i];

            if (attr.isChangeOverTime) {
                let values = this.geoObjectOverTime.attributes[attr.code].values;

                let valLen = values.length;
                for (let j = 0; j < valLen; ++j) {
                    let period: TimeRangeEntry = values[j];

                    if (this.dateService.between(date, period.startDate, period.endDate)) {
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
