import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from "@angular/core";
import { LayerRecord } from "@registry/model/list-type";

@Component({
    selector: "record-panel",
    templateUrl: "./record-panel.component.html",
    styleUrls: ["./dataset-location-manager.css"]
})
export class RecordPanelComponent {
    @Input() record: LayerRecord
    @Output() close = new EventEmitter<void>();

    onClose():void {
        this.close.emit();
    }
}
