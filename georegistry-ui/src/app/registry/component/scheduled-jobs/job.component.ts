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
import { Router, ActivatedRoute } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HttpErrorResponse } from "@angular/common/http";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { JobConflictModalComponent } from "./conflict-widgets/job-conflict-modal.component";
import { ReuploadModalComponent } from "./conflict-widgets/reupload-modal.component";
import { RegistryService, IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { ScheduledJob } from "@registry/model/registry";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";
import { ModalTypes } from "@shared/model/modal";

import { GeoRegistryConfiguration } from "@core/model/core";
import { PageResult } from "@shared/model/core";
import { Subscription } from "rxjs";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { environment } from 'src/environments/environment';
import { ShapefileModalComponent } from "../importer/modals/shapefile-modal.component";
import { ConfigurationModalComponent } from "./configuration-modal.component";

@Component({
    selector: "job",
    templateUrl: "./job.component.html",
    styleUrls: ["./scheduled-jobs.css"]
})
export class JobComponent implements OnInit, OnDestroy {

    message: string = null;
    job: ScheduledJob;
    allSelected: boolean = false;
    historyId: string = "";

    page: PageResult<any> = {
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
    subscription: Subscription = null;

    constructor(public service: RegistryService, private modalService: BsModalService,
        private router: Router, private route: ActivatedRoute, private dateService: DateService,
        private localizeService: LocalizationService, authService: AuthService, public ioService: IOService) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        this.historyId = this.route.snapshot.params["oid"];

        this.onPageChange(1);

        let baseUrl = WebSockets.buildBaseUrl();

        this.notifier = webSocket(baseUrl + "/websocket/notify");
        this.subscription = this.notifier.subscribe(message => {
            if (message.type === "IMPORT_JOB_CHANGE") {
                this.onPageChange(this.page.pageNumber);
            }
        });
    }

    ngOnDestroy() {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.notifier.complete();
    }

    formatAffectedRows(rows: string) {
        return rows.replace(/,/g, ", ");
    }

    formatValidationResolve(obj: any) {
        return JSON.stringify(obj);
    }

    onProblemResolved(problem: any): void {
        for (let i = 0; i < this.page.resultSet.length; ++i) {
            let pageConflict = this.page.resultSet[i];

            if (pageConflict.id === problem.id) {
                this.page.resultSet.splice(i, 1);
            }
        }
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "net.geoprism.registry.io.ParentCodeException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.parent.lookup");
        }

        if (probType === "net.geoprism.registry.io.PostalCodeLocationException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.postal.code.lookup");
        }

        if (probType === "net.geoprism.registry.io.AmbiguousParentException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.multi.parent.lookup");
        }

        if (probType === "net.geoprism.registry.io.InvalidGeometryException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.invalid.geom.lookup");
        }

        if (probType === "net.geoprism.registry.DataNotFoundException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.datanotfound");
        }

        if (probType === "net.geoprism.registry.geoobject.ImportOutOfRangeException") {
            return this.localizeService.decode("scheduledjobs.job.problem.type.importOutOfRange");
        }

        if (
            probType === "net.geoprism.registry.roles.CreateGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.WriteGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.DeleteGeoObjectPermissionException" ||
            probType === "net.geoprism.registry.roles.ReadGeoObjectPermissionException"
        ) {
            return this.localizeService.decode("scheduledjobs.job.problem.type.permission");
        }

        // if(probType === "net.geoprism.registry.io.TermValueException"){
        //   return this.localizeService.decode( "scheduledjobs.job.problem.type.postal.code.lookup" );
        // }

        if (
            probType === "com.runwaysdk.dataaccess.DuplicateDataException" ||
            probType === "net.geoprism.registry.DuplicateGeoObjectException" ||
            probType === "net.geoprism.registry.DuplicateGeoObjectCodeException"
        ) {
            return this.localizeService.decode("scheduledjobs.job.problem.type.duplicate.data.lookup");
        }

        return probType;
    }

    onEdit(problem: any): void {
        // this.router.navigate( ['/registry/master-list-history/', code] )

        this.bsModalRef = this.modalService.show(JobConflictModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.problem = problem;
        this.bsModalRef.content.job = this.job;
        this.bsModalRef.content.onConflictAction.subscribe(data => {
            if (data.action === "RESOLVED") {
                this.onProblemResolved(data.data);
            }
        });
    }

    onPageChange(pageNumber: any): void {
        this.message = null;

        this.service.getScheduledJob(this.historyId, this.page.pageSize, pageNumber, true).then(response => {
            this.job = response;

            if (this.job.stage === "IMPORT_RESOLVE") {
                this.page = this.job.importErrors;
            } else if (this.job.stage === "VALIDATION_RESOLVE") {
                this.page = this.job.problems;

                for (let i = 0; i < this.page.resultSet.length; ++i) {
                    let problem = this.page.resultSet[i];

                    if (problem.type === "RowValidationProblem") {
                        this.hasRowValidationProblem = true;
                    }
                }
            }

            if (response.exception) {
                this.error(response.exception);
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onViewAllActiveJobs(): void {

    }

    onViewAllCompleteJobs(): void {

    }

    toggleAll(): void {
        this.allSelected = !this.allSelected;

        this.job.importErrors.resultSet.forEach(row => {
            row.selected = this.allSelected;
        });
    }

    onReuploadAndResume(historyId: string): void {
        this.bsModalRef = this.modalService.show(ReuploadModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        this.bsModalRef.content.job = this.job;

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.router.navigate(["/registry/scheduled-jobs"]);
        });
    }

    onResolveScheduledJob(historyId: string): void {
        if (this.page.resultSet.length === 0) {
            this.service.resolveScheduledJob(historyId).then(response => {
                this.router.navigate(["/registry/scheduled-jobs"]);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true
            });

            if (this.job.stage === "VALIDATION_RESOLVE") {
                this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.validationDescription");
                this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.validationButton");
            } else {
                this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.importDescription");
                this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.importButton");
            }

            this.bsModalRef.content.type = ModalTypes.danger;

            this.bsModalRef.content.onConfirm.subscribe(data => {
                this.service.resolveScheduledJob(historyId).then(response => {
                    this.router.navigate(["/registry/scheduled-jobs"]);
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            });
        }
    }

    onCancelScheduledJob(historyId: string): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });

        this.bsModalRef.content.message = this.localizeService.decode("etl.import.cancel.modal.description");
        this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.cancel.modal.button");

        this.bsModalRef.content.type = ModalTypes.danger;

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.ioService.cancelImport(this.job.configuration).then(response => {
                // this.bsModalRef.hide()
                this.router.navigate(["/registry/scheduled-jobs"]);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onConfiguration(): void {
        console.log(this.job.configuration);

        this.bsModalRef = this.modalService.show(ConfigurationModalComponent, { backdrop: true, ignoreBackdropClick: true });
        this.bsModalRef.content.init(this.job.configuration);
    }

    error(err: any): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }
}
