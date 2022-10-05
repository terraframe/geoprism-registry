import { ChangeDetectorRef, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { ListData, ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { BsModalService } from "ngx-bootstrap/modal";
import { LazyLoadEvent } from "primeng/api";
import { ListTypeService } from "@registry/service/list-type.service";
import { AuthService } from "@shared/service";
import { HttpErrorResponse } from "@angular/common/http";
import { Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { ExportFormatModalComponent } from "../list-type/export-format-modal.component";
import { GeoRegistryConfiguration } from "@core/model/registry";
import { OverlayerIdentifier } from "@registry/model/constants";
import { NgxSpinnerService } from "ngx-spinner";

declare let registry: GeoRegistryConfiguration;

@Component({
    selector: "list-panel",
    templateUrl: "./list-panel.component.html",
    styleUrls: []
})
export class ListPanelComponent implements OnInit, OnDestroy {

    CONSTANTS = {
        LIST_MODAL: OverlayerIdentifier.LIST_MODAL
    };

    @Input() oid: string;

    @Output() error: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>();

    @Output() onRowSelect: EventEmitter<{ version: string, uid: string }> = new EventEmitter<{
        version: string,
        uid: string
    }>();

    @Output() close: EventEmitter<void> = new EventEmitter<void>();

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

    progressNotifier: WebSocketSubject<{ type: string, content: any }>;
    progressSubscription: Subscription = null;

    isRefreshing: boolean = false;
    progress: { current: number, total: number } = null;
    refresh: Subject<void>;

    // Verticle size of the panel
    size: number = 100;

    // eslint-disable-next-line no-useless-constructor
    constructor(private modalService: BsModalService,
        private service: ListTypeService,
        private spinner: NgxSpinnerService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef) {
        this.userOrgCodes = this.authService.getMyOrganizations();
    }

    ngOnInit(): void {
        this.refresh = new Subject<void>();

        this.tableState = null;

        if (localStorage.getItem(this.oid) != null) {
            const data: ListData = JSON.parse(localStorage.getItem(this.oid));

            this.tableState = data.event;
        }

        this.service.getVersion(this.oid).then(version => {
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
                pageSize: 30
            };
        });

        let baseUrl = WebSockets.buildBaseUrl();

        this.progressNotifier = webSocket(baseUrl + "/websocket/progress/" + this.oid);
        this.progressSubscription = this.progressNotifier.subscribe(message => {
            if (message.content != null) {
                this.handleProgressChange(message.content);
            } else {
                this.handleProgressChange(null);
            }
        });
    }

    ngOnDestroy(): void {
        this.refresh.unsubscribe();

        if (this.progressSubscription != null) {
            this.progressSubscription.unsubscribe();
        }

        if (this.progressNotifier != null) {
            this.progressNotifier.complete();
        }
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

        if (this.list.isMember || this.list.listMetadata.visibility === "PUBLIC") {
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
        }

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
                            this.error.emit(err);
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

    onClick(event: TableEvent): void {
        if (event.type === "view") {
            const result: any = event.row;

            this.onRowSelect.next({
                version: this.list.oid,
                uid: result.uid
            });
        }
    }

    onPublish(): void {
        if (!this.isRefreshing) {
            this.service.publishList(this.list.oid).toPromise().then((result: { jobOid: string }) => {
                this.isRefreshing = true;
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        }
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

    onToggleSize(): void {
        if (this.size === 50) {
            this.size = 100;
        } else {
            this.size = 50;
        }

        this.cdr.detectChanges();
    }

    percent(): number {
        if (this.progress != null) {
            return Math.floor(this.progress.current / this.progress.total * 100);
        }

        return 0;
    }

    onClose(): void {
        this.close.emit();
    }

}
