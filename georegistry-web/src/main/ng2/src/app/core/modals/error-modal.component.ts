import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { LocalizationService } from '../service/localization.service';

@Component( {
    selector: 'error-modal',
    templateUrl: './error-modal.component.html',
    styleUrls: ['./error-modal.css']
} )
export class ErrorModalComponent {
    /*
     * Message
     */
    @Input() message: string =  this.localizeService.decode("error.modal.default.message");

    constructor( public bsModalRef: BsModalRef, private localizeService: LocalizationService ) { }
}
