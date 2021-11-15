import { Component, OnInit, OnDestroy } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { RegistryService } from "@registry/service";
import { DateService } from "@shared/service/date.service";
import { GeoObjectEditorComponent } from "../geoobject-editor/geoobject-editor.component";

import { ErrorHandler } from "@shared/component";
import { LocalizationService, AuthService, ProgressService } from "@shared/service";
import { ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ExportFormatModalComponent } from "../master-list/export-format-modal.component";

declare let acp: string;

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
    listAttrs: any[];

    showInvalid = false;

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    public searchPlaceholder = "";

    notifier: WebSocketSubject<{ type: string, content: any }>;

    constructor(public service: ListTypeService,  private route: ActivatedRoute, private dateService: DateService,
        private modalService: BsModalService, private localizeService: LocalizationService, private authService: AuthService) {
        this.searchPlaceholder = localizeService.decode("masterlist.search");
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get("oid");

        this.service.getVersion(oid).then(version => {
            this.setList(version);
            const orgCode = this.list.orgCode;
            const typeCode = this.list.superTypeCode != null ? this.list.superTypeCode : this.list.typeCode;

            this.isWritable = this.authService.isGeoObjectTypeRC(orgCode, typeCode);

            this.onPageChange(1);

            if (version.refreshProgress != null) {
                this.handleProgressChange(version.refreshProgress);
            }
        });

        let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + acp;

        this.notifier = webSocket(baseUrl + "/websocket/progress/" + oid);
        this.notifier.subscribe(message => {
            if (message.content != null) {
                this.handleProgressChange(message.content);
            } else {
                this.handleProgressChange(message);
            }
        });
    }

    ngOnDestroy() {
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

        return attrs;
    }

    getTypeaheadDataObservable(attribute: any): void {
        return Observable.create((observer: any) => {
            this.message = null;

            // Get the valid values
            this.service.values(this.list.oid, attribute.search, attribute.name, attribute.base, this.getFilter()).then(options => {
                options.unshift({ label: "[" + this.localizeService.decode("masterlist.nofilter") + "]", value: null });

                observer.next(options);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    handleProgressChange(progress: any): void {
        this.isRefreshing = (progress.current < progress.total);

        // this.pService.progress(progress);

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

            this.filter.push({ attribute: attribute.base, value: attribute.value, label: label });
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

        // this.service.publishList(this.list.oid).toPromise()
        //     .then((historyOid: string) => {
        //         this.isRefreshing = true;
        //     }).catch((err: HttpErrorResponse) => {
        //         this.error(err);
        //     });

    }

    onNewGeoObject(): void {
        let editModal = this.modalService.show(GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true });
        // editModal.content.fetchGeoObject( data.code, this.list.typeCode );
        editModal.content.configureAsNew(this.list.typeCode, this.list.forDate, this.list.isGeometryEditable);
        editModal.content.setListId(this.list.oid);
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            this.onPageChange(this.page.pageNumber);
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
                window.location.href = acp + "/list-type/export-shapefile?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.getFilter()));
            } else if (format === "EXCEL") {
                window.location.href = acp + "/list-type/export-spreadsheet?oid=" + this.list.oid + "&filter=" + encodeURIComponent(JSON.stringify(this.getFilter()));
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

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
