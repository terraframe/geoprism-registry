import { Component, Input, ViewChild } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

export interface ActionDetailComponent {
    endEdit(): void;
}

@Component( {
    selector: 'action-detail-modal',
    templateUrl: './action-detail-modal.component.html',
    styleUrls: []
} )
export class ActionDetailModalComponent {

    action: any;

    @ViewChild( "cuDetail" ) cuDetail: ActionDetailComponent;
    @ViewChild( "arDetail" ) arDetail: ActionDetailComponent;
    @ViewChild( "spDetail" ) spDetail: ActionDetailComponent;

    @Input()
    set curAction( action: any ) {
        this.action = action;
    }

    /*
     * Called on confirm
     */
    public onFormat: Subject<any>;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
    }

    cancel(): void {
        if ( this.cuDetail != null ) {
            this.cuDetail.endEdit();
        }

        if ( this.arDetail != null ) {
            this.arDetail.endEdit();
        }

        if ( this.spDetail != null ) {
            this.spDetail.endEdit();
        }

        this.bsModalRef.hide();
    }

    confirm(): void {
        this.bsModalRef.hide();
    }
}
