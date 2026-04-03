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
import { ManageGeoObjectTypeModalState, GeoObjectType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { RegistryService } from "@registry/service";

@Component({
    selector: "manage-geo-object-type",
    templateUrl: "./manage-geo-object-type.component.html",
    styleUrls: ["./manage-geo-object-type.css"],
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
export class ManageGeoObjectTypeComponent implements OnInit {

    @Input() type: GeoObjectType;
    @Input() readOnly: boolean = false;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>()

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    constructor(public service: RegistryService) {
    }

    ngOnInit(): void {
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
    }

    onGeoObjectTypeChange(data: any): void {
        // send persisted geoobjecttype to the parent calling component (hierarchy.component) so the
        // updated GeoObjectType can be reflected in the template
        this.typeChange.emit(data);
    }

    update(): void {

    }

    close(): void {
        this.onCancel.emit();
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
