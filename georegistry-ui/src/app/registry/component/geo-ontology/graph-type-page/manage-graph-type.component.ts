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
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { HttpErrorResponse } from "@angular/common/http";
import { GraphType } from "@registry/model/registry";
import { GraphTypeService } from "@registry/service/graph-type.service";

@Component({
    selector: "manage-graph-type",
    templateUrl: "./manage-graph-type.component.html",
    styleUrls: ["./manage-graph-type.css"],
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
export class ManageGraphTypeComponent implements OnInit {

    @Input() typeCode: string;
    @Input() type: GraphType = null;
    @Input() readOnly: boolean = false;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<GraphType> = new EventEmitter<GraphType>()


    constructor(public service: GraphTypeService) {
    }

    ngOnInit(): void {
    }

    update(): void {
        this.service.apply(this.typeCode, this.type).then(type => {
            this.typeChange.emit(type);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        this.onCancel.emit();
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
