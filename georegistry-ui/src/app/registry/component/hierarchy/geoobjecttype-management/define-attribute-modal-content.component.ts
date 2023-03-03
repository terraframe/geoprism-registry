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

import { Component, OnInit, ViewChild, Input, EventEmitter, Output } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { StepConfig } from "@shared/model/modal";

import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { AttributeType, ManageGeoObjectTypeModalState, AttributedType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { AttributeTypeService } from "@registry/service";
import { AttributeInputComponent } from "../geoobjecttype-management/attribute-input.component";

@Component({
    selector: "define-attribute-modal-content",
    templateUrl: "./define-attribute-modal-content.component.html",
    styleUrls: ["./define-attribute-modal-content.css"]
})
export class DefineAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: AttributedType;
    @Input() service: AttributeTypeService;

    @Output() stateChange: EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();
    @Output() geoObjectTypeChange: EventEmitter<AttributedType> = new EventEmitter<AttributedType>();

    message: string = null;
    newAttribute: AttributeType = null;
    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.attributes"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.create.attribute"), active: true, enabled: true }
        ]
    };

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" };

    @ViewChild(AttributeInputComponent) attributeInputComponent: AttributeInputComponent;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        public bsModalRef: BsModalRef,
        private modalStepIndicatorService: ModalStepIndicatorService,
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.setAttribute("character");
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit(): void {
        if (this.attributeInputComponent) {
            this.attributeInputComponent.animate();
        }
    }

    handleOnSubmit(): void {
        this.service.addAttributeType(this.geoObjectType.code, this.newAttribute).then(data => {
            this.geoObjectType.attributes.push(data);

            this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

            this.geoObjectTypeChange.emit(this.geoObjectType);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    setAttribute(type: string): void {
        this.newAttribute = {
            code: "",
            type: type,
            label: this.localizeService.create(),
            description: this.localizeService.create(),
            isDefault: false,
            required: false,
            unique: false,
            isChangeOverTime: true,
        }

        if (type === "term") {
            this.newAttribute.rootTerm = {
                code: null,
                label: null,
                description: null
            };
            this.newAttribute.termOptions = [];

        } else if (type === "float") {
            this.newAttribute.precision = 32;
            this.newAttribute.scale = 8;
        }
    }

    isFormValid(): boolean {
        let isAttrValid: boolean = false;

        if (this.attributeInputComponent) {
            isAttrValid = this.attributeInputComponent.isValid();
        }

        if (isAttrValid) {
            return true;
        }

        return false;
    }

    cancel(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
