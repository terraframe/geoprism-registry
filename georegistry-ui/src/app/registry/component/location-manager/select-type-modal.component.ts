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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { ListTypeVersion } from "@registry/model/list-type";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Observer, Subject, Subscription } from "rxjs";


@Component({
    selector: "select-type-modal",
    templateUrl: "./select-type-modal.component.html",
    styleUrls: []
})
export class SelectTypeModalComponent implements OnInit, OnDestroy {

    version: ListTypeVersion;

    type: string;

    /*
     * Called on confirm
     */
    onCreate: Subject<string>;

    subscription: Subscription;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onCreate = new Subject();
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }

        this.onCreate.unsubscribe();
    }

    init(version: ListTypeVersion, observer: Observer<string>): void {
        this.version = version;
        this.subscription = this.onCreate.subscribe(observer);
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onCreate.next(this.type);
    }
}
