import { Component, Input } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { LocalizationService } from "@shared/service/localization.service";

@Component({
    selector: "error-modal",
    templateUrl: "./localization-feedback-modal.component.html",
    styleUrls: ["./localization-feedback-modal.css"]
})
export class LocalizationFeedbackModalComponent {

    /*
     * Message
     */
    @Input() message: string = this.localizeService.decode("error.modal.default.message");

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) { }

}
