import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { LazyLoadEvent, FilterMetadata } from "primeng/api";

import { Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";

import { ErrorHandler } from "@shared/component";
import { AuthService, ProgressService } from "@shared/service";
import { ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ExportFormatModalComponent } from "./export-format-modal.component";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { LngLatBounds } from "mapbox-gl";

import { GeoRegistryConfiguration } from "@core/model/registry";
import { GeometryService } from "@registry/service/geometry.service";
import { LocationManagerStateService } from "@registry/service/location-manager.service";
import { RegistryCacheService } from "@registry/service/registry-cache.service";
import { LocationManagerState } from "../location-manager/location-manager.component";
declare let registry: GeoRegistryConfiguration;

@Component({
    selector: "list",
    templateUrl: "./list.component.html",
    styleUrls: ["./list.component.css"]
})
export class ListComponent implements OnInit, OnDestroy {

    message: string = null;

    list: ListTypeVersion = null;
    current: string = "";
    isRefreshing: boolean = false;
    isWritable: boolean = false;
    isRM: boolean = false;
    isSRA: boolean = false;

    orgCode: string;
    userOrgCodes: string[];

    config: GenericTableConfig = null;
    cols: GenericTableColumn[] = null;
    refresh: Subject<void>;

    filters: { [s: string]: FilterMetadata; } = null;

    showInvalid = false;

    historyOid: string = null;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    progressNotifier: WebSocketSubject<{ type: string, content: any }>;
    progressSubscription: Subscription = null;

    jobNotifier: WebSocketSubject<{ type: string, message: string }>;
    jobSubscription: Subscription = null;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private modalService: BsModalService,
        private service: ListTypeService,
        private pService: ProgressService,
        private geomService: GeometryService,
        private locationManagerService: LocationManagerStateService,
        private cacheService: RegistryCacheService,
        private authService: AuthService) {
        this.userOrgCodes = this.authService.getMyOrganizations();
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

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
                sort: { field: "code", order: 1 }
            };

            if (version.refreshProgress != null) {
                this.handleProgressChange(version.refreshProgress);
            }
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

        this.refresh = new Subject<void>();
    }

    ngOnDestroy() {
        if (this.refresh != null) {
            this.refresh.unsubscribe();
        }

        if (this.progressSubscription != null) {
            this.progressSubscription.unsubscribe();
        }

        this.progressNotifier.complete();

        if (this.jobSubscription != null) {
            this.jobSubscription.unsubscribe();
        }

        this.jobNotifier.complete();
    }

    ngAfterViewInit() {

    }

    onClick(event: TableEvent): void {
        if (event.type === "view") {
            this.onGotoMap(event.row);
        }
        //  else if (event.type === "remove") {
        //     this.onRemove(event.row as Sensor);
        // } else if (event.type === "create") {
        //     this.newInstance();
        // }
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
                        this.service.values(this.list.oid, column.text, attribute.name, this.filters).then(options => {
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

    handleProgressChange(progress: any): void {
        this.isRefreshing = (progress.current < progress.total);

        this.pService.progress(progress);

        if (!this.isRefreshing && this.refresh != null) {
            // Refresh the resultSet
            // this.onPageChange(1);
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

    onEdit(data): void {
        let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
        editModal.content.configureAsExisting(data.code, this.list.typeCode, this.list.forDate, this.list.isGeometryEditable);
        editModal.content.setListId(this.list.oid);
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            // this.onPageChange(this.page.pageNumber);
            this.refresh.next();
        });
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
        if (!type) {
            type = this.list.typeCode;
        }

        const params: any = {
            type: type,
            code: "__NEW__"
        };

        this.locationManagerService.addLayerForList(this.list, params);

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
        });
    }

    onExport(): void {
        const criteria = {
            filters: this.filters != null ? { ...this.filters } : {}
        };

        if (!this.showInvalid) {
            criteria.filters["invalid"] = { value: false, matchMode: "equals" };
        }

        this.bsModalRef = this.modalService.show(ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.list);
        this.bsModalRef.content.onFormat.subscribe(data => {
            if (data.format === "SHAPEFILE") {
                let url = registry.contextPath + "/list-type/export-shapefile?oid=" + this.list.oid;
                url += "&criteria=" + encodeURIComponent(JSON.stringify(criteria));

                if (data.actualGeometryType != null && data.actualGeometryType.length > 0) {
                    url += "&actualGeometryType=" + encodeURIComponent(data.actualGeometryType);
                }

                window.location.href = url;
            } else if (data.format === "EXCEL") {
                window.location.href = registry.contextPath + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&criteria=" + encodeURIComponent(JSON.stringify(criteria));
            }
        });
    }

    onWheel(event: WheelEvent): void {
        let tableEl = (<Element>event.target).parentElement.closest("table").parentElement;

        tableEl.scrollLeft += event.deltaY;
        event.preventDefault();
    }

    onGotoMap(result: any): void {
        let state: LocationManagerState = { pageContext: "DATA" };

        this.locationManagerService.addLayerForList(this.list, state);

        if (result == null) {
            this.service.getBounds(this.list.oid).then(bounds => {
                if (bounds && Array.isArray(bounds)) {
                    let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                    const array = llb.toArray();

                    state.bounds = JSON.stringify(array);
                }

                this.router.navigate(["/registry/location-manager"], {
                    queryParams: state
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.service.record(this.list.oid, result.uid, false).then(record => {
                this.cacheService.getTypeCache().waitOnTypes().then(() => {
                    this.locationManagerService.selectListRecord(this.list.oid, result.uid, record, state);

                    if (record.recordType === "GEO_OBJECT") {
                        this.router.navigate(["/registry/location-manager"], {
                            queryParams: state
                        });
                    } else {
                        this.service.getBounds(this.list.oid).then(bounds => {
                            if (bounds && Array.isArray(bounds)) {
                                let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                                const array = llb.toArray();

                                state.bounds = JSON.stringify(array);
                            }

                            this.router.navigate(["/registry/location-manager"], {
                                queryParams: state
                            });
                        }).catch((err: HttpErrorResponse) => {
                            this.error(err);
                        });
                    }
                });
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    onRunCuration(): void {
        this.service.createCurationJob(this.list).then(job => {
            this.router.navigate(["/registry/curation-job", this.list.oid]);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onFilter(event: LazyLoadEvent): void {
        this.filters = null;

        if (event.filters != null) {
            this.filters = event.filters;
        }
    }

    isListInOrg(): boolean {
        if (this.userOrgCodes && this.userOrgCodes.length > 0 && this.userOrgCodes.indexOf(this.orgCode) !== -1) {
            return true;
        }

        return false;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
