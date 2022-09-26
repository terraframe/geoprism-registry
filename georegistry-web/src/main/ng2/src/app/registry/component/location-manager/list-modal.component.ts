import { Component, OnDestroy, OnInit } from "@angular/core";
import { ListData, ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { BsModalRef } from "ngx-bootstrap/modal";
import { LazyLoadEvent } from "primeng/api";
import { ListTypeService } from "@registry/service/list-type.service";
import { AuthService, ProgressService } from "@shared/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";

@Component({
    selector: "list-modal",
    templateUrl: "./list-modal.component.html",
    styleUrls: []
})
export class ListModalComponent implements OnInit, OnDestroy {

    list: ListTypeVersion = null;
    current: string = "";
    isWritable: boolean = false;
    isRM: boolean = false;
    isSRA: boolean = false;

    orgCode: string;
    userOrgCodes: string[];

    config: GenericTableConfig = null;
    cols: GenericTableColumn[] = null;

    showInvalid = false;

    tableState: LazyLoadEvent = null;

    message: string = null;

    public onRowSelect: Subject<{
        version: string,
        uid: string
    }>;

    progressNotifier: WebSocketSubject<{ type: string, content: any }>;
    progressSubscription: Subscription = null;

    jobNotifier: WebSocketSubject<{ type: string, message: string }>;
    jobSubscription: Subscription = null;

    historyOid: string = null;

    isRefreshing: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef,
        private service: ListTypeService,
        private pService: ProgressService,
        private authService: AuthService) {
        this.userOrgCodes = this.authService.getMyOrganizations();
    }

    ngOnInit(): void {
        this.onRowSelect = new Subject();
    }

    ngOnDestroy(): void {
        this.onRowSelect.unsubscribe();

        if (this.progressSubscription != null) {
            this.progressSubscription.unsubscribe();
        }

        if (this.progressNotifier != null) {
            this.progressNotifier.complete();
        }

        if (this.jobSubscription != null) {
            this.jobSubscription.unsubscribe();
        }

        if (this.jobSubscription != null) {
            this.jobNotifier.complete();
        }
    }

    init(oid: string): void {
        this.tableState = null;

        if (localStorage.getItem(oid) != null) {
            const data: ListData = JSON.parse(localStorage.getItem(oid));

            this.tableState = data.event;
        }

        this.service.getVersion(oid).then(version => {
            this.list = version;
            this.orgCode = this.list.orgCode;
            const typeCode = this.list.superTypeCode != null ? this.list.superTypeCode : this.list.typeCode;

            this.isWritable = this.authService.isGeoObjectTypeRC(this.orgCode, typeCode);
            this.isRM = this.authService.isGeoObjectTypeRM(this.orgCode, typeCode);
            this.isSRA = this.authService.isSRA();

            this.refreshColumns();

            this.config = {
                service: this.service,
                remove: false,
                view: true,
                create: false,
                label: this.list.displayLabel,
                sort: [{ field: "code", order: 1 }],
                baseZIndex: 1051,
                pageSize: 10
            };
        });

        let baseUrl = WebSockets.buildBaseUrl();

        this.progressNotifier = webSocket(baseUrl + "/websocket/progress/" + oid);
        this.progressSubscription = this.progressNotifier.subscribe(message => {
            if (message.content != null) {
                this.handleProgressChange(message.content);
            } else {
                this.handleProgressChange(message);
            }
        });

        this.jobNotifier = webSocket(baseUrl + "/websocket/notify");
        this.jobSubscription = this.jobNotifier.subscribe(message => {
            this.handleJobChange();
        });
    }

    refreshColumns(): void {
        this.cols = [];

        if (this.list.isMember || this.list.geospatialMetadata.visibility === "PUBLIC") {
            this.cols.push({ header: "", type: "ACTIONS", sortable: false });
        }

        //
        // Order list columns
        // mdAttributes don't currently define the difference between hierarchy or custom attributes.
        // This ordering is a best attempt given these constraints.
        //
        let orderedArray = [];
        let code = this.list.attributes.filter(obj => {
            return obj.name === "code";
        });
        let label = this.list.attributes.filter(obj => {
            return obj.name.includes("displayLabel");
        });

        orderedArray.push(code[0], ...label);

        let customAttrs = [];
        let otherAttrs = [];
        this.list.attributes.forEach(attr => {
            if (attr.type === "input" && attr.name !== "latitude" && attr.name !== "longitude") {
                customAttrs.push(attr);
            } else if (attr.name !== "code" && !attr.name.includes("displayLabel") && attr.name !== "latitude" && attr.name !== "longitude") {
                otherAttrs.push(attr);
            }
        });

        orderedArray.push(...customAttrs, ...otherAttrs);

        let coords = this.list.attributes.filter(obj => {
            return obj.name === "latitude" || obj.name === "longitude";
        });

        if (coords.length === 2) {
            orderedArray.push(...coords);
        }

        orderedArray.forEach(attribute => {
            if (this.showInvalid || attribute.name !== "invalid") {
                let column: GenericTableColumn = {
                    header: attribute.label,
                    field: attribute.name,
                    type: "TEXT",
                    sortable: true,
                    filter: true
                };

                if (attribute.type === "date") {
                    column.type = "DATE";
                } else if (attribute.name === "invalid" || attribute.type === "boolean") {
                    column.type = "BOOLEAN";
                } else if (attribute.type === "number") {
                    column.type = "NUMBER";
                } else if (attribute.type === "list") {
                    column.type = "AUTOCOMPLETE";
                    column.text = "";
                    column.onComplete = () => {
                        this.service.values(this.list.oid, column.text, attribute.name, this.tableState.filters).then(options => {
                            column.results = options;
                        }).catch((err: HttpErrorResponse) => {
                            this.error(err);
                        });
                    };
                }

                this.cols.push(column);
            }
        });
    }

    onWheel(event: WheelEvent): void {
        let tableEl = (<Element>event.target).parentElement.closest("table").parentElement;

        tableEl.scrollLeft += event.deltaY;
        event.preventDefault();
    }

    onLoadEvent(event: LazyLoadEvent): void {
        this.tableState = event;

        const data: ListData = {
            event: event,
            oid: this.list.oid
        };

        localStorage.setItem(data.oid, JSON.stringify(data));
    }

    isListInOrg(): boolean {
        if (this.userOrgCodes && this.userOrgCodes.length > 0 && this.userOrgCodes.indexOf(this.orgCode) !== -1) {
            return true;
        }

        return false;
    }

    handleProgressChange(progress: any): void {
        this.isRefreshing = (progress.current < progress.total);

        this.pService.progress(progress);
    }

    handleJobChange(): void {
        if (this.historyOid != null) {
            this.service.getJob(this.historyOid).then(job => {
                if (job != null) {
                    if (job.status === "SUCCESS" || job.status === "FAILURE") {
                        this.handleProgressChange({ current: 1, total: 1 });

                        this.historyOid = null;
                    }

                    if (job.status === "FAILURE" && job.exception != null) {
                        this.message = job.exception.message;
                    }
                }
            });
        }
    }

    onClick(event: TableEvent): void {
        if (event.type === "view") {
            const result: any = event.row;

            this.onRowSelect.next({
                version: this.list.oid,
                uid: result.uid
            });

            this.bsModalRef.hide();
        }
    }

    onClose(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
