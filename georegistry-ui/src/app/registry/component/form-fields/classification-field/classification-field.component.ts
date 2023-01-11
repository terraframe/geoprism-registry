import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from "@angular/core";
import { Classification } from "@registry/model/classification-type";
import { ClassificationService } from "@registry/service/classification.service";
import { LocalizedValue } from "@core/model/core";
import { BsModalService } from "ngx-bootstrap/modal";
import { TypeaheadMatch } from "ngx-bootstrap/typeahead";
import { Observable, Observer, Subscription } from "rxjs";
import { ClassificationFieldModalComponent } from "./classification-field-modal.component";

@Component({
    selector: "classification-field",
    templateUrl: "./classification-field.component.html",
    styleUrls: []
})
export class ClassificationFieldComponent implements OnInit, OnDestroy {

    @Input() classificationType: string;
    @Input() rootCode: string;

    @Input() name: string;
    @Input() disabled: boolean = false;
    @Input() customStyles: string = "";
    @Input() classNames: string = "";
    @Input() container: string = null;

    @Input() value: { code: string, label: LocalizedValue } = null;

    @Output() valueChange = new EventEmitter<{ code: string, label: LocalizedValue }>();

    loading: boolean = false;
    text: string = "";

    typeahead: Observable<any> = null;
    subscription: Subscription = null;

    constructor(
        private modalService: BsModalService,
        private service: ClassificationService) { }

    ngOnInit(): void {
        this.typeahead = new Observable((observer: Observer<any>) => {
            this.service.search(this.classificationType, this.rootCode, this.text).then(results => {
                observer.next(results);
            });
        });

        if (this.value != null) {
            this.text = this.value.label.localizedValue;
        }
    }

    ngOnDestroy(): void {
        if (this.subscription != null) {
            this.subscription.unsubscribe();
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
        const bsModalRef = this.modalService.show(ClassificationFieldModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true
        });
        this.subscription = bsModalRef.content.init(this.classificationType, this.rootCode, this.disabled, this.value, classification => {
            this.text = classification.displayLabel.localizedValue;
            this.setValue({ code: classification.code, label: classification.displayLabel });
        });
    }

    onTextChange(): void {
        if (this.value != null && (this.text == null || this.text.length === 0)) {
            this.setValue(null);
        }
    }

}
