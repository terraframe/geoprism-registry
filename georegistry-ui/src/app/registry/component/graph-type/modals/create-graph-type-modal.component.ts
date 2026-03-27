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

import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";

import { LocalizationService } from "@shared/service/localization.service";
import { GraphTypeService } from "@registry/service/graph-type.service";
import { GraphType } from "@registry/model/registry";

@Component({
    selector: "create-graph-type-modal",
    templateUrl: "./create-graph-type-modal.component.html",
    styleUrls: []
})
export class CreateGraphTypeModalComponent implements OnInit {

    typeCode: string;

    type: GraphType;
    message: string = null;
    organizationLabel: string;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onGraphTypeChange: Subject<GraphType>;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: GraphTypeService,
        private lService: LocalizationService,
        public bsModalRef: BsModalRef) {
        this.onGraphTypeChange = new Subject<GraphType>();
    }

    ngOnInit(): void {
        this.type = {
            code: "",
            typeCode: "",
            label: this.lService.create(),
            description: this.lService.create(),
        };
    }

    init(typeCode: string) {
        this.typeCode = typeCode;
        this.type = {
            code: "",
            typeCode: typeCode,
            label: this.lService.create(),
            description: this.lService.create(),
        };
    }

    handleOnSubmit(): void {
        this.message = null;

        this.service.apply(this.typeCode, this.type).then(data => {
            this.onGraphTypeChange.next(data);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
