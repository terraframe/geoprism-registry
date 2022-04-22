import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { LazyLoadEvent, FilterMetadata } from "primeng/api";

import { Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";
import * as ColorGen from "color-generator";

import { ErrorHandler } from "@shared/component";
import { AuthService, ProgressService } from "@shared/service";
import { ContextLayer, ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ExportFormatModalComponent } from "./export-format-modal.component";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";
import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
import { LngLatBounds } from "mapbox-gl";

import { GeoRegistryConfiguration } from "@core/model/registry";
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
        private authService: AuthService) {
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

        this.service.getVersion(oid).then(version => {
            this.list = version;
            const orgCode = this.list.orgCode;
            const typeCode = this.list.superTypeCode != null ? this.list.superTypeCode : this.list.typeCode;

            this.isWritable = this.authService.isGeoObjectTypeRC(orgCode, typeCode);
            this.isRM = this.authService.isGeoObjectTypeRM(orgCode, typeCode);

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
            layers: JSON.stringify([this.layerFromList(this.list)]),
            type: type,
            code: "__NEW__"
        };

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
        this.bsModalRef.content.onFormat.subscribe(format => {
            if (format === "SHAPEFILE") {
                window.location.href = registry.contextPath + "/list-type/export-shapefile?oid=" + this.list.oid + "&criteria=" + encodeURIComponent(JSON.stringify(criteria));
            } else if (format === "EXCEL") {
                window.location.href = registry.contextPath + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&criteria=" + encodeURIComponent(JSON.stringify(criteria));
            }
        });
    }

    onWheel(event: WheelEvent): void {
        let tableEl = (<Element>event.target).parentElement.closest("table").parentElement;

        tableEl.scrollLeft += event.deltaY;
        event.preventDefault();
    }

    layerFromList(version: ListTypeVersion): ContextLayer {
        let layer: ContextLayer = new ContextLayer();
        layer.oid = version.oid;
        layer.color = ColorGen().hexString();
        layer.label = version.displayLabel;
        layer.rendered = true;
        layer.forDate = version.forDate;
        layer.versionNumber = version.versionNumber;
        return layer;
    }

    onGotoMap(result: any): void {
        const params: any = { layers: JSON.stringify([this.layerFromList(this.list)]) };

        if (result != null) {
            params.version = this.list.oid;
            params.uid = result.uid;
            params.pageContext = "DATA";

            this.router.navigate(["/registry/location-manager"], {
                queryParams: params
            });
        } else {
            this.service.getBounds(this.list.oid).then(bounds => {
                if (bounds && Array.isArray(bounds)) {
                    let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                    const array = llb.toArray();

                    params.bounds = JSON.stringify(array);
                }

                this.router.navigate(["/registry/location-manager"], {
                    queryParams: params
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

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
