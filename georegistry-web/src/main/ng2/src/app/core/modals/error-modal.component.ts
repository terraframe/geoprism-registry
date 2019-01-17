import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';


@Component( {
    selector: 'error-modal',
    templateUrl: './error-modal.component.html',
    styleUrls: []
} )
export class ErrorModalComponent {
    /*
     * Message
     */
    @Input() message: string = 'Unable to complete your action';

    constructor( public bsModalRef: BsModalRef ) { }
}
