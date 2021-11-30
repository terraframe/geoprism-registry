import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent } from "@shared/component";
import { AuthService, LocalizationService } from "@shared/service";
import { ListType, ListTypeEntry, ListTypeVersion } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { PublishVersionComponent } from "./publish-version.component";

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
        private service: ListTypeService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private authService: AuthService) { }

    ngOnInit(): void {
        this.isRC = this.authService.isGeoObjectTypeOrSuperRC({
            organizationCode: this.list.organization,
            code: this.list.typeCode,
            superTypeCode: this.list.superTypeCode,
        });

        // Expand the most recent version by default
        this.list.entries.filter(entry => {
            return (entry.versions != null && entry.versions.length > 0);
        }).forEach(entry => {
            entry.versions[0].collapsed = true;
        })
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

    onEdit(entry: ListTypeEntry, version: ListTypeVersion): void {
        this.bsModalRef = this.modalService.show(PublishVersionComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.list, entry, version);
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
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

}
