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
import { ControlContainer, NgForm } from "@angular/forms";


@Component({
    selector: "multi-select-field",
    templateUrl: "./multi-select-field.component.html",
    styleUrls: [],
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }],
})
export class MultiSelectFieldComponent {

    @Input() container: any;

    @Input() noValueText: string = "Select options";

    @Input() disabled: boolean = false;

    @Input() options: { value: string, label: string }[] = [];

    @Input() value: string[] = [];

    @Output() valueChange = new EventEmitter<string[]>();

    buildButtonLabel(showAll: boolean = false): string {

        let labels: string[] = [];

        if (this.options != null) {
            for (let i = 0; i < this.value.length; ++i) {
                const option = this.options.find(t => t.value === this.value[i]);

                if (option != null) {
                    labels.push(option.label);
                }
            }
        }

        if (showAll) {
            // if (labels.length == 0) return this.lService.decode("synchronization.config.none");
            return labels.sort().join(", ");
        } else {
            return this.noValueText;
        }
    }

    clickOption($event, option: { value: string, label: string }) {
        $event.stopPropagation();

        if (this.value.indexOf(option.value) == -1) {
            this.value.push(option.value);
        }
        else {
            this.value.splice(this.value.indexOf(option.value), 1);
        }

        this.valueChange.emit(this.value);
    }

    strArrayContains(oValue: string): boolean {
        return this.value.indexOf(oValue) != -1;
    }
}
