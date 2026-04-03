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

import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";

import { LocalizationService } from "@shared/service/localization.service";
import { GraphTypeService } from "@registry/service/graph-type.service";
import { GraphType } from "@registry/model/registry";

@Component({
    selector: "create-graph-type",
    templateUrl: "./create-graph-type.component.html",
    styleUrls: []
})
export class CreateGraphTypeComponent implements OnInit {

    @Input() typeCode: string;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<GraphType> = new EventEmitter<GraphType>()

    type: GraphType;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: GraphTypeService,
        private lService: LocalizationService) {
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

        this.service.apply(this.typeCode, this.type).then(data => {
            this.typeChange.emit(data);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleCancel(): void {
        this.onCancel.emit();
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
