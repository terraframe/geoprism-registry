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

import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, OnInit } from "@angular/core";
import { ObjectClass, ObjectOverTime } from "@registry/model/object-class";
import { DateTextComponent } from "@shared/component/date-text/date-text.component";
import { NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault, NgTemplateOutlet } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { DateBoundary } from "../geoobject-shared-attribute-editor/stability-period.component";
import { DateFieldComponent, LocalizeComponent } from "@shared/component";
import { ConvertKeyLabel } from "@shared/component/localize/convert-key-label.component";

@Component({
    selector: "object-panel",
    templateUrl: "./object-panel.component.html",
    styleUrls: ["./object-panel.css"],
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, NgTemplateOutlet, NgSwitch, NgSwitchCase, NgSwitchDefault, DateTextComponent, DateFieldComponent, LocalizeComponent, ConvertKeyLabel]
})
export class ObjectPanelComponent implements OnChanges, OnInit {
    @Input() type: ObjectClass;
    @Input() object: ObjectOverTime;

    @Output() close = new EventEmitter<void>();

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['object']) {
            // Do something
        }
    }

    ngOnInit(): void {

        const boundaries: DateBoundary[] = [];

        this.type.attributes.filter(a => a.isChangeOverTime).forEach(attribute => {
            const attributeOverTime = this.object.data[attribute.code];

            if (attributeOverTime != null) {
                attributeOverTime.values.forEach(period => {

                    if (period.startDate != null && period.endDate != null) {
                        let startIndex = boundaries.findIndex(boundary => period.startDate === boundary.date);

                        if (startIndex !== -1) {
                            boundaries[startIndex].isStart = true;
                        } else {
                            boundaries.push({ date: period.startDate, isStart: true, isEnd: false });
                        }

                        let endIndex = boundaries.findIndex(boundary => period.endDate === boundary.date);
                        if (endIndex !== -1) {
                            boundaries[endIndex].isEnd = true;
                        } else {
                            boundaries.push({ date: period.endDate, isStart: false, isEnd: true });
                        }
                    }
                });
            }
        });

        console.log(boundaries);
    }


    onClose(): void {
        this.close.emit();
    }

}
