import { Component, Input } from '@angular/core';
import { LocalizationService } from '@shared/service';

@Component( {
    selector: 'error-message',
    templateUrl: './error-message.component.html',
    styleUrls: ['./error-message.css']
} )
export class ErrorMessageComponent {
    /*
     * Message
     */
    @Input() message: string =  this.localizeService.decode("error.modal.default.message");

    constructor( private localizeService: LocalizationService ) { }
}
