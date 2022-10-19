import { Component, OnDestroy, OnInit } from "@angular/core";
import { ListData, ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, GenericTableGroup, TableEvent } from "@shared/model/generic-table";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { LazyLoadEvent } from "primeng/api";
import { ListTypeService } from "@registry/service/list-type.service";
import { AuthService } from "@shared/service";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { ExportFormatModalComponent } from "../list-type/export-format-modal.component";
import { GeoRegistryConfiguration } from "@core/model/registry";
import { OverlayerIdentifier } from "@registry/model/constants";
import { NgxSpinnerService } from "ngx-spinner";

declare let registry: GeoRegistryConfiguration;

@Component({
    selector: "list-modal",
    templateUrl: "./list-modal.component.html",
    styleUrls: []
})
export class ListModalComponent implements OnInit, OnDestroy {

    CONSTANTS = {
        LIST_MODAL: OverlayerIdentifier.LIST_MODAL
    };

    list: ListTypeVersion = null;
    current: string = "";
    isWritable: boolean = false;
    isRM: boolean = false;
    isSRA: boolean = false;

    orgCode: string;
    userOrgCodes: string[];

    config: GenericTableConfig = null;
    groups: GenericTableGroup[][] = null;
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
    progress: { current: number, total: number } = null;
    refresh: Subject<void>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef,
        private modalService: BsModalService,
        private service: ListTypeService,
        private spinner: NgxSpinnerService,
        private authService: AuthService) {
        this.userOrgCodes = this.authService.getMyOrganizations();
    }

    ngOnInit(): void {
        this.onRowSelect = new Subject();
        this.refresh = new Subject<void>();
    }

    ngOnDestroy(): void {
        this.onRowSelect.unsubscribe();
        this.refresh.unsubscribe();

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
                this.handleProgressChange(null);
            }
        });

        this.jobNotifier = webSocket(baseUrl + "/websocket/notify");
        this.jobSubscription = this.jobNotifier.subscribe(message => {
            this.handleJobChange();
        });
    }

    refreshColumns(): void {
        this.cols = [];
        const orderedArray = [];

        const mainGroups: GenericTableGroup[] = [];
        const subGroups: GenericTableGroup[] = [];

        if (this.list.isMember || this.list.geospatialMetadata.visibility === "PUBLIC") {
            this.cols.push({ header: "", type: "ACTIONS", sortable: false });

            mainGroups.push({ label: "", colspan: 1 });
            subGroups.push({ label: "", colspan: 1 });
        }

        this.list.attributes.forEach(group => {
            if (this.showInvalid || group.name !== "invalid") {
                mainGroups.push({
                    label: group.label,
                    colspan: group.colspan
                });

                group.columns.forEach(subgroup => {
                    subGroups.push({
                        label: subgroup.label,
                        colspan: subgroup.colspan
                    });

                    subgroup.columns.forEach(attribute => {
                        orderedArray.push(attribute);
                    });
                });
            }
        });

        this.groups = [mainGroups, subGroups];

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

    handleShowInvalidChange(): void {
        this.refreshColumns();
        this.refresh.next();
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

    handleProgressChange(progress: { current: number, total: number }): void {
        if (progress != null) {
            this.isRefreshing = (progress.current < progress.total);
            this.progress = progress;

            if (this.isRefreshing) {
                this.spinner.show(this.CONSTANTS.LIST_MODAL);
            } else {
                this.spinner.hide(this.CONSTANTS.LIST_MODAL);
            }
        } else {
            this.isRefreshing = false;
            this.progress = null;
        }

        if (!this.isRefreshing && this.refresh != null) {
            this.refresh.next();
        }
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

    onPublish(): void {
        this.message = null;

        this.service.publishList(this.list.oid).toPromise().then((result: { jobOid: string }) => {
            this.isRefreshing = true;
            this.list.curation = {};
            this.historyOid = result.jobOid;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onNewGeoObject(type: string = null): void {
    }

    onExport(): void {
        const criteria = {
            filters: this.tableState.filters != null ? { ...this.tableState.filters } : {}
        };

        if (!this.showInvalid) {
            criteria.filters["invalid"] = { value: false, matchMode: "equals" };
        }

        const modal = this.modalService.show(ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        modal.content.init(this.list);
        modal.content.onFormat.subscribe(data => {
            if (data.format === "SHAPEFILE") {
                let url = registry.contextPath + "/list-type/export-shapefile?oid=" + this.list.oid;
                url += "&criteria=" + encodeURIComponent(JSON.stringify(criteria));

                if (data.actualGeometryType != null && data.actualGeometryType.length > 0) {
                    url += "&actualGeometryType=" + encodeURIComponent(data.actualGeometryType);
                }

                window.open(url, "_blank");
            } else if (data.format === "EXCEL") {
                window.open(registry.contextPath + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&criteria=" + encodeURIComponent(JSON.stringify(criteria)), "_blank");
            }
        });
    }

    percent(): number {
        if (this.progress != null) {
            return Math.floor(this.progress.current / this.progress.total * 100);
        }

        return 0;
    }

    onClose(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
