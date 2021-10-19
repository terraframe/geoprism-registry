import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { GeoObjectType, AttributeType, ManageGeoObjectTypeModalState, AttributedType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";

import { ErrorHandler } from "@shared/component";
import { StepConfig } from "@shared/model/modal";

import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { AttributeTypeService, RegistryService } from "@registry/service";

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

    @Input() geoObjectType: AttributedType;
    @Input() attribute: AttributeType = null;
    @Input() service: AttributeTypeService;

    @Output() geoObjectTypeChange: EventEmitter<AttributedType> = new EventEmitter<AttributedType>();
    @Output() stateChange: EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

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
        private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngAfterViewInit() {

    }

    ngOnDestroy() {
    }

    handleOnSubmit(): void {
        this.service.updateAttributeType(this.geoObjectType.code, this.attribute).then(data => {
            for (let i = 0; i < this.geoObjectType.attributes.length; i++) {
                let attr = this.geoObjectType.attributes[i];
                if (attr.code === data.code) {
                    Object.assign(attr, data);
                    break;
                }
            }

            this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });

            this.geoObjectTypeChange.emit(this.geoObjectType);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.stateChange.emit(state);
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
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });
    }

    back(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
