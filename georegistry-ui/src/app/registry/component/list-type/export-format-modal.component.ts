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
import { Subject } from "rxjs";

@Component({
    selector: "export-format-modal",
    templateUrl: "./export-format-modal.component.html",
    styleUrls: []
})
export class ExportFormatModalComponent implements OnInit, OnDestroy {

    format: string;

    actualGeometryType: string;

    list: ListTypeVersion = null;

    /*
     * Called on confirm
     */
    public onFormat: Subject<{ format: string, actualGeometryType: string }>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef) { }

    ngOnInit(): void {
        this.onFormat = new Subject();
    }

    ngOnDestroy(): void {
        this.onFormat.unsubscribe();
    }

    init(list: ListTypeVersion): void {
        this.list = list;
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onFormat.next({
            format: this.format,
            actualGeometryType: this.actualGeometryType
        });
    }

}
