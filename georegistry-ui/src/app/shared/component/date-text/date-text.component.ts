import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { DateService } from "../../service";

@Component({
    selector: "date-text",
    templateUrl: "./date-text.component.html",
    styleUrls: []
})
export class DateTextComponent implements OnChanges {

    @Input() date: string | Date | number = '';

    formattedDate: string = '';

    constructor(private service: DateService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['date'] != null) {
            this.formattedDate = this.service.formatDateForDisplay(changes['date'].currentValue);
        }
    }

}
