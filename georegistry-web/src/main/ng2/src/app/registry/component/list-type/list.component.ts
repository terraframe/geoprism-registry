import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { LazyLoadEvent, FilterMetadata } from "primeng/api";

import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Subject, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { DateService } from "@shared/service/date.service";
import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService, ProgressService } from "@shared/service";
import { ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ExportFormatModalComponent } from "./export-format-modal.component";

import { GeoRegistryConfiguration } from "@core/model/registry"; import { GenericTableColumn, GenericTableConfig, TableEvent } from "@shared/model/generic-table";
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

    filters: any[] = [];

    showInvalid = false;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    public searchPlaceholder = "";

    notifier: WebSocketSubject<{ type: string, content: any }>;
    subscription: Subscription = null;

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private service: ListTypeService,
        private pService: ProgressService,
        private dateService: DateService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private authService: AuthService) {
        this.searchPlaceholder = localizeService.decode("masterlist.search");
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

        let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + registry.contextPath;

        this.notifier = webSocket(baseUrl + "/websocket/progress/" + oid);
        this.subscription = this.notifier.subscribe(message => {
            if (message.content != null) {
                this.handleProgressChange(message.content);
            } else {
                this.handleProgressChange(message);
            }
        });
    }

    ngOnDestroy() {
        if (this.refresh != null) {
            this.refresh.unsubscribe();
        }

        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.notifier.complete();
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

        this.list.attributes.forEach(attribute => {
            if (this.showInvalid || attribute.name !== "invalid") {
                let type = "TEXT";
                let sortable = true;

                if (attribute.type === "date") {
                    type = "DATE";
                } else if (attribute.name === "invalid") {
                    type = "BOOLEAN";
                }

                this.cols.push({ header: attribute.label, field: attribute.name, type: type, sortable: sortable, filter: sortable });
            }
        });
    }

    // setList(_list: ListTypeVersion): void {
    //     this.list = _list;
    //     this.listAttrs = this.calculateListAttributes();
    // }

    // getTypeaheadDataObservable(attribute: any): void {
    //     return Observable.create((observer: any) => {
    //         this.message = null;

    //         // Get the valid values
    //         this.service.values(this.list.oid, attribute.search, attribute.name, attribute.base, this.getFilter()).then(options => {
    //             if (this.filter.findIndex(filt => filt.value === "null") === -1) {
    //                 options.unshift({ label: "[" + this.localizeService.decode("list.emptyValue") + "]", value: "null" });
    //             }
    //             options.unshift({ label: "[" + this.localizeService.decode("masterlist.nofilter") + "]", value: null });

    //             observer.next(options);
    //         }).catch((err: HttpErrorResponse) => {
    //             this.error(err);
    //         });
    //     });
    // }

    handleProgressChange(progress: any): void {
        this.isRefreshing = (progress.current < progress.total);

        this.pService.progress(progress);

        if (!this.isRefreshing && this.refresh != null) {
            // Refresh the resultSet
            // this.onPageChange(1);
            this.refresh.next();
        }
    }

    // handleDateChange(attribute: any): void {
    //     attribute.isCollapsed = true;

    //     // Remove the current attribute filter if it exists
    //     this.filter = this.filter.filter(f => f.attribute !== attribute.base);
    //     this.selected = this.selected.filter(s => s !== attribute.base);

    //     if (attribute.value != null && ((attribute.value.start != null && attribute.value.start !== "") || (attribute.value.end != null && attribute.value.end !== ""))) {
    //         let label = "[" + attribute.label + "] : [";

    //         if (attribute.value.start != null) {
    //             label += attribute.value.start;
    //         }

    //         if (attribute.value.start != null && attribute.value.end != null) {
    //             label += " - ";
    //         }

    //         if (attribute.value.end != null) {
    //             label += attribute.value.end;
    //         }

    //         label += "]";

    //         this.filter.push({ attribute: attribute.base, value: attribute.value, label: label });
    //         this.selected.push(attribute.base);
    //     }

    //     this.onPageChange(1);
    // }

    // handleInputChange(attribute: any): void {
    //     attribute.isCollapsed = true;

    //     // Remove the current attribute filter if it exists
    //     this.filter = this.filter.filter(f => f.attribute !== attribute.base);
    //     this.selected = this.selected.filter(s => s !== attribute.base);

    //     if (attribute.value != null && attribute.value !== "") {
    //         const label = "[" + attribute.label + "] : " + "[" + attribute.value + "]";

    //         this.filter.push({ attribute: attribute.base, value: attribute.value === "null" ? null : attribute.value, label: label });
    //         this.selected.push(attribute.base);
    //     }

    //     this.onPageChange(1);
    // }

    // handleListChange(e: TypeaheadMatch, attribute: any): void {
    //     attribute.value = e.item;
    //     attribute.isCollapsed = true;

    //     this.selected = this.selected.filter(s => s !== attribute.base);

    //     this.list.attributes.forEach(attr => {
    //         if (attr.base === attribute.base) {
    //             attr.search = "";
    //         }
    //     });

    //     if (attribute.value.value != null && attribute.value.value !== "") {
    //         const label = "[" + attribute.label + "] : " + "[" + attribute.value.label + "]";

    //         this.filter.push({ attribute: attribute.base, value: e.item.value, label: label });
    //         this.selected.push(attribute.base);
    //         attribute.search = e.item.label;
    //     } else {
    //         this.filter = this.filter.filter(f => f.attribute !== attribute.base);
    //         attribute.search = "";
    //     }

    //     this.onPageChange(1);
    // }

    // isFilterable(attribute: any): boolean {
    //     return attribute.type !== "none" && attribute.name !== "invalid" && (attribute.dependency.length === 0 || this.selected.indexOf(attribute.base) !== -1 || this.selected.filter(value => attribute.dependency.includes(value)).length > 0);
    // }

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

        this.service.publishList(this.list.oid).toPromise().then((historyOid: string) => {
            this.isRefreshing = true;
            this.list.curation = {};
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onNewGeoObject(): void {
        const params: any = {
            layers: JSON.stringify([this.list.oid]),
            type: this.list.typeCode,
            code: "__NEW__"
        };

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
        });
    }

    onExport(): void {
        const filters = [...this.filters];

        if (!this.showInvalid) {
            filters.push({ attribute: "invalid", value: "false" });
        }

        this.bsModalRef = this.modalService.show(ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.onFormat.subscribe(format => {
            if (format === "SHAPEFILE") {
                window.location.href = registry.contextPath + "/list-type/export-shapefile?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(filters));
            } else if (format === "EXCEL") {
                window.location.href = registry.contextPath + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(filters));
            }
        });
    }

    changeTypeaheadLoading(attribute: any, loading: boolean): void {
        attribute.loading = loading;
    }

    onWheel(event: WheelEvent): void {
        let tableEl = (<Element>event.target).parentElement.closest("table").parentElement;

        tableEl.scrollLeft += event.deltaY;
        event.preventDefault();
    }

    onGotoMap(result: any): void {
        const params: any = { layers: JSON.stringify([this.list.oid]) };

        if (result != null) {
            params.version = this.list.oid;
            params.uid = result.uid;
            params.pageContext = "DATA";
        }

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
        });
    }

    onRunCuration(): void {
        this.service.createCurationJob(this.list).then(job => {
            this.router.navigate(["/registry/curation-job", this.list.oid]);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onFilter(event: LazyLoadEvent): void {
        this.filters = [];

        if (event.filters != null) {
            this.filters = Object.keys(event.filters).map(key => {
                const filter: FilterMetadata = event.filters[key];
                const index = this.list.attributes.findIndex(attr => attr.name === key);
                const attribute = this.list.attributes[index];
                const label = "[" + attribute.label + "] : " + "[" + filter.value + "]";

                return { attribute: attribute.base, value: filter.value, label: label };
            });
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
