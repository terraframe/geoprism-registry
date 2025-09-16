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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler } from "@shared/component";

import { RegistryService, IOService, SynchronizationConfigService } from "@registry/service";
import { ScheduledJob, SynchronizationConfig } from "@registry/model/registry";
import { PageResult } from "@shared/model/core";
import { interval, Observable, Subscription, switchMap, timeout } from "rxjs";

@Component({
    selector: "sync-details",
    templateUrl: "./details.component.html",
    styleUrls: ["./details.css"]
})
export class SyncDetailsComponent implements OnInit, OnDestroy {

    message: string = null;
    job: ScheduledJob;
    historyId: string = "";

    config: SynchronizationConfig = null;

    page: PageResult<any> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    pollingSubscription: Subscription = null;

    constructor(private configService: SynchronizationConfigService, public service: RegistryService, private route: ActivatedRoute) {
    }

    ngOnInit(): void {
        this.historyId = this.route.snapshot.params["oid"];

        const configOid = this.route.snapshot.paramMap.get("config");

        this.configService.get(configOid).then(config => {
            this.config = config;

            this.onPageChange(1);
        });
    }

    ngOnDestroy() {
        if (this.pollingSubscription != null) {
            this.pollingSubscription.unsubscribe();
        }
    }

    formatGeoObjectCode(codes: string) {
        return codes == null ? "" : codes.replace(/,/g, ", ");
    }

    formatAffectedRows(rows: string) {
        return rows == null ? "" : rows.replace(/,/g, ", ");
    }

    onPageChange(pageNumber: any): void {
        this.message = null;

        this.service.getExportDetails(this.historyId, this.page.pageSize, pageNumber).then(response => {
            this.job = response;

            this.page = this.job.exportErrors;

            if (response.exception && response.exception.type && response.exception.type.indexOf("ExportJobHasErrors") === -1) {
                this.error(response.exception);
            }
            else if (this.job.stage === 'EXPORT' && this.pollingSubscription == null) {
                const pollInterval = 60000; // Poll every 60 seconds
                const timeoutInterval = pollInterval * 60; // Poll every 60 seconds

                this.pollingSubscription = interval(pollInterval).pipe(timeout(timeoutInterval)).subscribe(() => {
                    this.onPageChange(this.page.pageNumber)
                });
            }
            
            if (this.job.stage !== 'EXPORT' && this.pollingSubscription != null) {
                // The job is finished. Stop polling
                this.pollingSubscription.unsubscribe();

                this.pollingSubscription = null;
            }


        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: any): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
