import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { LocalizationService } from '@shared/service';
import { ModalTypes } from '@shared/model/modal';


@Component( {
    selector: 'confirm-modal',
    templateUrl: './confirm-modal.component.html',
    styleUrls: ['./modal.css']
} )
export class ConfirmModalComponent {
    /*
     * Message
     */
    @Input() message: string = this.localizeService.decode("confirm.modal.default.message");

    @Input() data: any;

    @Input() submitText: string = this.localizeService.decode("modal.button.submit");

    @Input() cancelText: string = this.localizeService.decode("modal.button.cancel");

    @Input() type: ModalTypes = ModalTypes.warning;

    /*
     * Called on confirm
     */
    public onConfirm: Subject<any>;

    constructor( public bsModalRef: BsModalRef, private localizeService: LocalizationService ) { }

    ngOnInit(): void {
        this.onConfirm = new Subject();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onConfirm.next( this.data );
    }
}
