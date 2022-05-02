import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { ListTypePublishModalComponent } from "./publish-modal.component";
import { Subject } from "rxjs";

@Component({
    selector: "lists-for-type",
    templateUrl: "./lists-for-type.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListsForTypeComponent implements OnInit, OnDestroy, OnChanges {

    @Input() listByType: ListTypeByType = null;
    @Output() error = new EventEmitter<HttpErrorResponse>();

    groups = {
        single: [],
        interval: [],
        incremental: []
    };

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
        private modalService: BsModalService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.onListTypeChange = new Subject();
        this.onListTypeChange.subscribe(() => {
            // Refresh
            this.service.listForType(this.listByType.typeCode).then(listByType => {
                this.listByType = listByType;
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

    ngOnDestroy() {
        this.onListTypeChange.unsubscribe();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.listByType != null) {
            this.groups = {
                single: [],
                interval: [],
                incremental: []
            };

            changes.listByType.currentValue.lists.forEach(list => {
                if (list.listType === "single") {
                    this.groups.single.push(list);
                } else if (list.listType === "interval") {
                    this.groups.interval.push(list);
                } else if (list.listType === "incremental") {
                    this.groups.incremental.push(list);
                }
            });
        }
    }

    onCreate(): void {
        this.bsModalRef = this.modalService.show(ListTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.listByType, this.onListTypeChange);
    }

    onEdit(list: ListType): void {
        this.bsModalRef = this.modalService.show(ListTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.listByType, this.onListTypeChange, list);
    }

    onDelete(list: ListType): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " [" + list.displayLabel.localizedValue + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(data => {
            this.service.remove(list).then(response => {
                this.listByType.lists = this.listByType.lists.filter((value, index, arr) => {
                    return value.oid !== list.oid;
                });
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

}
