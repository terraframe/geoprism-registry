import { Component, OnInit } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { BusinessType } from "@registry/model/business-type";
import { AttributeType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { BusinessTypeService } from "@registry/service/business-type.service";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { ModalTypes } from "@shared/model/modal";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "manage-business-type-modal",
    templateUrl: "./manage-business-type-modal.component.html",
    styleUrls: ["./manage-business-type-modal.css"],
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
export class ManageBusinessTypeModalComponent implements OnInit {

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    message: string = null;
    type: BusinessType;
    public onBusinessTypeChange: Subject<BusinessType>;
    readOnly: boolean = false;

    constructor(public service: BusinessTypeService, private localizationService: LocalizationService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onBusinessTypeChange = new Subject();
    }

    init(type: BusinessType, readOnly: boolean) {
        this.type = type;
        this.readOnly = readOnly;
    }

    createAttribute(): void {
        this.onModalStateChange({ state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" });
    }

    editAttribute(attr: AttributeType, e: any): void {
        this.onModalStateChange({ state: GeoObjectTypeModalStates.editAttribute, attribute: attr, termOption: "" });
    }

    removeAttributeType(attr: AttributeType, e: any): void {
        let confirmBsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        confirmBsModalRef.content.message = this.localizationService.decode("confirm.modal.verify.delete") + "[" + attr.label.localizedValue + "]";
        confirmBsModalRef.content.data = { attributeType: attr, geoObjectType: this.type };
        confirmBsModalRef.content.submitText = this.localizationService.decode("modal.button.delete");
        confirmBsModalRef.content.type = ModalTypes.danger;

        confirmBsModalRef.content.onConfirm.subscribe(data => {
            this.service.deleteAttributeType(this.type.code, attr.code).then(() => {
                this.type.attributes.splice(this.type.attributes.indexOf(attr), 1);

                this.onBusinessTypeChange.next(this.type);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onModalStateChange(state: ManageGeoObjectTypeModalState): void {
        this.modalState = state;
    }

    onTypeChange(): void {
        this.onBusinessTypeChange.next(this.type);
    }

    update(): void {
        this.service.apply(this.type).then(type => {
            this.onBusinessTypeChange.next(type);

            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        if (this.type.oid != null) {
            this.service.unlock(this.type.oid).then(() => {
                this.bsModalRef.hide();
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.bsModalRef.hide();
        }
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}