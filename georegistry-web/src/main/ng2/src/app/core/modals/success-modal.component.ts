import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { LocalizationService } from '../service/localization.service';

@Component( {
    selector: 'success-modal',
    templateUrl: './success-modal.component.html',
    styleUrls: ['./success-modal.css']
} )
export class SuccessModalComponent {
    /*
     * Message
     */
    @Input() message: string = this.message ? this.message : this.localizeService.decode("success.modal.default.message");

    constructor( public bsModalRef: BsModalRef, private localizeService: LocalizationService ) { }
}
