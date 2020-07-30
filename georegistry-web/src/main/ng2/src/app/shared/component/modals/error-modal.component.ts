import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { LocalizationService } from '@shared/service';

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
