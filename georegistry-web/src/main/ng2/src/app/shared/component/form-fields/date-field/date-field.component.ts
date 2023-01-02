import { Component, Input, Output, EventEmitter, ChangeDetectorRef, ViewChild } from "@angular/core";
import { LocalizationService } from "@shared/service/localization.service";
import { DateService } from "@shared/service/date.service";
import { PRESENT } from "@shared/model/date";

import { BsDatepickerConfig, BsDatepickerDirective } from "ngx-bootstrap/datepicker";


@Component({
    selector: "date-field",
    templateUrl: "./date-field.component.html",
    styleUrls: ["./date-field.css"]
})
export class DateFieldComponent {

    @ViewChild(BsDatepickerDirective, { static: false }) datepicker: BsDatepickerDirective;

    @Input() allowFutureDates: boolean = true;
    @Input() allowInfinity: boolean = false;
    @Input() inputName: string = this.idGenerator();
    @Input() classNames: string = "";
    @Input() customStyles: string = "";
    @Input() localizeLabelKey: string = ""; // localization key used to localize in the component template
    @Input() label: string = ""; // raw string input
    @Input() disable: boolean = false;
    @Input() required: boolean = false;
    @Input() placement: string = "bottom";
    @Input() oldDate: string = null;
    @Input() title: string = "";
    @Input() placeholder: string = "YYYY-MM-DD";

    _value: Date;
    // eslint-disable-next-line accessor-pairs
    @Input() set value(value: string | number | Date) {
        if (value) {
            this.setValue(value || null);
        }
    }

    @Output() public valueChange = new EventEmitter<string>();

    /* You can pass a function in with (change)='function()' */
    @Output() public change = new EventEmitter<any>();

    today: Date = new Date();
    message: string;
    returnFocusToInput: boolean = false;
    valueIsPresent: boolean = false;

    @Input() valid: boolean = true;
    @Output() public validChange = new EventEmitter<boolean>();

    constructor(private localizationService: LocalizationService, private bsDatepickerConfig: BsDatepickerConfig, private changeDetectorRef: ChangeDetectorRef, private dateService: DateService) {
        this.bsDatepickerConfig.dateInputFormat = "YYYY-MM-DD";
    }

    private setValue(value: string | number | Date): void {
        // @ts-ignore
        if (!isNaN(value)) {
            // @ts-ignore
            value = parseInt(value);
        }

        if (value && typeof value === "string") {
            let date = new Date(+value.split("-")[0], +value.split("-")[1] - 1, +value.split("-")[2]);

            if (value === PRESENT) {
                this.valueIsPresent = true;
            }

            this._value = date;
        } else if (value && typeof value === "number") {
            // Custom attributes of date type come through as UTC time
            this._value = new Date(new Date(value).getUTCFullYear(), new Date(value).getUTCMonth(), new Date(value).getUTCDate());
        } else {
            this._value = null;
        }
    }

    public getValue(): Date {
        return this._value;
    }

    public setInvalid(message: string) {
        this.valid = false;
        this.message = message;
    }

    idGenerator() {
        let S4 = function() {
            return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
        };
        return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
    }

    isEqual(date1: Date, date2: Date): boolean {
        let equal = false;

        if (date1 && date2) {
            // if(date1.toISOString().substr(0, 10) === PRESENT && date2.toISOString().substr(0, 10) === PRESENT){
            if (date1.getTime() === date2.getTime()) {
                return true;
            }
        }

        return equal;
    }

    toggleInfinity(): void {
        let date = this.getValue();

        if (date && this.isEqual(date, this.dateService.getPresentDate())) {
            this.setValue(null); // clear the date picker
            this.valueChange.emit(null);
            this.valueIsPresent = false;
        } else {
            this.setValue(PRESENT);
            this.valueChange.emit(this.dateService.getDateString(this.getValue()));
            this.valueIsPresent = true;
        }

        this.change.emit();
    }

    toggle(event: Date): void {
        setTimeout(() => {
            let validity = this.dateService.validateDate(event, this.required, this.allowFutureDates);
            this.valid = validity.valid;
            this.message = validity.message;

            if (this.valid) {
                // Must adhere to the ISO 8601 format
                let formattedDate = this.dateService.getDateString(event);

                if (formattedDate === PRESENT) {
                    this.valueIsPresent = true;
                } else {
                    this.valueIsPresent = false;
                }

                this.valueChange.emit(formattedDate);
            } else {
                // hack to avoid ngx-datepicker from putting "invalid date" in the input
                this.setValue(null);

                this.valueChange.emit(null);
            }

            this.change.emit();
            this.validChange.emit(this.valid);
        }, 0);
    }

}
