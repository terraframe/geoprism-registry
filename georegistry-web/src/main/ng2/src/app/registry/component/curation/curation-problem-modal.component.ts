import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ScheduledJobOverview } from "@registry/model/registry";

import { ErrorHandler } from "@shared/component";
import { CurationJob, CurationProblem } from "@registry/model/list-type";

@Component({
    selector: "curation-problem-modal",
    templateUrl: "./curation-problem-modal.component.html",
    styleUrls: []
})
export class CurationProblemModalComponent {

    message: string = null;

    problem: CurationProblem;
    job: CurationJob;
    callback: Observer<any>;

    readonly: boolean = false;
    edit: boolean = false;

    constructor(public bsModalRef: BsModalRef) {
    }

    init(problem: CurationProblem, job: CurationJob, callback: Observer<any>): void {
        this.problem = problem;
        this.job = job;
        this.callback = callback;
    }

    onProblemResolvedListener(problem: CurationProblem): void {
        this.callback.next({ action: "RESOLVED", data: problem });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
