///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { ConfirmModalComponent } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
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

                this.createGroups(this.listByType);
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
            this.createGroups(changes.listByType.currentValue);
        }
    }

    createGroups(listByType: ListTypeByType): void {
        this.groups = {
            single: [],
            interval: [],
            incremental: []
        };

        listByType.lists.forEach(list => {
            if (list.listType === "single") {
                this.groups.single.push(list);
            } else if (list.listType === "interval") {
                this.groups.interval.push(list);
            } else if (list.listType === "incremental") {
                this.groups.incremental.push(list);
            }
        });
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

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.service.remove(list).then(() => {
                this.listByType.lists = this.listByType.lists.filter((value) => {
                    return value.oid !== list.oid;
                });

                this.createGroups(this.listByType);
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

}
