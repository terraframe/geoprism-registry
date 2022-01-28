import { Component, Input, Output, EventEmitter, OnInit } from "@angular/core";
import { Classification } from "@registry/model/classification-type";
import { AttributeType } from "@registry/model/registry";
import { ClassificationService } from "@registry/service/classification.service";
import { LocalizedValue } from "@shared/model/core";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Observer } from "rxjs";

@Component({
    selector: "classification-field",
    templateUrl: "./classification-field.component.html",
    styleUrls: []
})
export class ClassificationFieldComponent implements OnInit {

    @Input() attributeType: AttributeType;

    @Input() name: string;
    @Input() disabled: boolean = false;
    @Input() customStyles: string = "";
    @Input() classNames: string = "";

    @Input() value: { code: string, label: LocalizedValue } = null;

    @Output() valueChange = new EventEmitter<{ code: string, label: LocalizedValue }>();

    loading: boolean = false;
    text: string = "";

    typeahead: Observable<Object> = null;

    constructor(private service: ClassificationService) { }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<Object>) => {
            this.service.search(this.attributeType.classificationType, this.text).then(results => {
                observer.next(results);
            });
        });

        if (this.value != null) {
            this.text = this.value.label.localizedValue;
        }
    }

    typeaheadOnSelect(match: TypeaheadMatch): void {
        if (match != null) {
            const item: Classification = match.item;
            this.text = item.displayLabel.localizedValue;

            if (this.value == null || this.value.code !== item.code) {
                this.setValue({ code: item.code, label: item.displayLabel });
            }
        } else if (this.value != null) {
            this.setValue(null);
        }
    }

    setValue(value: { code: string, label: LocalizedValue }): void {
        this.value = value;
        this.valueChange.emit(this.value);
    }

    onViewTree(): void {
        // Open tree widget model and pre-load it will the currently selected value

    }

}
