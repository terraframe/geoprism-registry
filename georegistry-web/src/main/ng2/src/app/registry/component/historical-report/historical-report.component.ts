import { Component, ViewEncapsulation } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { trigger, style, animate, transition } from "@angular/animations";

import { ErrorHandler } from "@shared/component";
import { PageResult } from "@shared/model/core";
import { TransitionEventService } from "@registry/service/transition-event.service";
import { HistoricalRow } from "@registry/model/transition-event";
import { AuthService, DateService } from "@shared/service";
import { IOService } from "@registry/service";

@Component({

    selector: "historical-report",
    templateUrl: "./historical-report.component.html",
    styleUrls: [],
    encapsulation: ViewEncapsulation.None,
    animations: [
        [
            trigger("fadeInOut", [
                transition(":enter", [
                    style({
                        opacity: 0
                    }),
                    animate("300ms")
                ]),
                transition(":leave",
                    animate("100ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ]),
            trigger("fadeIn", [
                transition(":enter", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ])
            ])
        ]
    ]
})
export class HistoricalReportComponent {

    message: string = null;

    page: PageResult<HistoricalRow> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    data = {
        type: '',
        startDate: '',
        endDate: ''
    }

    types: { label: string, code: string }[] = [];

    isValid: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, private iService: IOService, private authService: AuthService,
        private dateService: DateService) { }

    ngOnInit(): void {

        this.iService.listGeoObjectTypes(true).then(types => {
            this.types = types.filter(t => {
                const orgCode = t.orgCode;
                const typeCode = t.superTypeCode != null ? t.superTypeCode : t.code;

                return this.authService.isGeoObjectTypeRM(orgCode, typeCode);
            });
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    refresh(pageNumber: number = 1): void {
        this.service.getHistoricalReport(this.data.type, this.data.startDate, this.data.endDate, this.page.pageSize, pageNumber).then(page => {
            this.page = page;
        }).catch((response: HttpErrorResponse) => {
            this.error(response);
        });
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    checkDates(): any {
        setTimeout(() => {
            this.isValid = (this.data.startDate != null && this.data.endDate != null);
        }, 0);
    }

    public error(err: any): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
