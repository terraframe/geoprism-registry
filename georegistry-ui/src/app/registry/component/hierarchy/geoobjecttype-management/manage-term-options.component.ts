import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef } from "@angular/core";
import {
    trigger,
    state,
    style,
    animate,
    transition,
    AnimationEvent
} from "@angular/animations";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { HttpErrorResponse } from "@angular/common/http";

import { StepConfig, ModalTypes } from "@shared/model/modal";
import { ErrorHandler, ConfirmModalComponent } from "@shared/component";
import { LocalizationService, ModalStepIndicatorService } from "@shared/service";

import { RegistryService } from "@registry/service";
import { Term, ManageGeoObjectTypeModalState, AttributeType } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";

@Component({
    selector: "manage-term-options",
    templateUrl: "./manage-term-options.component.html",
    styleUrls: ["./manage-term-options.css"],
    animations: [
        trigger("toggleInputs", [
            state("none, void",
                style({ opacity: 0 })
            ),
            state("show",
                style({ opacity: 1 })
            ),
            transition("none => show", animate("300ms")),
            transition("show => none", animate("100ms"))
        ]),
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
export class ManageTermOptionsComponent implements OnInit {

    @Input() attribute: AttributeType;

    @Output() attributeChange = new EventEmitter<AttributeType>();
    @Output() stateChange: EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

    message: string = null;
    termOption: Term;
    state: string = "none";
    enableTermOptionForm = false;
    modalStepConfig: StepConfig = {
        steps: [
            { label: this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.attributes"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.edit.attribute"), active: true, enabled: false },
            { label: this.localizeService.decode("modal.step.indicator.manage.term.options"), active: true, enabled: true }
        ]
    };

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private cdr: ChangeDetectorRef,
        private modalService: BsModalService, private localizeService: LocalizationService, private modalStepIndicatorService: ModalStepIndicatorService,
        private registryService: RegistryService) { }

    ngOnInit(): void {
        this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
        this.termOption = {
            code: "",
            label: this.localizeService.create(),
            description: this.localizeService.create()
        }
    }

    ngAfterViewInit() {
        this.state = "show";
        this.cdr.detectChanges();
    }

    ngOnDestroy() {

    }

    handleOnSubmit(): void {

    }

    animate(): void {
        this.state = "none";
    }

    onAnimationDone(event: AnimationEvent): void {
        this.state = "show";
    }

    isValid(): boolean {
        if (this.termOption.code && this.termOption.code.length > 0) {
            // If code has a space
            if (this.termOption.code.indexOf(" ") !== -1) {
                return false;
            }

            // If label is only spaces
            for (let i = 0; i < this.termOption.label.localeValues.length; i++) {
                if (this.termOption.label.localeValues[i].value.replace(/\s/g, "").length === 0) {
                    return false;
                }
            }

            return true;
        } else if (this.termOption.code && this.termOption.code.indexOf(" ") !== -1) {
            return false;
        }

        return false;
    }

    addTermOption(): void {
        this.registryService.addAttributeTermTypeOption(this.attribute.rootTerm.code, this.termOption).then(data => {
            this.attribute.rootTerm.children.push(data);

            this.attributeChange.emit(this.attribute);

            this.clearTermOption();

            this.enableTermOptionForm = false;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    deleteTermOption(termOption: Term): void {
        this.registryService.deleteAttributeTermTypeOption(this.attribute.rootTerm.code, termOption.code).then(data => {
            if (this.attribute.rootTerm.children.indexOf(termOption) !== -1) {
                this.attribute.rootTerm.children.splice(this.attribute.rootTerm.children.indexOf(termOption), 1);
            }

            this.attributeChange.emit(this.attribute);

            this.clearTermOption();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    removeTermOption(termOption: Term): void {
        this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + "[" + termOption.label + "]";
        this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
        this.bsModalRef.content.type = ModalTypes.danger;

        (<ConfirmModalComponent>this.bsModalRef.content).onConfirm.subscribe(data => {
            this.deleteTermOption(termOption);
        });
    }

    editTermOption(termOption: Term): void {
        const state = {
            state: GeoObjectTypeModalStates.editTermOption,
            attribute: this.attribute,
            termOption: JSON.parse(JSON.stringify(termOption))
        };

        this.stateChange.emit(state);
    }

    clearTermOption(): void {
        this.termOption.code = "";
        this.termOption.label = this.localizeService.create();
        this.termOption.description = this.localizeService.create();
    }

    cancelTermOption(): void {
        this.clearTermOption();
        this.enableTermOptionForm = false;
    }

    openAddTermOptionForm(): void {
        this.enableTermOptionForm = true;
    }

    close(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.editAttribute, attribute: this.attribute, termOption: "" });
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
