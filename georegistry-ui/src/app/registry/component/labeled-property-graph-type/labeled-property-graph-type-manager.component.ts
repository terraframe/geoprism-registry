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

import { Component, OnDestroy, OnInit, ViewChildren, QueryList } from "@angular/core";
import { ActivatedRoute, Params, Router } from "@angular/router";
import { HttpErrorResponse } from "@angular/common/http";


import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { Subscription } from "rxjs";
import { LabeledPropertyGraphType } from "@registry/model/labeled-property-graph-type";
import { LabeledPropertyGraphTypeService } from "@registry/service/labeled-property-graph-type.service";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { LabeledPropertyGraphTypePublishModalComponent } from "./publish-modal.component";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "labeled-property-graph-type-manager",
    templateUrl: "./labeled-property-graph-type-manager.component.html",
    styleUrls: ["./labeled-property-graph-type-manager.css"]
})
export class LabeledPropertyGraphTypeManagerComponent implements OnInit, OnDestroy {

    message: string = null;

    types: { label: string, oid: string }[] = [];
    current: LabeledPropertyGraphType = null;

    subscription: Subscription = null;

    noQueryParams = false;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: LabeledPropertyGraphTypeService,
        private modalService: BsModalService,
        private route: ActivatedRoute,
        private router: Router,
        private localizeService: LocalizationService
    ) { }

    ngOnInit(): void {
        this.subscription = this.route.queryParams.subscribe((params: Params) => {
            const oid = params.oid;

            if (oid != null && oid.length > 0) {
                this.service.entries(oid).then(current => {
                    this.current = current;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            } else {
                this.noQueryParams = true;
            }

            // this.refresh();
        });

        if (this.types.length === 0) {
            this.service.getAll().then(types => {
                this.types = types;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }
    }

    ngAfterViewInit() {
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    onCreate(): void {
        this.bsModalRef = this.modalService.show(LabeledPropertyGraphTypePublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init((type) => {
            this.types.push({ oid: type.oid, label: type.displayLabel.localizedValue });

            this.router.navigate([], {
                relativeTo: this.route,
                queryParams: { oid: type.oid },
                queryParamsHandling: "merge",
                replaceUrl: true
            });
        }, null);
    }

    onEdit(type: { label: string, oid: string }): void {
        // this.bsModalRef = this.modalService.show(PublishVersionComponent, {
        //     animated: true,
        //     backdrop: true,
        //     ignoreBackdropClick: true
        // });
        // this.bsModalRef.content.init(this.type, entry, version);
    }

    onDelete(type: { label: string, oid: string }): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " Version [" + type.label + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.service.remove(type).then(() => {
                const index = this.types.findIndex(v => v.oid === type.oid);

                if (index !== -1) {
                    this.types.splice(index, 1);
                }

                if (this.current != null && this.current.oid === type.oid) {
                    this.current = null;

                    this.router.navigate([], {
                        relativeTo: this.route,
                        queryParams: { oid: null },
                        queryParamsHandling: "merge",
                        replaceUrl: true
                    });

                }
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }


    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
