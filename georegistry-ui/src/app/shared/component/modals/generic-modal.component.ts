import { Component } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { LocalizationService } from "@shared/service/localization.service";

export class GenericButton {

    label: string;
    onClick: Function;
    shouldClose: boolean;
    class: string;

}

@Component({
    selector: "generic-modal",
    templateUrl: "./generic-modal.component.html",
    styleUrls: ["./modal.css"]
})
export class GenericModalComponent {

    /*
     * Message
     */
    message: string = this.localizeService.decode("confirm.modal.default.message");

    buttons: GenericButton[] = [];

    data: any;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) { }

    init(message: string, buttons: GenericButton[]): void {
        this.message = message;
        this.buttons = buttons;
    }

    onClick(button: GenericButton): void {
        if (button.shouldClose) {
            this.bsModalRef.hide();
        }

        button.onClick(this.data);
    }

}
