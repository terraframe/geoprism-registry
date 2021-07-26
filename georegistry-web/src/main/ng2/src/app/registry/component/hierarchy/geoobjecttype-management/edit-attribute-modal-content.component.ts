import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { GeoObjectType, Attribute, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";

import { ErrorHandler } from "@shared/component";
import { StepConfig } from "@shared/model/modal";

import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { RegistryService, GeoObjectTypeManagementService } from "@registry/service";

import { AttributeInputComponent } from "../geoobjecttype-management/attribute-input.component";

@Component({
    selector: "edit-attribute-modal-content",
    templateUrl: "./edit-attribute-modal-content.component.html",
    styleUrls: ["./edit-attribute-modal-content.css"],
    // host: { '[@fadeInOut]': 'true' },
    animations: [
        [
            trigger("fadeInOut", [
                transition("void => *", [
                    style({
                        opacity: 0
                    }),
                    animate("500ms")
                ])
            ])
        ]]
})
export class EditAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: Attribute = null;
    @Output() geoObjectTypeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>();

    message: string = null;
    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.editAttribute, attribute: this.attribute, termOption: "" };
    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.attributes"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.edit.attribute"), active: true, enabled: true }
        ]
    };

    @ViewChild(AttributeInputComponent) attributeInputComponent: AttributeInputComponent;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef,
        private modalStepIndicatorService: ModalStepIndicatorService,
        private geoObjectTypeManagementService: GeoObjectTypeManagementService,
        private localizeService: LocalizationService,
        private registryService: RegistryService) { }

    ngOnInit(): void {

        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);

    }

    ngAfterViewInit() {

    }

    ngOnDestroy() {
    }

    handleOnSubmit(): void {

        this.registryService.updateAttributeType(this.geoObjectType.code, this.attribute).then(data => {

            for (let i = 0; i < this.geoObjectType.attributes.length; i++) {

                let attr = this.geoObjectType.attributes[i];
                if (attr.code === data.code) {

                    Object.assign(attr, data);
                    break;

                }

            }

            this.geoObjectTypeManagementService.setModalState({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

            this.geoObjectTypeChange.emit(this.geoObjectType);

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

        this.geoObjectTypeManagementService.setModalState({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

    }

    back(): void {

        this.geoObjectTypeManagementService.setModalState({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

    }

    error(err: HttpErrorResponse): void {

        this.message = ErrorHandler.getMessageFromError(err);

    }

}
