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
import { BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";
import { ConfirmModalComponent } from "@shared/component";
import { ConceptClass } from "@registry/model/object-class";
import { AttributeType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { ModalTypes } from "@shared/model/modal";
import { LocalizationService } from "@shared/service/localization.service";
import { LocalizePipe } from "@shared/pipe/localize.pipe";
import { EditTermOptionInputComponent } from "../../geo-ontology/geoobjecttype-management/edit-term-option-input.component";
import { ManageTermOptionsComponent } from "../../geo-ontology/geoobjecttype-management/manage-term-options.component";
import { EditAttributeModalContentComponent } from "../../geo-ontology/geoobjecttype-management/edit-attribute-modal-content.component";
import { DefineAttributeModalContentComponent } from "../../geo-ontology/geoobjecttype-management/define-attribute-modal-content.component";
import { RouterLink } from "@angular/router";
import { LocalizedInputComponent } from "../../form-fields/localized-input/localized-input.component";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { FormsModule } from "@angular/forms";
import { NgIf, NgFor } from "@angular/common";
import { ConceptClassService } from "@registry/service/concept-class.service";

@Component({
    selector: "manage-concept-class",
    templateUrl: "./manage-concept-class.component.html",
    styleUrls: ["./manage-concept-class.css"],
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
                transition(":leave", animate("500ms", style({
                    opacity: 0
                })))
            ])
        ]
    ],
    standalone: true,
    imports: [NgIf, FormsModule, LocalizeComponent, LocalizedInputComponent, NgFor, RouterLink, DefineAttributeModalContentComponent, EditAttributeModalContentComponent, ManageTermOptionsComponent, EditTermOptionInputComponent, LocalizePipe]
})
export class ManageConceptClassComponent implements OnInit {

    @Input() type: ConceptClass;
    @Input() readOnly: boolean = false;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<ConceptClass> = new EventEmitter<ConceptClass>()


    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    constructor(
        public service: ConceptClassService,
        private localizationService: LocalizationService,
        private modalService: BsModalService) {
    }

    ngOnInit(): void {
    }

    createAttribute(): void {
        this.onModalStateChange({ state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" });
    }

    editAttribute(attr: AttributeType, e: any): void {
        this.onModalStateChange({ state: GeoObjectTypeModalStates.editAttribute, attribute: attr, termOption: "" });
    }

    removeAttributeType(attr: AttributeType, e: any): void {
        let confirmBsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });
        confirmBsModalRef.content.message = this.localizationService.decode("confirm.modal.verify.delete") + "[" + attr.label.localizedValue + "]";
        confirmBsModalRef.content.data = { attributeType: attr, geoObjectType: this.type };
        confirmBsModalRef.content.submitText = this.localizationService.decode("modal.button.delete");
        confirmBsModalRef.content.type = ModalTypes.danger;

        confirmBsModalRef.content.onConfirm.subscribe(data => {
            this.service.deleteAttributeType(this.type.code, attr.code).then(() => {
                this.type.attributes.splice(this.type.attributes.indexOf(attr), 1);

                this.typeChange.emit(this.type);
            }).catch((err: HttpErrorResponse) => {
                this.onError.emit(err);
            });
        });
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
    }

    onTypeChange(): void {
        this.typeChange.emit(this.type);
    }

    update(): void {
        this.service.apply(this.type).then(type => {
            this.typeChange.emit(this.type);
        }).catch((err: HttpErrorResponse) => {
            this.onError.emit(err);
        });
    }

    close(): void {
        if (this.type.oid != null) {
            this.service.unlock(this.type.oid).then(() => {
                this.onCancel.emit();
            }).catch((err: HttpErrorResponse) => {
                this.onError.emit(err);
            });
        } else {
            this.onCancel.emit();
        }
    }


}
