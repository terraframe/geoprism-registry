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

import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ScheduledJobOverview } from "@registry/model/registry";

import { ErrorHandler } from "@shared/component";

@Component({
    selector: "job-conflict-modal",
    templateUrl: "./job-conflict-modal.component.html",
    styleUrls: []
})
export class JobConflictModalComponent implements OnInit {

    message: string = null;
    problem: any;
    job: ScheduledJobOverview;

    /*
     * Observable subject for submission.  Called when an update is successful
     */
    onConflictAction: Subject<any>;

    readonly: boolean = false;
    edit: boolean = false;

    constructor(public bsModalRef: BsModalRef) {
        this.onConflictAction = new Subject();
    }

    ngOnInit(): void {

    }

    onProblemResolvedListener(problem: any): void {
        this.onConflictAction.next({ action: "RESOLVED", data: problem });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
