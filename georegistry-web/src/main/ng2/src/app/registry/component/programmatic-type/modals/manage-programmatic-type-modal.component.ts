import { Component, OnInit } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { ConfirmModalComponent, ErrorHandler } from "@shared/component";
import { ProgrammaticType } from "@registry/model/programmatic-type";
import { AttributeType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { ProgrammaticTypeService } from "@registry/service/programmatic-type.service";
import { GeoObjectTypeModalStates } from "@registry/model/constants";
import { ModalTypes } from "@shared/model/modal";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "manage-programmatic-type-modal",
    templateUrl: "./manage-programmatic-type-modal.component.html",
    styleUrls: ["./manage-programmatic-type-modal.css"],
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
export class ManageProgrammaticTypeModalComponent implements OnInit {

    modalState: ManageGeoObjectTypeModalState = { state: GeoObjectTypeModalStates.manageGeoObjectType, attribute: "", termOption: "" };

    message: string = null;
    modalStateSubscription: Subscription;
    type: ProgrammaticType;
    public onProgrammaticTypeChange: Subject<ProgrammaticType>;
    readOnly: boolean = false;

    constructor(private service: ProgrammaticTypeService, private localizationService: LocalizationService, private modalService: BsModalService, public bsModalRef: BsModalRef) {
    }

    ngOnInit(): void {
        this.onProgrammaticTypeChange = new Subject();
    }

    ngOnDestroy() {
        this.modalStateSubscription.unsubscribe();
    }

    init(type: ProgrammaticType, readOnly: boolean) {
        this.type = type;
        this.readOnly = readOnly;
    }

    createAttribute(): void {
        this.modalState = { state: GeoObjectTypeModalStates.defineAttribute, attribute: "", termOption: "" };
    }

    editAttribute(attr: AttributeType, e: any): void {
        this.modalState = { state: GeoObjectTypeModalStates.editAttribute, attribute: attr, termOption: "" };
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
            this.service.removeAttributeType(this.type.code, attr.code).then(() => {

                this.type.attributes.splice(this.type.attributes.indexOf(attr), 1);

                this.onProgrammaticTypeChange.next(this.type);
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        });
    }

    onModalStateChange(state: any): void {
    }

    handleChange(data: any): void {
        this.onProgrammaticTypeChange.next(data);
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
