import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent } from "@shared/component";
import { AuthService, LocalizationService } from "@shared/service";
import { ListType, ListTypeEntry, ListTypeVersion } from "@registry/model/list-type";
import { ListTypePublishModalComponent } from "./publish-modal.component";
import { ListTypeService } from "@registry/service/list-type.service";
import { PublishVersionComponent } from "./publish-version.component";
import { Router } from "@angular/router";
import { LngLatBounds } from "mapbox-gl";
import * as ColorGen from "color-generator";
import { ListVectorLayerDataSource } from "@registry/service/layer-data-source";
import { GeometryService } from "@registry/service/geometry.service";

@Component({
    selector: "list-type",
    templateUrl: "./list-type.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListTypeComponent implements OnInit, OnDestroy {

    @Input() list: ListType;
    @Output() error = new EventEmitter<HttpErrorResponse>();
    isRC: boolean = false;

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private router: Router,
        private service: ListTypeService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private geomService: GeometryService,
        private authService: AuthService) { }

    ngOnInit(): void {
        this.isRC = this.authService.isGeoObjectTypeOrSuperRC({
            organizationCode: this.list.organization,
            code: this.list.typeCode,
            superTypeCode: this.list.superTypeCode
        });

        // Expand the most recent version by default
        this.list.entries.filter(entry => {
            return (entry.versions != null && entry.versions.length > 0);
        }).forEach(entry => {
            entry.versions[0].collapsed = true;
        });
    }

    ngOnDestroy() {
    }

    toggleVersions(entry: ListTypeEntry) {
        entry.showAll = !entry.showAll;
    }

    onCreate(entry: ListTypeEntry): void {
        this.bsModalRef = this.modalService.show(PublishVersionComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.list, entry);
    }

    onCreateEntries(): void {
        // Expand the most recent version by default
        this.service.createEntries(this.list.oid).then(list => {
            list.entries.forEach(entry => {
                if (this.list.entries.findIndex(e => e.oid === entry.oid) === -1) {
                    this.list.entries.push(entry);
                }
            });
        }).catch((err: HttpErrorResponse) => {
            this.error.emit(err);
        });
    }

    onEdit(entry: ListTypeEntry, version: ListTypeVersion): void {
        this.bsModalRef = this.modalService.show(PublishVersionComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.list, entry, version);
    }

    onViewConfiguration(list: ListType): void {
        this.bsModalRef = this.modalService.show(ListTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(list, null, list);
    }

    onDelete(entry: ListTypeEntry, version: ListTypeVersion): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " Version [" + version.versionNumber + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.removeVersion(version).then(response => {
                const index = entry.versions.findIndex(v => v.oid === version.oid);

                if (index !== -1) {
                    entry.versions.splice(index, 1);
                }
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

    layerFromVersion(version: ListTypeVersion): any {
        let dataSource = new ListVectorLayerDataSource(this.service, version.oid);
        let layer = dataSource.createLayer(version.displayLabel, true, ColorGen().hexString());
        this.geomService.zoomOnReady(layer.getId());
        return this.geomService.getDataSourceFactory().serializeLayers([layer]);
    }

    onGotoMap(version: ListTypeVersion): void {
        this.service.getBounds(version.oid).then(bounds => {
            const queryParams: any = {
                layers: JSON.stringify(this.layerFromVersion(version))
            };

            if (bounds && Array.isArray(bounds)) {
                let llb = new LngLatBounds([bounds[0], bounds[1]], [bounds[2], bounds[3]]);
                const array = llb.toArray();

                queryParams.bounds = JSON.stringify(array);
            }

            this.router.navigate(["/registry/location-manager"], {
                queryParams: queryParams
            });
        }).catch((err: HttpErrorResponse) => {
            this.error.emit(err);
        });
    }

}
