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
import { SourceService } from "@registry/service/source.service";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";

@Component({
    selector: "source-field",
    templateUrl: "./source-field.component.html",
    styleUrls: []
})
export class SourceFieldComponent {

    @Input() name: string;
    @Input() disabled: boolean = false;
    @Input() customStyles: string = "";
    @Input() classNames: string = "";
    @Input() container: string = null;

    @Input() value: string = null;

    @Output() valueChange = new EventEmitter<string>();

    loading: boolean = false;
    text: string = "";

    typeahead: Observable<string[]> = null;
    subscription: Subscription = null;


    constructor(
        private service: SourceService) { }

    ngOnInit(): void {
        this.text = this.value;

        this.typeahead = new Observable((observer: Observer<string[]>) => {
            this.service.search(this.text).then(results => {
                observer.next(results.map(s => s.code));
            });
        });
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
        }
    }


    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: string = match.item;

            this.setValue(item);
        } else if (this.value != null) {
            this.setValue(null);
        }
    }

    setValue(value: string): void {
        this.value = value;
        this.valueChange.emit(this.value);
    }

    onTextChange(): void {
        if (this.value != null && (this.text == null || this.text.length === 0)) {
            this.setValue(null);
        }
    }

}