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
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { LocalizationService } from "@shared/service";
import { PublishEvents } from "@registry/model/publish";
import { PublishService } from "@registry/service/publish.service";
import { PublishEventsModalComponent } from "./publish-events-modal.component";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { RegistryService } from "@registry/service";

@Component({
    selector: "publish-manager",
    templateUrl: "./publish-manager.component.html",
    styleUrls: []
})
export class PublishManagerComponent implements OnInit, OnDestroy {

    message: string = null;

    publishes: PublishEvents[] = [];
    current: PublishEvents = null;

    subscription: Subscription = null;

    noQueryParams = false;

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    types: { label: string, value: string }[] = [];
    hierarchies: { label: string, value: string }[] = [];
    dagTypes: { label: string, value: string }[] = [];
    undirectedTypes: { label: string, value: string }[] = [];
    businessTypes: { label: string, value: string }[] = [];
    edgeTypes: { label: string, value: string }[] = [];


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: PublishService,
        private modalService: BsModalService,
        private route: ActivatedRoute,
        private router: Router,
        private localizeService: LocalizationService,
        private businessService: BusinessTypeService,
        private registryService: RegistryService,
    ) { }

    ngOnInit(): void {
        this.subscription = this.route.queryParams.subscribe((params: Params) => {
            const uid = params.uid;

            if (uid != null && uid.length > 0) {
                this.service.get(uid).then(current => {
                    this.current = current;
                }).catch((err: HttpErrorResponse) => {
                    this.error(err);
                });
            } else {
                this.noQueryParams = true;
            }

            // this.refresh();
        });

        if (this.publishes.length === 0) {
            this.service.getAll().then(publishes => {
                this.publishes = publishes;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        }

        this.businessService.getAll().then(response => {
            this.businessTypes = response.map(b => { return { label: b.displayLabel.localizedValue, value: b.code } });
        })

        this.businessService.getEdges().then(response => {
            this.edgeTypes = response.map(b => { return { label: b.label.localizedValue, value: b.code } });
        })

        this.registryService.init(false, true).then(response => {
            this.hierarchies = response.hierarchies.map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.types = response.types.map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.dagTypes = response.graphTypes
                .filter(b => b.typeCode === 'DirectedAcyclicGraphType')
                .map(b => { return { label: b.label.localizedValue, value: b.code } });
            this.undirectedTypes = response.graphTypes
                .filter(b => b.typeCode === 'UndirectedGraphType')
                .map(b => { return { label: b.label.localizedValue, value: b.code } });
        });
    }

    ngAfterViewInit() {
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }


    onCreate(): void {
        this.bsModalRef = this.modalService.show(PublishEventsModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.init(this.types,
            this.hierarchies,
            this.dagTypes,
            this.undirectedTypes,
            this.businessTypes,
            this.edgeTypes,
            (publish) => {
                this.publishes.push(publish);

                this.router.navigate([], {
                    relativeTo: this.route,
                    queryParams: { uid: publish.uid },
                    queryParamsHandling: "merge",
                    replaceUrl: true
                });
            }, null);
    }

    onDelete(publish: PublishEvents): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + " Publish [" + publish.uid + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = "danger";

        this.bsModalRef.content.onConfirm.subscribe(() => {
            this.service.remove(publish).then(() => {
                const index = this.publishes.findIndex(v => v.uid === publish.uid);

                if (index !== -1) {
                    this.publishes.splice(index, 1);
                }

                if (this.current != null && this.current.uid === publish.uid) {
                    this.current = null;

                    this.router.navigate([], {
                        relativeTo: this.route,
                        queryParams: { uid: null },
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
