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

import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { TypeaheadMatch, TypeaheadModule } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";
import { NgClass } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ConceptObjectService } from "@registry/service/concept-object.service";
import { ObjectOverTime } from "@registry/model/object-class";
import { AttributedType, AttributeType } from "@registry/model/registry";
import { ClassificationFieldModalComponent } from "./classification-field-modal.component";

@Component({
    selector: "classification-field",
    templateUrl: "./classification-field.component.html",
    styleUrls: [],
    standalone: true,
    imports: [FormsModule, TypeaheadModule, NgClass]
})
export class ClassificationFieldComponent implements OnInit, OnDestroy {

    @Input() type: AttributedType;
    @Input() attribute: AttributeType;

    @Input() name: string;
    @Input() disabled: boolean = false;
    @Input() customStyles: string = "";
    @Input() classNames: string = "";
    @Input() container: string = null;

    @Input() value: { code: string } = null;

    @Output() valueChange = new EventEmitter<{ code: string }>();

    loading: boolean = false;
    text: string = "";

    typeahead: Observable<ObjectOverTime[]> = null;
    subscription: Subscription = null;

    constructor(
        private modalService: BsModalService,
        private service: ConceptObjectService) {

    }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<ObjectOverTime[]>) => {
            this.service.search(this.type, this.attribute, this.text).then(results => {
                observer.next(results);
            });
        });

        if (this.value != null) {
            this.text = this.value.code;
        }
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }

    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: ObjectOverTime = match.item;
            this.text = item.code;

            if (this.value == null || this.value.code !== item.code) {
                this.setValue({ code: item.code });
            }
        } else if (this.value != null) {
            this.setValue(null);
        }
    }

    setValue(value: { code: string }): void {
        this.value = value;
        this.valueChange.emit(this.value);
    }

    onViewTree(): void {
        const bsModalRef = this.modalService.show(ClassificationFieldModalComponent, {
            animated: false, backdrop: true, ignoreBackdropClick: true
        });

        this.subscription = bsModalRef.content.init(this.type, this.attribute, this.disabled, this.value, (classification: ObjectOverTime) => {
            this.text = classification.code;
            this.setValue({ code: classification.code });
        });
    }

    onTextChange(): void {
        if (this.value != null && (this.text == null || this.text.length === 0)) {
            this.setValue(null);
        }
    }

}
