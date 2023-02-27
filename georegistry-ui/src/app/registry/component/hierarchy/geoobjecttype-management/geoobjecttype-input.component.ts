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

import { Component, OnInit, Input, Output, EventEmitter } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { StepConfig, ModalTypes } from "@shared/model/modal";
import { ErrorHandler, ConfirmModalComponent } from "@shared/component";

import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { GeoObjectType, ManageGeoObjectTypeModalState, AttributeType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { RegistryService } from "@registry/service";

@Component({
    selector: "geoobjecttype-input",
    templateUrl: "./geoobjecttype-input.component.html",
    styleUrls: ["./geoobjecttype-input.css"],
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
export class GeoObjectTypeInputComponent implements OnInit {

    @Input() readOnly: boolean = false;
    @Input() geoObjectType: GeoObjectType;

    @Output() geoObjectTypeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>();
    @Output() stateChange: EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

    editGeoObjectType: GeoObjectType;

    organizationLabel: string;

    // eslint-disable-next-line accessor-pairs
    @Input("setGeoObjectType")
    set in(geoObjectType: GeoObjectType) {
        if (geoObjectType) {
            this.editGeoObjectType = JSON.parse(JSON.stringify(geoObjectType));
        }
    }

    message: string = null;

    // modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizationService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: true }
        ]
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef, private modalService: BsModalService,
        private modalStepIndicatorService: ModalStepIndicatorService,
        private localizationService: LocalizationService, private registryService: RegistryService) { }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);

        // this.geoObjectTypeManagementService.setModalState(this.modalState);

        this.fetchOrganizationLabel();
    }

    defineAttributeModal(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" });
    }

    fetchOrganizationLabel(): void {
        this.registryService.getOrganizations().then(orgs => {
            for (let i = 0; i < orgs.length; ++i) {
                if (orgs[i].code === this.editGeoObjectType.organizationCode) {
                    this.organizationLabel = orgs[i].label.localizedValue;
                }
            }
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    manageAttributes(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageAttributes, attribute: "", termOption: "" });
    }

    // onModalStateChange(state: ManageGeoObjectTypeModalState): void {
    //     this.modalState = state;
    // }

    update(): void {
        this.registryService.updateGeoObjectType(this.editGeoObjectType).then(geoObjectType => {
            // emit the persisted geoobjecttype to the parent widget component (manage-geoobjecttype.component)
            // so that the change can be updated in the template
            this.geoObjectTypeChange.emit(geoObjectType);

            this.close();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    // resetGeoObjectType(): void {
    //     this.geoObjectType = this.geoObjectTypeOriginal;
    // }

    close(): void {
        // this.resetGeoObjectType();
        this.bsModalRef.hide();
    }

    isValid(): boolean {
        // if(this.attribute.code && this.attribute.label) {

        //     // if code has a space
        //     if(this.attribute.code.indexOf(" ") !== -1){
        //         return false;
        //     }

        //     // If label is only spaces
        //     if(this.attribute.label.replace(/\s/g, '').length === 0) {
        //         return false
        //     }

        //     return true;
        // }

        // return false;

        return true;
    }

    editAttribute(attr: AttributeType, e: any): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.editAttribute, attribute: attr, termOption: "" });
    }

    removeAttributeType(attr: AttributeType, e: any): void {
        this.confirmBsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.confirmBsModalRef.content.message = this.localizationService.decode("confirm.modal.verify.delete") + "[" + attr.label.localizedValue + "]";
        this.confirmBsModalRef.content.data = { attributeType: attr, geoObjectType: this.geoObjectType };
        this.confirmBsModalRef.content.submitText = this.localizationService.decode("modal.button.delete");
        this.confirmBsModalRef.content.type = ModalTypes.danger;

        (<ConfirmModalComponent> this.confirmBsModalRef.content).onConfirm.subscribe(data => {
            this.deleteAttributeType(data.geoObjectType.code, data.attributeType);
        });
    }

    deleteAttributeType(geoObjectTypeCode: string, attr: AttributeType): void {
        this.registryService.deleteAttributeType(geoObjectTypeCode, attr.code).then(data => {
            this.geoObjectType.attributes.splice(this.geoObjectType.attributes.indexOf(attr), 1);

            this.geoObjectTypeChange.emit(this.geoObjectType);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
