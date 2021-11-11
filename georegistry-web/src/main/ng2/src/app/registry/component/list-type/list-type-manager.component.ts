import { Component, OnDestroy, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { RegistryService } from "@registry/service";

import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { Organization } from "@shared/model/core";
import { GeoObjectType } from "@registry/model/registry";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ListTypePublishModalComponent } from "./publish-modal.component";
import { Subject } from "rxjs";

@Component({
    selector: "list-type-manager",
    templateUrl: "./list-type-manager.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListTypeManagerComponent implements OnInit, OnDestroy {

    message: string = null;
    typesByOrg: { org: Organization, types: GeoObjectType[] }[] = [];
    listByType: ListTypeByType = null;

    /*
     * Observable subject for ListType changes.  Called when an update is successful
     */
    onListTypeChange: Subject<ListType>;

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private registryService: RegistryService,
        private modalService: BsModalService,
        private router: Router,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.onListTypeChange = new Subject();
        this.onListTypeChange.subscribe(() => {

        });

        this.registryService.init().then(response => {
            this.typesByOrg = [];

            response.organizations.forEach(org => {
                this.typesByOrg.push({ org: org, types: response.types.filter(t => t.organizationCode === org.code) });
            })
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    ngOnDestroy() {
        this.onListTypeChange.unsubscribe();
    }

    onTypeClick(type: GeoObjectType): void {
        this.service.listForType(type).then(listByType => {
            this.listByType = listByType;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCreate(): void {
        this.bsModalRef = this.modalService.show(ListTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.listByType, this.onListTypeChange ,true);
    }

    // onView(code: string): void {
    //     this.router.navigate(["/registry/master-list-view/", code])
    // }

    // onEdit(pair: { label: string, oid: string, visibility: string }): void {
    //     this.service.getListType(pair.oid).then(list => {
    //         this.bsModalRef = this.modalService.show(PublishModalComponent, {
    //             animated: true,
    //             backdrop: true,
    //             ignoreBackdropClick: true
    //         });
    //         this.bsModalRef.content.edit = true;
    //         this.bsModalRef.content.readonly = !list.write;
    //         this.bsModalRef.content.master = list;
    //         this.bsModalRef.content.originalPublishStartDate = list.publishingStartDate;
    //         this.bsModalRef.content.isNew = false;

    //         this.bsModalRef.content.onListTypeChange.subscribe(ret => {
    //             pair.label = ret.displayLabel.localizedValue;
    //             pair.visibility = ret.visibility;
    //         });
    //     }).catch((err: HttpErrorResponse) => {
    //         this.error(err);
    //     });
    // }

    // onDelete(org: ListTypeByOrg, list: { label: string, oid: string }): void {
    //     this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
    //         animated: true,
    //         backdrop: true,
    //         ignoreBackdropClick: true
    //     });
    //     this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + list.label + "]";
    //     this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
    //     this.bsModalRef.content.type = "danger";

    //     this.bsModalRef.content.onConfirm.subscribe(data => {
    //         this.service.deleteListType(list.oid).then(response => {
    //             org.lists = org.lists.filter((value, index, arr) => {
    //                 return value.oid !== list.oid;
    //             });
    //         }).catch((err: HttpErrorResponse) => {
    //             this.error(err);
    //         });
    //     });
    // }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
