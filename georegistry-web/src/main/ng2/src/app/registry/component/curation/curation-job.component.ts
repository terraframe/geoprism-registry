import { Component, OnInit } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HttpErrorResponse } from "@angular/common/http";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { RegistryService, IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { ScheduledJob } from "@registry/model/registry";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";

import { GeoRegistryConfiguration } from "@core/model/registry";
import { JobConflictModalComponent } from "../scheduled-jobs/conflict-widgets/job-conflict-modal.component";
import { PageResult } from "@shared/model/core";
import { ListTypeService } from "@registry/service/list-type.service";
import { CurationJob, CurationProblem, ListTypeVersion } from "@registry/model/list-type";
import { CurationProblemModalComponent } from "./curation-problem-modal.component";
declare let registry: GeoRegistryConfiguration;

@Component({
    selector: "curation-job",
    templateUrl: "./curation-job.component.html",
    styleUrls: []
})
export class CurationJobComponent implements OnInit {

    message: string = null;
    allSelected: boolean = false;

    version: ListTypeVersion;
    job: CurationJob;

    page: PageResult<CurationProblem> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    timeCounter: number = 0;

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    isPolling: boolean = false;
    hasRowValidationProblem: boolean = false;

    notifier: WebSocketSubject<{ type: string, message: string }>;

    constructor(public service: ListTypeService, private modalService: BsModalService,
        private router: Router, private route: ActivatedRoute, private dateService: DateService,
        private localizeService: LocalizationService, authService: AuthService, public ioService: IOService) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

        const oid = this.route.snapshot.params["oid"];
        this.service.getVersion(oid).then(version => {
            this.version = version;

            this.onPageChange(1);
        })


        let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + registry.contextPath;

        this.notifier = webSocket(baseUrl + "/websocket/notify");
        this.notifier.subscribe(message => {
            if (message.type === "CURATION_JOB_CHANGE") {
                this.onPageChange(this.page.pageNumber);
            }
        });
    }

    ngOnDestroy() {
    }

    formatAffectedRows(rows: string) {
        return rows.replace(/,/g, ", ");
    }

    formatValidationResolve(obj: any) {
        return JSON.stringify(obj);
    }

    onProblemResolved(problem: any): void {

        const index = this.page.resultSet.findIndex(p => p.id === problem.id);

        if (index !== -1) {
            this.page.resultSet.splice(index, 1);
        }
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "NO_GEOMETRY") {
            // return this.localizeService.decode("scheduledjobs.job.problem.type.parent.lookup");
            return 'Missing geometry';
        }

        return probType;
    }

    onEdit(problem: CurationProblem): void {
        this.bsModalRef = this.modalService.show(CurationProblemModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.version, problem, this.job, (result: any) => {
            if (result.action === "RESOLVED") {
                this.onProblemResolved(result.data);
            }
        });
    }

    onPageChange(pageNumber: any): void {
        if (this.version != null) {

            this.message = null;

            this.service.getCurationInfo(this.version, true, pageNumber, this.page.pageSize).then(response => {
                this.job = response;

                if (this.job.status === "SUCCESS") {
                    this.page = this.job.page;
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    toggleAll(): void {
        this.allSelected = !this.allSelected;

        this.job.page.resultSet.forEach(row => {
            row.selected = this.allSelected;
        });
    }

    onResolveScheduledJob(historyId: string): void {
        // if (this.page.resultSet.length === 0) {
        //     // this.service.resolveScheduledJob(historyId).then(response => {
        //     //     this.router.navigate(["/registry/scheduled-jobs"]);
        //     // }).catch((err: HttpErrorResponse) => {
        //     //     this.error(err);
        //     // });
        // } else {
        //     this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
        //         animated: true,
        //         backdrop: true,
        //         ignoreBackdropClick: true
        //     });

        //     if (this.job.stage === "VALIDATION_RESOLVE") {
        //         this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.validationDescription");
        //         this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.validationButton");
        //     } else {
        //         this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.importDescription");
        //         this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.importButton");
        //     }

        //     this.bsModalRef.content.type = ModalTypes.danger;

        //     this.bsModalRef.content.onConfirm.subscribe(data => {
        //         this.service.resolveScheduledJob(historyId).then(response => {
        //             this.router.navigate(["/registry/scheduled-jobs"]);
        //         }).catch((err: HttpErrorResponse) => {
        //             this.error(err);
        //         });
        //     });
        // }
    }

    // onCancelScheduledJob(historyId: string): void {
    //     this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
    //         animated: true,
    //         backdrop: true,
    //         ignoreBackdropClick: true
    //     });

    //     this.bsModalRef.content.message = this.localizeService.decode("etl.import.cancel.modal.description");
    //     this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.cancel.modal.button");

    //     this.bsModalRef.content.type = ModalTypes.danger;

    //     this.bsModalRef.content.onConfirm.subscribe(data => {
    //         this.ioService.cancelImport(this.job.configuration).then(response => {
    //             // this.bsModalRef.hide()
    //             this.router.navigate(["/registry/scheduled-jobs"]);
    //         }).catch((err: HttpErrorResponse) => {
    //             this.error(err);
    //         });
    //     });
    // }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
