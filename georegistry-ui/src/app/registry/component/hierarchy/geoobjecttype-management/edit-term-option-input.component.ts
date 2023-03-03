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

import { Component, OnInit, Input, EventEmitter, Output } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { StepConfig } from "@shared/model/modal";
import { LocalizationService, ModalStepIndicatorService } from "@shared/service";
import { ErrorHandler } from "@shared/component";

import { Term, ManageGeoObjectTypeModalState, AttributeType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { RegistryService } from "@registry/service";

@Component({
    selector: "edit-term-option-input",
    templateUrl: "./edit-term-option-input.component.html",
    styleUrls: [],
    animations: [
        trigger("openClose",
            [
                transition(
                    ":enter", [
                    style({ opacity: 0 }),
                    animate("500ms", style({ opacity: 1 }))
                ]
                ),
                transition(
                    ":leave", [
                    style({ opacity: 1 }),
                    animate("0ms", style({ opacity: 0 }))
                ]
                )]
        )
    ]
})
export class EditTermOptionInputComponent implements OnInit {

    @Input() attribute: AttributeType = null;
    @Input() termOption: Term;

    @Output() stateChange: EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

    message: string = null;
    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.editAttribute, attribute: this.attribute, termOption: "" };
    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.attributes"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.edit.attribute"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.term.options"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.edit.term.option"), active: true, enabled: true }

        ]
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private modalStepIndicatorService: ModalStepIndicatorService,
                private localizeService: LocalizationService, private registryService: RegistryService) { }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit() {

    }

    ngOnDestroy() {
    }

    handleOnSubmit(): void {
        this.registryService.updateAttributeTermTypeOption(this.attribute.rootTerm.code, this.termOption).then(data => {
            // Update the term definition on the attribute
            const index = this.attribute.rootTerm.children.findIndex(t => t.code === data.code);

            if (index !== -1) {
                this.attribute.rootTerm.children[index] = data;
            }

            this.stateChange.emit({ state: GeoObjectTypeModalStates.manageTermOption, attribute: this.attribute, termOption: null });

        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    isFormValid(): boolean {
        // let isAttrValid: boolean = this.attributeInputComponent.isValid();

        // if(isAttrValid){
        //     return true;
        // }

        // return false;
        return true;
    }

    cancel(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageTermOption, attribute: this.attribute, termOption: null });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
