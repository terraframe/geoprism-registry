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

import { Component, Input, Output, EventEmitter } from "@angular/core";

import { GeoRegistryConfiguration } from "@core/model/core"; import { environment } from 'src/environments/environment';

@Component({
    selector: "boolean-field",
    templateUrl: "./boolean-field.component.html",
    styleUrls: ["./boolean-field.css"]
})
export class BooleanFieldComponent {

    @Input() value: boolean = false;
    @Output() public valueChange = new EventEmitter<boolean>();

    @Input() localizeLabelKey: string = ""; // localization key used to localize in the component template
    @Input() label: string = ""; // raw string input

    @Input() disable: boolean = false;

    /* You can pass a function in with (change)='function()' */
    @Output() public change = new EventEmitter<any>();

    // eslint-disable-next-line no-useless-constructor
    constructor() { }

    toggle(): void {
        if (!this.disable) {
            this.value = !this.value;

            this.valueChange.emit(this.value);
            this.change.emit(this.value);
        }
    }
}