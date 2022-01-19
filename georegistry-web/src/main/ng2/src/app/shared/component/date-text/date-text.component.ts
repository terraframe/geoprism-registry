import { Component, Input, OnChanges, SimpleChanges } from "@angular/core";
import { DateService } from "@shared/service";

@Component({
    selector: "date-text",
    templateUrl: "./date-text.component.html",
    styleUrls: []
})
export class DateTextComponent implements OnChanges {

    @Input() date: string = null;
    formattedDate: string = null;

    constructor(private service: DateService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.date != null) {
            this.formattedDate = this.service.formatDateForDisplay(changes.date.currentValue);
        }
    }

}
