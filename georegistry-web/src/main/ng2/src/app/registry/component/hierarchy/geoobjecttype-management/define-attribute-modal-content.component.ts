import { Component, OnInit, ViewChild, Input, EventEmitter, Output } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { ErrorHandler } from "@shared/component";
import { StepConfig } from "@shared/model/modal";

import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { GeoObjectType, AttributeType, AttributeTerm, AttributeDecimal, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { RegistryService, GeoObjectTypeManagementService } from "@registry/service";
import { AttributeInputComponent } from "../geoobjecttype-management/attribute-input.component";

@Component({
    selector: "define-attribute-modal-content",
    templateUrl: "./define-attribute-modal-content.component.html",
    styleUrls: ["./define-attribute-modal-content.css"]
})
export class DefineAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Output() geoObjectTypeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>();

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
        private geoObjectTypeManagementService: GeoObjectTypeManagementService,
        private localizeService: LocalizationService,
        private registryService: RegistryService) { }

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

        this.registryService.addAttributeType(this.geoObjectType.code, this.newAttribute).then(data => {

            this.geoObjectType.attributes.push(data);

            this.geoObjectTypeManagementService.setModalState({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

            this.geoObjectTypeChange.emit(this.geoObjectType);

        }).catch((err: HttpErrorResponse) => {

            this.error(err);

        });

    }

    setAttribute(type: string): void {

        if (type === "term") {

            this.newAttribute = new AttributeTerm("", type, this.localizeService.create(), this.localizeService.create(), false, false, false, true);

        } else if (type === "float") {

            this.newAttribute = new AttributeDecimal("", type, this.localizeService.create(), this.localizeService.create(), false, false, false, true);

        } else {

            this.newAttribute = new AttributeType("", type, this.localizeService.create(), this.localizeService.create(), false, false, false, true);

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

        this.geoObjectTypeManagementService.setModalState({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

    }

    error(err: HttpErrorResponse): void {

        this.message = ErrorHandler.getMessageFromError(err);

    }

}
