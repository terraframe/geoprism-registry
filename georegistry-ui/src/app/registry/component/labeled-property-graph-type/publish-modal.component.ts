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
import { HttpErrorResponse } from "@angular/common/http";
import { v4 as uuid } from "uuid";


import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { LabeledPropertyGraphType } from "@registry/model/labeled-property-graph-type";
import { LabeledPropertyGraphTypeService } from "@registry/service/labeled-property-graph-type.service";
import { PRESENT } from "@shared/model/date";

@Component({
    selector: "labeled-property-graph-type-publish-modal",
    templateUrl: "./publish-modal.component.html",
    styleUrls: ["./labeled-property-graph-type-manager.css"]
})
export class LabeledPropertyGraphTypePublishModalComponent implements OnInit {

    message: string = null;
    onChange: any = null;

    type: LabeledPropertyGraphType = null;

    isNew: boolean = false;

    entityLabel: string = '';


    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: LabeledPropertyGraphTypeService,
        private lService: LocalizationService,
        private bsModalRef: BsModalRef) {

    }

    ngOnInit(): void {
    }

    init(onChange: any, type?: LabeledPropertyGraphType): void {

        this.onChange = onChange;

        if (type == null) {
            this.isNew = true;
            this.type = {
                oid: null,
                graphType: "single",
                displayLabel: this.lService.create(),
                description: this.lService.create(),
                code: "graph_" + Math.floor(Math.random() * 999999),
                hierarchy: '',
                strategyType: "",
                strategyConfiguration: {
                    code: null,
                    typeCode: null
                }

            };
        } else {
            this.type = type;
            this.isNew = false;
            this.entityLabel = this.type.strategyConfiguration.code;

            if (this.type.graphType === "interval") {
                this.type.intervalJson.forEach(interval => {
                    interval.readonly = interval.endDate !== PRESENT ? "BOTH" : "START";
                    interval.oid = uuid();
                });
            }
        }
    }

    onSubmit(type: LabeledPropertyGraphType): void {
        this.service.apply(type).then(response => {
            if (this.onChange != null) {
                this.onChange(response);
            }
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
