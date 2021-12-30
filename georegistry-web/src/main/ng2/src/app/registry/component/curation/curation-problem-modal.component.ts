import { Component } from "@angular/core";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Observer } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { ScheduledJobOverview } from "@registry/model/registry";

import { ErrorHandler } from "@shared/component";
import { CurationJob, CurationProblem, ListTypeVersion } from "@registry/model/list-type";
import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";
import { DateService } from "@shared/service";

@Component({
    selector: "curation-problem-modal",
    templateUrl: "./curation-problem-modal.component.html",
    styleUrls: []
})
export class CurationProblemModalComponent {

    message: string = null;
    version: ListTypeVersion;
    problem: CurationProblem;
    job: CurationJob;
    callback: Observer<any>;

    readonly: boolean = false;
    edit: boolean = false;

    constructor(public bsModalRef: BsModalRef, private modalService: BsModalService, private dateService: DateService) {
    }

    init(version: ListTypeVersion, problem: CurationProblem, job: CurationJob, callback: Observer<any>): void {
        this.version = version;
        this.problem = problem;
        this.job = job;
        this.callback = callback;
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "NO_GEOMETRY") {
            // return this.localizeService.decode("scheduledjobs.job.problem.type.parent.lookup");
            return 'Missing geometry';
        }

        return probType;
    }

    onEditGeoObject(): void {
        let editModal = this.modalService.show(GeoObjectEditorComponent, {
            backdrop: true,
            ignoreBackdropClick: true
        });

        editModal.content.configureAsExisting(this.problem.goCode, this.problem.typeCode, this.version.forDate, true);
        editModal.content.setMasterListId(this.version.oid);
        editModal.content.setOnSuccessCallback(() => {
            this.onProblemResolvedListener(this.problem);
            this.bsModalRef.hide();
        });
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
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
