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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import { BsModalService, BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { Subscription } from "rxjs";
import { webSocket, WebSocketSubject } from "rxjs/webSocket";
import { WebSockets } from "@shared/component/web-sockets/web-sockets";

import { ConfirmModalComponent } from "@shared/component";
import { DateService, LocalizationService, ProgressService } from "@shared/service";
import { LabeledPropertyGraphType, LabeledPropertyGraphTypeEntry, LabeledPropertyGraphTypeVersion } from "@registry/model/labeled-property-graph-type";
import { LabeledPropertyGraphTypeService } from "@registry/service/labeled-property-graph-type.service";
import { LabeledPropertyGraphTypePublishModalComponent } from "./publish-modal.component";
import { Progress } from "@shared/model/progress";


@Component({
    selector: "labeled-property-graph-type",
    templateUrl: "./labeled-property-graph-type.component.html",
    styleUrls: ["./labeled-property-graph-type-manager.css"]
})
export class LabeledPropertyGraphTypeComponent implements OnInit, OnDestroy {

    @Input() type: LabeledPropertyGraphType;
    @Output() error = new EventEmitter<HttpErrorResponse>();

    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;

    progressNotifier: WebSocketSubject<any>;
    progressSubscription: Subscription = null;

    isRefreshing: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: LabeledPropertyGraphTypeService,
        private modalService: BsModalService,
        private localizeService: LocalizationService,
        private pService: ProgressService,
        private dateService: DateService) { }

    ngOnInit(): void {
        // Expand the most recent version by default
        this.type.entries.filter(entry => {
            return (entry.versions != null && entry.versions.length > 0);
        }).forEach(entry => {
            entry.versions[0].collapsed = true;
        });

        let baseUrl = WebSockets.buildBaseUrl();
        this.progressNotifier = webSocket(baseUrl + "/websocket/progress/" + this.type.oid);
        this.progressSubscription = this.progressNotifier.subscribe(message => {
            if (message.content != null) {
                this.handleProgressChange(message.content);
            } else {
                this.handleProgressChange(message);
            }
        });

    }

    ngOnDestroy() {
        if (this.progressSubscription != null) {
            this.progressSubscription.unsubscribe();
        }

        this.progressNotifier.complete();
    }

    toggleVersions(entry: LabeledPropertyGraphTypeEntry) {
        entry.showAll = !entry.showAll;
    }

    onCreate(entry: LabeledPropertyGraphTypeEntry): void {

        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = "Are you sure you want to publish a new version of the entry [" + entry.period.value + "]";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.service.createVersion(entry).then(version => {
                entry.versions.unshift(version);
            }).catch((err: HttpErrorResponse) => {
                this.error.emit(err);
            });
        });
    }

    onCreateEntries(): void {
        this.service.createEntries(this.type.oid).then(type => {
            type.entries.forEach(entry => {
                if (this.type.entries.findIndex(e => e.oid === entry.oid) === -1) {
                    this.type.entries.push(entry);
                }

                // Order by date
                this.type.entries.sort((a, b) => {
                    const date1 = this.dateService.getDateFromDateString(a.forDate);
                    const date2 = this.dateService.getDateFromDateString(b.forDate);

                    return date2.getTime() - date1.getTime();
                });
            });
        }).catch((err: HttpErrorResponse) => {
            this.error.emit(err);
        });
    }

    onViewConfiguration(type: LabeledPropertyGraphType): void {
        this.bsModalRef = this.modalService.show(LabeledPropertyGraphTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(null, type);
    }

    onDelete(entry: LabeledPropertyGraphTypeEntry, version: LabeledPropertyGraphTypeVersion): void {
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

    handleProgressChange(progress: Progress): void {
        console.log(progress)

        this.isRefreshing = (progress.current < progress.total);
        progress.description = '';

        this.pService.progress(progress);
    }

}
