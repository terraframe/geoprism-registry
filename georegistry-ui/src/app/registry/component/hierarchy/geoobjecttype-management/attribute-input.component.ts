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

import { Component, Input, Output, EventEmitter, ChangeDetectorRef, OnChanges, SimpleChanges } from "@angular/core";
import {
    trigger,
    state,
    style,
    animate,
    transition
} from "@angular/animations";

import { GeoObjectType, AttributeType } from "@registry/model/registry";
import { ClassificationTypeService } from "@registry/service/classification-type.service";
import { ClassificationType } from "@registry/model/classification-type";

@Component({
    selector: "attribute-input",
    templateUrl: "./attribute-input.component.html",
    styleUrls: ["./attribute-input.css"],
    animations: [
        trigger("toggleInputs", [
            state("none, void",
                style({ opacity: 0 })
            ),
            state("show",
                style({ opacity: 1 })
            ),
            transition("none => show", animate("300ms"))
            // transition('show => none', animate('100ms'))
        ])
    ]
})
export class AttributeInputComponent implements OnChanges {

    @Input() isNew: boolean = false;
    @Input() excludeDescription: boolean = false;
    @Input() type: string = null;
    @Input() geoObjectType: GeoObjectType;
    @Input() attribute: AttributeType;
    @Output() attributeChange = new EventEmitter<AttributeType>();
    message: string = null;

    state: string = "none";
    classifications: ClassificationType[] = [];

    constructor(private service: ClassificationTypeService, private cdr: ChangeDetectorRef) { }

    ngOnInit(): void {
    }

    ngAfterViewInit() {
        this.state = "show";
        this.cdr.detectChanges();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.type != null && this.attribute.type === "classification") {
            this.service.page({}).then((page) => {
                this.classifications = page.resultSet;
            });
        }
    }

    ngOnDestroy() {

    }

    handleOnSubmit(): void {

    }

    toggleIsUnique(): void {
        this.attribute.unique = !this.attribute.unique;
    }

    animate(): void {
        this.state = "none";
    }

    onAnimationDone(event: AnimationEvent): void {
        this.state = "show";
    }

    isValid(): boolean {
        if (this.attribute.code) {
            // if code has a space
            if (this.attribute.code.indexOf(" ") !== -1) {
                return false;
            }

            if (this.attribute.label.localeValues[0].value.length === 0) {
                return false;
            }

            if (this.type === "float" && (this.attribute.precision == null || this.attribute.precision.toString() === "")) {
                return false;
            }

            if (this.type === "float" && (this.attribute.scale == null || this.attribute.scale.toString() === "")) {
                return false;
            }

            if (this.type === "classification" && (this.attribute.classificationType == null || this.attribute.classificationType.length === 0)) {
                return false;
            }

            return true;
        }

        return false;
    }

}
