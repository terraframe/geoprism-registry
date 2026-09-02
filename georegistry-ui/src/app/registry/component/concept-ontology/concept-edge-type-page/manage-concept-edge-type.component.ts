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
import { HttpErrorResponse } from "@angular/common/http";
import { ConceptEdgeTypeService } from "@registry/service/concept-edge-type.service";
import { LocalizedTextComponent } from "../../form-fields/localized-text/localized-text.component";
import { ConvertKeyLabel } from "@shared/component/localize/convert-key-label.component";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { FormsModule } from "@angular/forms";
import { NgIf, NgFor } from "@angular/common";
import { ConceptEdgeType, ConceptClass } from "@registry/model/object-class";

@Component({
    selector: "manage-concept-edge-type",
    templateUrl: "./manage-concept-edge-type.component.html",
    styleUrls: ["./manage-concept-edge-type.css"],
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
    imports: [NgIf, FormsModule, LocalizeComponent, NgFor, ConvertKeyLabel, LocalizedTextComponent]
})
export class ManageConceptEdgeTypeComponent implements OnInit {
    @Input() type: ConceptEdgeType = null;
    @Input() conceptClasses: ConceptClass[] = [];

    @Input() readOnly: boolean = false;
    @Input() isNew: boolean = false;

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<ConceptEdgeType> = new EventEmitter<ConceptEdgeType>()


    constructor(public service: ConceptEdgeTypeService) {
    }

    ngOnInit(): void {

    }

    apply(): void {

        this.service.apply(this.type).then(type => {
            this.typeChange.emit(type);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    close(): void {
        this.onCancel.emit();
    }

    error(err: HttpErrorResponse): void {
        this.onError.emit(err);
    }

}
