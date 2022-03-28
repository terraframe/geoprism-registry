import { Component, Input, Output, EventEmitter } from "@angular/core";
import { LayerRecord } from "@registry/model/list-type";

@Component({
    selector: "record-popup",
    templateUrl: "./record-popup.component.html",
    styleUrls: ["./dataset-location-manager.css"]
})
export class RecordPopupComponent {

    @Input() public record: LayerRecord;
    @Input() public canEdit: boolean = false;

    @Output() public edit = new EventEmitter<void>();

    onEdit(): void {
        this.edit.emit();
    }

}
