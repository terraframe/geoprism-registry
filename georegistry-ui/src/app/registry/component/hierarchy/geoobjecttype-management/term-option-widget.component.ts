import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectorRef } from "@angular/core";
import {
    trigger,
    state,
    style,
    animate,
    transition,
    AnimationEvent
} from "@angular/animations";
import { BsModalRef } from "ngx-bootstrap/modal";

import { AttributeType, ManageGeoObjectTypeModalState } from "@registry/model/registry";
import { GeoObjectTypeModalStates } from "@registry/model/constants";

@Component({
    selector: "term-option-widget",
    templateUrl: "./term-option-widget.component.html",
    styleUrls: ["./term-option-widget.css"],
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
export class TermOptionWidgetComponent implements OnInit {

    @Input() attribute: AttributeType = null;

    @Output() stateChange : EventEmitter<ManageGeoObjectTypeModalState> = new EventEmitter<ManageGeoObjectTypeModalState>();

    message: string = null;
    state: string = "none";

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void {

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

    // isValid(): boolean {
    //     if(this.termOption.code && this.termOption.code.length > 0 && this.termOption.label && this.termOption.label.length > 0){

    //         // If code has a space
    //         if(this.termOption.code.indexOf(" ") !== -1){
    //             return false;
    //         }

    //         // If label is only spaces
    //         if(this.termOption.label.replace(/\s/g, '').length === 0) {
    //             return false
    //         }

    //         return true;
    //     }
    //     else if(this.termOption.code && this.termOption.code.indexOf(" ") !== -1){
    //         return false;
    //     }

    //     return false
    // }

    openAddTermOptionForm(): void {
        this.stateChange.emit({ state: GeoObjectTypeModalStates.manageTermOption, attribute: this.attribute, termOption: "" });
    }

}