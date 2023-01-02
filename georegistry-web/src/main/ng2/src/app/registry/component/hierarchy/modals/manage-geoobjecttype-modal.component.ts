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
