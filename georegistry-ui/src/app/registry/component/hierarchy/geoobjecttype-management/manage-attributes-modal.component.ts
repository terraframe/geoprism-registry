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
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { StepConfig, ModalTypes } from "@shared/model/modal";
import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { GeoObjectType, AttributeType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";

import { RegistryService } from "@registry/service";

@Component({
    selector: "manage-attributes-modal",
    templateUrl: "./manage-attributes-modal.component.html",
    styleUrls: ["./manage-attributes-modal.css"]
})
export class ManageAttributesModalComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: AttributeType = null;

    @Output() geoObjectTypeChange: EventEmitter<GeoObjectType> = new EventEmitter<GeoObjectType>();
    @Output() stateChange : EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

    message: string = null;
    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.attributes"), active: true, enabled: true }
        ]
    };

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageAttributes, attribute: this.attribute, termOption: "" };

    /*
     * Observable subject for TreeNode changes.  Called when create is successful
     */
    public onDeleteAttribute: Subject<boolean>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef, private modalService: BsModalService, private localizeService: LocalizationService,
        private modalStepIndicatorService: ModalStepIndicatorService, private registryService: RegistryService) { }

    ngOnInit(): void {
        this.onDeleteAttribute = new Subject();
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
    }

    ngOnDestroy() {
        this.onDeleteAttribute.unsubscribe();
    }

    defineAttributeModal(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" });
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
        this.confirmBsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + "[" + attr.label.localizedValue + "]";
        this.confirmBsModalRef.content.data = { attributeType: attr, geoObjectType: this.geoObjectType };
        this.confirmBsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.confirmBsModalRef.content.type = ModalTypes.danger;

        (<ConfirmModalComponent> this.confirmBsModalRef.content).onConfirm.subscribe(data => {
            this.deleteAttributeType(data.geoObjectType.code, data.attributeType);
        });
    }

    deleteAttributeType(geoObjectTypeCode: string, attr: AttributeType): void {
        this.registryService.deleteAttributeType(geoObjectTypeCode, attr.code).then(data => {
            this.onDeleteAttribute.next(data);

            if (data) {
                this.geoObjectType.attributes.splice(this.geoObjectType.attributes.indexOf(attr), 1);
            }

            this.geoObjectTypeChange.emit(this.geoObjectType);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: this.attribute, termOption: "" });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
