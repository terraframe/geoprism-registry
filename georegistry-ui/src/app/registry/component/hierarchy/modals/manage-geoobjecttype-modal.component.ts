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
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { ManageGeoObjectTypeModalState, GeoObjectType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { RegistryService } from "@registry/service";

@Component({
    selector: "manage-geoobjecttype-modal",
    templateUrl: "./manage-geoobjecttype-modal.component.html",
    styleUrls: ["./manage-geoobjecttype-modal.css"],
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
export class ManageGeoObjectTypeModalComponent implements OnInit {

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    message: string = null;
    geoObjectType: GeoObjectType;
    public onGeoObjectTypeSubmitted: Subject<GeoObjectType>;
    readOnly: boolean = false;

    constructor(public service: RegistryService, public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onGeoObjectTypeSubmitted = new Subject();
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
    }

    onGeoObjectTypeChange(data: any): void {
        // send persisted geoobjecttype to the parent calling component (hierarchy.component) so the
        // updated GeoObjectType can be reflected in the template
        this.onGeoObjectTypeSubmitted.next(data);
    }

    update(): void {

    }

    close(): void {
        this.bsModalRef.hide();
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
