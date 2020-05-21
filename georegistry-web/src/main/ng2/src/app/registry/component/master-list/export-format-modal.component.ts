import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';


@Component( {
    selector: 'export-format-modal',
    templateUrl: './export-format-modal.component.html',
    styleUrls: []
} )
export class ExportFormatModalComponent {

    format: string;

    /*
     * Called on confirm
     */
    public onFormat: Subject<any>;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onFormat = new Subject();
    }

    confirm(): void {
        this.bsModalRef.hide();
        this.onFormat.next( this.format );
    }
}
