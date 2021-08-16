import { Component, Input, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { LocalizationService } from "@shared/service";

@Component({
    selector: "success-modal",
    templateUrl: "./success-modal.component.html",
    styleUrls: ["./success-modal.css"]
})
export class SuccessModalComponent implements OnInit {

    /*
     * Message
     */
    @Input() message: string;

    @Input() submitText: string = this.localizeService.decode("modal.button.close");

    public onConfirm: Subject<any>;

    // eslint-disable-next-line no-useless-constructor
    constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) { }

    ngOnInit(): void {
        this.message = this.message ? this.message : this.localizeService.decode("success.modal.default.message");
        this.onConfirm = new Subject();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next();
    }

}
