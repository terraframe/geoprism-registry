import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { ListColumn, ListData, ListTypeVersion } from "@registry/model/list-type";
import { GenericTableColumn, GenericTableConfig, TableColumnSetup, TableEvent } from "@shared/model/generic-table";
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
export class ListPanelComponent implements OnInit, OnDestroy, OnChanges {

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
    setup: TableColumnSetup = null;

    showInvalid = false;

    tableState: LazyLoadEvent = null;

    progressNotifier: WebSocketSubject<{ type: string, content: any }>;
    progressSubscription: Subscription = null;

    isRefreshing: boolean = false;
    progress: { current: number, total: number } = null;
    refresh: Subject<void>;

    // Verticle size of the panel
    size: number = 50;

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

    ngOnChanges(changes: SimpleChanges): void {
        if (changes["oid"] != null) {
            this.list = null;

            this.ngOnDestroy();

            this.ngOnInit();
        }
    }

    refreshColumns(): void {
        const columns = [];

        const mainGroups: GenericTableColumn[] = [];
        const subGroups: GenericTableColumn[] = [];
        const orderedArray = [];

        if (this.list.isMember || this.list.geospatialMetadata.visibility === "PUBLIC") {
            const column = { header: "", type: "ACTIONS", sortable: false, rowspan: 3, colspan: 1, headerType: "ATTRIBUTE" };

            mainGroups.push(column);
            columns.push(column);
        }

        this.list.attributes.forEach(group => {
            if (this.showInvalid || group.name !== "invalid") {
                mainGroups.push({
                    header: group.label,
                    colspan: group.colspan,
                    rowspan: group.rowspan,
                    headerType: "GROUP"
                });

                group.columns.forEach(subgroup => {
                    if (subgroup.columns != null) {
                        subGroups.push({
                            header: subgroup.label,
                            colspan: subgroup.colspan,
                            rowspan: subgroup.rowspan,
                            headerType: "GROUP"
                        });

                        subgroup.columns.forEach(attribute => {
                            if (this.showInvalid || attribute.name !== "invalid") {
                                const column = this.createColumn(attribute);

                                orderedArray.push(column);
                                columns.push(column);
                            }
                        });
                    } else {
                        if (this.showInvalid || subgroup.name !== "invalid") {
                            const column = this.createColumn(subgroup);

                            subGroups.push(column);
                            columns.push(column);
                        }
                    }
                });
            }
        });

        this.setup = {
            headers: [mainGroups, subGroups, orderedArray],
            columns: columns
        };

        console.log(this.setup);
    }

    createColumn(attribute: ListColumn): GenericTableColumn {
        let column: GenericTableColumn = {
            headerType: "ATTRIBUTE",
            header: attribute.label,
            field: attribute.name,
            type: "TEXT",
            sortable: true,
            filter: true,
            rowspan: attribute.rowspan,
            colspan: attribute.colspan
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

        return column;
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
