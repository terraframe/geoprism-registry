import { Component, Input, Output, EventEmitter } from "@angular/core";
import { BusinessObject, BusinessType } from "@registry/model/business-type";

@Component({
    selector: "business-object-panel",
    templateUrl: "./business-object-panel.component.html",
    styleUrls: ["./dataset-location-manager.css"]
})
export class BusinessObjectPanelComponent {

    @Input() type: BusinessType;
    @Input() object: BusinessObject;

    @Output() close = new EventEmitter<void>();

    onClose(): void {
        this.close.emit();
    }

}
