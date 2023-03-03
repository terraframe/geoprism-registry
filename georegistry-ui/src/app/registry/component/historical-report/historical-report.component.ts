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

import { Component, ViewEncapsulation } from "@angular/core";
import { HttpErrorResponse, HttpParams } from "@angular/common/http";
import { trigger, style, animate, transition } from "@angular/animations";

import { ErrorHandler } from "@shared/component";
import { PageResult } from "@shared/model/core";
import { TransitionEventService } from "@registry/service/transition-event.service";
import { HistoricalRow } from "@registry/model/transition-event";
import { AuthService, DateService } from "@shared/service";
import { IOService } from "@registry/service";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

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
        type: "",
        startDate: "",
        endDate: ""
    }

    types: { label: string, code: string, super?: { label?: string, code?: string } }[] = [];

    isValid: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(private service: TransitionEventService, private iService: IOService, private authService: AuthService,
        public dateService: DateService) { }

    ngOnInit(): void {
        this.iService.listGeoObjectTypes(true).then(types => {
            this.types = types;
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

    exportToExcel(): void {
        let params: HttpParams = new HttpParams();
        params = params.set("typeCode", this.data.type.toString());
        params = params.set("startDate", this.data.startDate.toString());
        params = params.set("endDate", this.data.endDate.toString());

        window.location.href = environment.apiUrl + "/api/transition-event/export-excel?" + params.toString();
    }

    checkDates(): any {
        setTimeout(() => {
            this.isValid = (this.data.startDate != null && this.data.endDate != null && !this.dateService.after(this.data.startDate, this.data.endDate));
        }, 0);
    }

    public error(err: any): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
