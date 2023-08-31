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
import { Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HttpErrorResponse } from "@angular/common/http";
import { interval } from "rxjs";

import { RegistryService, IOService } from "@registry/service";
import { ScheduledJob, ScheduledJobOverview } from "@registry/model/registry";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";
import { PageResult } from "@shared/model/core";

@Component({
    selector: "scheduled-jobs",
    templateUrl: "./scheduled-jobs.component.html",
    styleUrls: ["./scheduled-jobs.css"]
})
export class ScheduledJobsComponent implements OnInit {

    message: string = null;

    activeJobsPage: PageResult<any> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    completeJobsPage: PageResult<any> = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        resultSet: []
    };

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    activeTimeCounter: number = 0;
    completeTimeCounter: number = 0;

    pollingData: any;

    isViewAllOpen: boolean = false;

    constructor(public service: RegistryService,
        private modalService: BsModalService,
        private router: Router,
        private localizeService: LocalizationService,
        private ioService: IOService,
        authService: AuthService) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        this.onActiveJobsPageChange(1);

        this.pollingData = interval(1000).subscribe(() => {
            this.activeTimeCounter++;
            this.completeTimeCounter++;

            if (this.isViewAllOpen) {
                if (this.activeTimeCounter >= 4) {
                    this.onActiveJobsPageChange(this.activeJobsPage.pageNumber);

                    this.activeTimeCounter = 0;
                }
                if (this.completeTimeCounter >= 7) {
                    this.onCompleteJobsPageChange(this.completeJobsPage.pageNumber);

                    this.completeTimeCounter = 0;
                }
            } else {
                if (this.activeTimeCounter >= 2) {
                    this.onActiveJobsPageChange(this.activeJobsPage.pageNumber);

                    this.activeTimeCounter = 0;
                }
            }
        });
    }

    ngOnDestroy() {
        this.pollingData.unsubscribe();
    }

    formatJobStatus(job: ScheduledJobOverview) {
        if (job.status === "FEEDBACK") {
            return this.localizeService.decode("etl.JobStatus.FEEDBACK");
        } else if (job.status === "RUNNING" || job.status === "NEW") {
            return this.localizeService.decode("etl.JobStatus.RUNNING");
        } else if (job.status === "QUEUED") {
            return this.localizeService.decode("etl.JobStatus.QUEUED");
        } else if (job.status === "SUCCESS") {
            return this.localizeService.decode("etl.JobStatus.SUCCESS");
        } else if (job.status === "CANCELED") {
            return this.localizeService.decode("etl.JobStatus.CANCELED");
        } else if (job.status === "FAILURE") {
            return this.localizeService.decode("etl.JobStatus.FAILURE");
        } else {
            return this.localizeService.decode("etl.JobStatus.RUNNING");
        }
    }

    formatStepConfig(page: PageResult<any>): void {
        page.resultSet.forEach(job => {
            let stepConfig = {
                steps: [
                    { label: this.localizeService.decode("scheduler.step.fileImport"), status: "COMPLETE" },

                    {
                        label: this.localizeService.decode("scheduler.step.staging"),
                        status: job.stage === "NEW" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "NEW")
                    },

                    {
                        label: this.localizeService.decode("scheduler.step.validation"),
                        status: job.stage === "VALIDATE" || job.stage === "VALIDATION_RESOLVE" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "VALIDATE")
                    },

                    {
                        label: this.localizeService.decode("scheduler.step.databaseImport"),
                        status: job.stage === "IMPORT" || job.stage === "IMPORT_RESOLVE" || job.stage === "RESUME_IMPORT" ? this.getJobStatus(job) : ""
                    }
                ]
            };

            job = job as ScheduledJobOverview;
            job.stepConfig = stepConfig;
        });
    }

    getCompletedStatus(jobStage: string, targetStage: string): string {
        let order = ["NEW", "VALIDATE", "VALIDATION_RESOLVE", "IMPORT", "IMPORT_RESOLVE", "RESUME_IMPORT"];

        let jobPos = order.indexOf(jobStage);
        let targetPos = order.indexOf(targetStage);
        if (targetPos < jobPos) {
            return "COMPLETE";
        } else {
            return "";
        }
    }

    getJobStatus(job: ScheduledJob): string {
        if (job.status === "QUEUED" || job.status === "RUNNING") {
            return "WORKING";
        } else if (job.status === "FEEDBACK") {
            return "STUCK";
        }

        return "";
    }

    onViewAllCompleteJobs(): void {
        this.onCompleteJobsPageChange(1);

        this.isViewAllOpen = true;
    }

    onView(code: string): void {
        this.router.navigate(["/registry/job/", code]);
    }

    onActiveJobsPageChange(pageNumber: any): void {
        this.message = null;

        this.service.getScheduledJobs(this.activeJobsPage.pageSize, pageNumber, "createDate", false).then(response => {
            this.activeJobsPage = response;
            this.formatStepConfig(this.activeJobsPage);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCompleteJobsPageChange(pageNumber: any): void {
        this.message = null;

        this.service.getCompletedScheduledJobs(this.completeJobsPage.pageSize, pageNumber, "createDate", false).then(response => {
            this.completeJobsPage = response;
            this.formatStepConfig(this.completeJobsPage);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancelScheduledJob(historyId: string, job: ScheduledJob): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        this.bsModalRef.content.message = this.localizeService.decode("etl.import.cancel.modal.description");
        this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.cancel.modal.button");

        this.bsModalRef.content.type = ModalTypes.danger;

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.ioService.cancelImport(job.configuration).then(response => {
                this.bsModalRef.hide();

                for (let i = 0; i < this.activeJobsPage.resultSet.length; ++i) {
                    let activeJob = this.activeJobsPage.resultSet[i];

                    if (activeJob.jobId === job.jobId) {
                        this.activeJobsPage.resultSet.splice(i, 1);
                        break;
                    }
                }

                this.onViewAllCompleteJobs();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onResolveScheduledJob(historyId: string, job: ScheduledJob): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.importDescription");
        this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.importButton");

        this.bsModalRef.content.type = ModalTypes.danger;

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.resolveScheduledJob(historyId).then(response => {
                this.bsModalRef.hide();

                for (let i = 0; i < this.activeJobsPage.resultSet.length; ++i) {
                    let activeJob = this.activeJobsPage.resultSet[i];

                    if (activeJob.jobId === job.jobId) {
                        this.activeJobsPage.resultSet.splice(i, 1);
                        break;
                    }
                }

                this.onViewAllCompleteJobs();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
