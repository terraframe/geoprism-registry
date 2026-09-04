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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from "@angular/core";
import {
    trigger,
    style,
    animate,
    transition
} from "@angular/animations";
import { HttpErrorResponse } from "@angular/common/http";
import { ConceptClass, ConceptEdgeType, ConceptSet, ObjectOverTime } from "@registry/model/object-class";
import { LocalizePipe } from "@shared/pipe/localize.pipe";
import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { FormsModule } from "@angular/forms";
import { NgIf, NgFor, NgClass } from "@angular/common";
import { ConceptSetService } from "@registry/service/concept-set.service";
import { ConvertKeyLabel } from "@shared/component/localize/convert-key-label.component";
import { LocalizedTextComponent } from "@registry/component/form-fields/localized-text/localized-text.component";
import { TypeaheadMatch, TypeaheadModule } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";
import { ConceptObjectService } from "@registry/service/concept-object.service";

@Component({
    selector: "manage-concept-set",
    templateUrl: "./manage-concept-set.component.html",
    styleUrls: ["./manage-concept-set.css"],
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
    imports: [NgIf, NgFor, NgClass, FormsModule, LocalizeComponent, LocalizedTextComponent, ConvertKeyLabel, TypeaheadModule]
})
export class ManageConceptSetComponent implements OnInit, OnDestroy {

    @Input() set: ConceptSet = null;
    @Input() readOnly: boolean = false;
    @Input() isNew: boolean = false;
    @Input() conceptClasses: ConceptClass[] = [];
    @Input() conceptEdgeTypes: ConceptEdgeType[] = [];

    @Output() onCancel: EventEmitter<void> = new EventEmitter<void>()
    @Output() onError: EventEmitter<HttpErrorResponse> = new EventEmitter<HttpErrorResponse>()
    @Output() typeChange: EventEmitter<ConceptSet> = new EventEmitter<ConceptSet>()

    text: string = "";
    loading: boolean = false;

    typeahead: Observable<ObjectOverTime[]> = null;

    constructor(
        private service: ConceptSetService,
        private cObjectService: ConceptObjectService) {
    }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<ObjectOverTime[]>) => {
            if (this.set.conceptClasses != null && this.set.conceptClasses.length > 0) {
                this.cObjectService.searchConceptClass(this.set.conceptClasses[0], this.text).then(results => {
                    observer.next(results);
                });
            }
        });

        if (this.set != null) {
            this.text = this.set.rootTerm;
        }
        else {
            this.text = ""
        }
    }

    ngOnDestroy(): void {
    }

    update(): void {
        this.service.apply(this.set).then(type => {
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

    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: ObjectOverTime = match.item;
            this.text = item.code;

            if (this.set.rootTerm == null || this.set.rootTerm !== item.code) {
                this.set.rootTerm = item.code;
            }
        } else if (this.set.rootTerm != null) {
            this.set.rootTerm = null;
        }
    }


    onTextChange(): void {
        if (this.set.rootTerm != null && (this.text == null || this.text.length === 0)) {
            this.set.rootTerm = null;
        }
    }
}
