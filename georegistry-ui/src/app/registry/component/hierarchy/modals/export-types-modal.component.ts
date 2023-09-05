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

import { Component, OnInit, ElementRef, ViewChild, OnDestroy } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { Organization } from "@shared/model/core";

@Component({
    selector: "export-types-modal",
    templateUrl: "./export-types-modal.component.html",
    styleUrls: []
})
export class ExportTypesModalComponent implements OnInit, OnDestroy {

    public organizations: Organization[] = [];
    public orgCode: string;


    public onNodeChange: Subject<string>;

    constructor(public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }

    ngOnDestroy(): void {
        this.onNodeChange.unsubscribe();
    }

    init(organizations: Organization[]): void {
        this.organizations = organizations;
    }

    onSelect(event: Event): void {
        this.orgCode = (event.target as HTMLInputElement).value;
    }

    onClick(): void {
        this.onNodeChange.next(this.orgCode);

        this.bsModalRef.hide();
    }

}
