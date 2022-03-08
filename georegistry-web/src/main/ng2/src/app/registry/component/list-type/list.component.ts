import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { DateService } from "@shared/service/date.service";
import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService, ProgressService } from "@shared/service";
import { ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ExportFormatModalComponent } from "./export-format-modal.component";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";

import { GeoRegistryConfiguration } from "@core/model/registry"; import { LngLatBounds } from "mapbox-gl";
declare let registry: GeoRegistryConfiguration;

@Component({
    selector: "list",
    templateUrl: "./list.component.html",
    styleUrls: ["./list.component.css"]
})
export class ListComponent implements OnInit, OnDestroy {

    message: string = null;
    list: ListTypeVersion = null;
    p: number = 1;
    current: string = "";
    filter: { attribute: string, value: string, label: string }[] = [];
    selected: string[] = [];
    page: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 100,
        results: []
    };

    sort = { attribute: "code", order: "ASC" };
    isRefreshing: boolean = false;
    isWritable: boolean = false;
    isRM: boolean = false;

    listAttrs: any[];

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
            this.setList(version);
            const orgCode = this.list.orgCode;
            const typeCode = this.list.superTypeCode != null ? this.list.superTypeCode : this.list.typeCode;

            this.isWritable = this.authService.isGeoObjectTypeRC(orgCode, typeCode);
            this.isRM = this.authService.isGeoObjectTypeRM(orgCode, typeCode);

            this.onPageChange(1);

            if (version.refreshProgress != null) {
                this.handleProgressChange(version.refreshProgress);
            }
        });

        let baseUrl = WebSockets.buildBaseUrl();

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
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.notifier.complete();
    }

    ngAfterViewInit() {

    }

    setList(_list: ListTypeVersion): void {
        this.list = _list;
        this.listAttrs = this.calculateListAttributes();
    }

    onShowInvalidChange() {
        this.onPageChange(1);
    }

    onPageChange(pageNumber: number): void {
        this.message = null;

        this.service.data(this.list.oid, pageNumber, this.page.pageSize, this.getFilter(), this.sort).then(page => {
            this.page = page;
            this.listAttrs = this.calculateListAttributes();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onSort(attribute: { name: string, label: string }): void {
        if (this.sort.attribute === attribute.name) {
            this.sort.order = (this.sort.order === "ASC" ? "DESC" : "ASC");
        } else {
            this.sort = { attribute: attribute.name, order: "ASC" };
        }

        this.onPageChange(1);
    }

    clearFilters(): void {
        this.list.attributes.forEach(attr => {
            attr.search = null;
        });

        this.filter = [];
        this.showInvalid = false;
        this.selected = [];

        this.onPageChange(1);
    }

    toggleFilter(attribute: any): void {
        attribute.isCollapsed = !attribute.isCollapsed;
    }

    getFilter(): { attribute: string, value: string, label: string }[] {
        let newFilter = JSON.parse(JSON.stringify(this.filter));

        if (!this.showInvalid) {
            newFilter.push({ attribute: "invalid", value: "false" });
        }

        return newFilter;
    }

    calculateListAttributes() {
        let attrs: any[];

        if (this.showInvalid) {
            attrs = this.list.attributes;
        } else {
            attrs = JSON.parse(JSON.stringify(this.list.attributes));

            let index = attrs.findIndex(attr => attr.name === "invalid");

            if (index !== -1) {
                attrs.splice(index, 1);
            }
        }

        attrs.forEach(attribute => {
            attribute.isCollapsed = true;
        });

        // Order list columns
        // mdAttributes don't currently define the difference between hierarchy or custom attributes.
        // This ordering is a best attempt given these constraints.
        //
        let orderedArray = [];
        let code = attrs.filter(obj => {
            return obj.name === "code";
        });
        let label = attrs.filter(obj => {
            return obj.name.includes("displayLabel");
        });

        orderedArray.push(code[0], ...label);

        let customAttrs = [];
        let otherAttrs = [];
        attrs.forEach(attr => {
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

        return orderedArray;
    }

    getTypeaheadDataObservable(attribute: any): void {
        return Observable.create((observer: any) => {
            this.message = null;

            // Get the valid values
            this.service.values(this.list.oid, attribute.search, attribute.name, attribute.base, this.getFilter()).then(options => {
                if (this.filter.findIndex(filt => filt.value === "null") === -1) {
                    options.unshift({ label: "[" + this.localizeService.decode("list.emptyValue") + "]", value: "null" });
                }
                options.unshift({ label: "[" + this.localizeService.decode("masterlist.nofilter") + "]", value: null });

                observer.next(options);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    handleProgressChange(progress: any): void {
        this.isRefreshing = (progress.current < progress.total);

        this.pService.progress(progress);

        if (!this.isRefreshing) {
            // Refresh the resultSet
            this.onPageChange(1);
        }
    }

    handleDateChange(attribute: any): void {
        attribute.isCollapsed = true;

        // Remove the current attribute filter if it exists
        this.filter = this.filter.filter(f => f.attribute !== attribute.base);
        this.selected = this.selected.filter(s => s !== attribute.base);

        if (attribute.value != null && ((attribute.value.start != null && attribute.value.start !== "") || (attribute.value.end != null && attribute.value.end !== ""))) {
            let label = "[" + attribute.label + "] : [";

            if (attribute.value.start != null) {
                label += attribute.value.start;
            }

            if (attribute.value.start != null && attribute.value.end != null) {
                label += " - ";
            }

            if (attribute.value.end != null) {
                label += attribute.value.end;
            }

            label += "]";

            this.filter.push({ attribute: attribute.base, value: attribute.value, label: label });
            this.selected.push(attribute.base);
        }

        this.onPageChange(1);
    }

    handleInputChange(attribute: any): void {
        attribute.isCollapsed = true;

        // Remove the current attribute filter if it exists
        this.filter = this.filter.filter(f => f.attribute !== attribute.base);
        this.selected = this.selected.filter(s => s !== attribute.base);

        if (attribute.value != null && attribute.value !== "") {
            const label = "[" + attribute.label + "] : " + "[" + attribute.value + "]";

            this.filter.push({ attribute: attribute.base, value: attribute.value === "null" ? null : attribute.value, label: label });
            this.selected.push(attribute.base);
        }

        this.onPageChange(1);
    }

    handleListChange(e: TypeaheadMatch, attribute: any): void {
        attribute.value = e.item;
        attribute.isCollapsed = true;

        this.selected = this.selected.filter(s => s !== attribute.base);

        this.list.attributes.forEach(attr => {
            if (attr.base === attribute.base) {
                attr.search = "";
            }
        });

        if (attribute.value.value != null && attribute.value.value !== "") {
            const label = "[" + attribute.label + "] : " + "[" + attribute.value.label + "]";

            this.filter.push({ attribute: attribute.base, value: e.item.value, label: label });
            this.selected.push(attribute.base);
            attribute.search = e.item.label;
        } else {
            this.filter = this.filter.filter(f => f.attribute !== attribute.base);
            attribute.search = "";
        }

        this.onPageChange(1);
    }

    isFilterable(attribute: any): boolean {
        return attribute.type !== "none" && attribute.name !== "invalid" && (attribute.dependency.length === 0 || this.selected.indexOf(attribute.base) !== -1 || this.selected.filter(value => attribute.dependency.includes(value)).length > 0);
    }

    onEdit(data): void {
        let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
        editModal.content.configureAsExisting(data.code, this.list.typeCode, this.list.forDate, this.list.isGeometryEditable);
        editModal.content.setListId(this.list.oid);
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            this.onPageChange(this.page.pageNumber);
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

    onNewGeoObject(type: string = null): void {
        if (!type) {
            type = this.list.typeCode;
        }

        const params: any = {
            layers: JSON.stringify([this.list.oid]),
            type: type,
            code: "__NEW__"
        };

        this.router.navigate(["/registry/location-manager"], {
            queryParams: params
        });
    }

    onExport(): void {
        this.bsModalRef = this.modalService.show(ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.onFormat.subscribe(format => {
            if (format === "SHAPEFILE") {
                window.location.href = registry.contextPath + "/list-type/export-shapefile?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.getFilter()));
            } else if (format === "EXCEL") {
                window.location.href = registry.contextPath + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.getFilter()));
            }
        });
    }

    changeTypeaheadLoading(attribute: any, loading: boolean): void {
        attribute.loading = loading;
    }

    formatDate(date: string): string {
        return this.dateService.formatDateForDisplay(date);
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

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
