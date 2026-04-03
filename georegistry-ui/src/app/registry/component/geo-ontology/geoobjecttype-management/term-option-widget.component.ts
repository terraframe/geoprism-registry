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
