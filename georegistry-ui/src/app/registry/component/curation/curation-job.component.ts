import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";

import { HttpErrorResponse } from "@angular/common/http";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService } from "@shared/service";

import { GeoRegistryConfiguration } from "@core/model/core";
import { PageResult } from "@shared/model/core";
import { ListTypeService } from "@registry/service/list-type.service";
import { CurationJob, CurationProblem, ListTypeVersion } from "@registry/model/list-type";
import { Subscription } from "rxjs";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";

import { environment } from 'src/environments/environment';

@Component({
    selector: "curation-job",
    templateUrl: "./curation-job.component.html",
    styleUrls: []
})
export class CurationJobComponent implements OnInit, OnDestroy {

    message: string = null;

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
    subscription: Subscription = null;

    constructor(private router: Router, public service: ListTypeService, private modalService: BsModalService,
        private route: ActivatedRoute, private dateService: DateService,
        private localizeService: LocalizationService, authService: AuthService) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.params["oid"];
        this.service.getVersion(oid).then(version => {
            this.version = version;

            this.onPageChange(1);
        });

        let baseUrl = WebSockets.buildBaseUrl();

        this.notifier = webSocket(baseUrl + "/websocket/notify");
        this.subscription = this.notifier.subscribe(message => {
            if (message.type === "CURATION_JOB_CHANGE") {
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

    onProblemResolved(problem: CurationProblem): void {
        const index = this.page.resultSet.findIndex(p => p.id === problem.id);

        if (index !== -1) {
            this.page.resultSet.splice(index, 1);
        }
    }

    getFriendlyProblemType(probType: string): string {
        if (probType === "NO_GEOMETRY") {
            return this.localizeService.decode("list.type.no.geometry");
        } else if (probType === "INVALID_GEOMETRY") {
            return this.localizeService.decode("list.type.invalid.geometry");
        }

        return probType;
    }

    onEdit(problem: CurationProblem): void {
        // this.bsModalRef = this.modalService.show(CurationProblemModalComponent, {
        //     animated: true,
        //     backdrop: true,
        //     ignoreBackdropClick: true
        // });
        // this.bsModalRef.content.init(this.version, problem, this.job, (result: any) => {
        //     // if (result.action === "RESOLVED") {
        //     // this.onProblemResolved(result.data);
        //     // }
        // });

        const params: any = {
            layers: JSON.stringify([this.version.oid]),
            version: this.version.oid,
            uid: problem.goUid,
            pageContext: 'DATA'
        };

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
        });
    }

    toggleResolution(problem: CurationProblem): void {
        const resolution = (problem.resolution == null || problem.resolution.length === 0 || problem.resolution === "UNRESOLVED")
            ? "APPLY_GEO_OBJECT"
            : "UNRESOLVED";

        this.service.setResolution(problem, resolution).then(() => {
            problem.resolution = resolution;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onPageChange(pageNumber: any): void {
        if (this.version != null) {
            this.message = null;

            this.service.getCurationInfo(this.version, false, pageNumber, this.page.pageSize).then(response => {
                this.job = response;

                if (this.job.status === "SUCCESS") {
                    this.page = this.job.page;
                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
