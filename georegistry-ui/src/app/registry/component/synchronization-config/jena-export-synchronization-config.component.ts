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

import { Component, OnInit, Input, OnDestroy, EventEmitter, Output } from "@angular/core";
import { Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { SynchronizationConfig } from "@registry/model/registry";
import { SynchronizationConfigService } from "@registry/service";
import { ListTypeService } from "@registry/service/list-type.service";
import { ListTypeVersion } from "@registry/model/list-type";
import { PublishService } from "@registry/service/publish.service";
import { Publish } from "@registry/model/publish";

@Component({
    selector: "jena-export-synchronization-config",
    templateUrl: "./jena-export-synchronization-config.component.html",
    styleUrls: []
})
export class JenaExportSynchronizationConfigComponent implements OnInit, OnDestroy {

    @Input() config: SynchronizationConfig;
    @Input() fieldChange: Subject<string>;
    @Output() onError = new EventEmitter<HttpErrorResponse>();

    subscription: Subscription = null;
    publishes: Publish[] = null;

    constructor(private pService: PublishService) { }

    ngOnInit(): void {
        this.reset();

        this.subscription = this.fieldChange.subscribe((field: string) => {
            if (field === "organization" || field === "system") {
                this.reset();
            }
        });

        this.pService.getAll().then(publishes => {
            this.publishes = publishes;
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    reset(): void {
        if (this.config.configuration == null) {
            this.config.configuration = {
                publishUid: null,
            };
        }
        // Get
        this.pService.getAll().then(publishes => {
            this.publishes = publishes;
        });
    }


    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
