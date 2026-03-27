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
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { GraphType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GraphTypeService } from "@registry/service/graph-type.service";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "manage-graph-type-modal",
    templateUrl: "./manage-graph-type-modal.component.html",
    styleUrls: ["./manage-graph-type-modal.css"],
    // host: { '[@fadeInOut]': 'true' },
    animations: [
        [
            trigger("fadeInOut", [
                transition("void => *", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ]),
                transition(":leave",
                    animate("500ms",
                        style({
                            opacity: 0
                        })
                    )
                )
            ])
        ]]
})
export class ManageGraphTypeModalComponent implements OnInit {

    message: string = null;

    typeCode: string;
    type: GraphType = null;

    public onGraphTypeChange: Subject<GraphType>;
    readOnly: boolean = false;

    constructor(public service: GraphTypeService, private localizationService: LocalizationService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onGraphTypeChange = new Subject();
    }

    init(typeCode: string, type: GraphType, readOnly: boolean) {
        this.typeCode = typeCode;
        this.type = type;
        this.readOnly = readOnly;
    }
    
    onTypeChange(): void {
        this.onGraphTypeChange.next(this.type);
    }

    update(): void {
        this.service.apply(this.typeCode, this.type).then(type => {
            this.onGraphTypeChange.next(type);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        if (this.type.code != null) {
            this.service.get(this.typeCode, this.type.code).then(() => {
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.bsModalRef.hide();
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
