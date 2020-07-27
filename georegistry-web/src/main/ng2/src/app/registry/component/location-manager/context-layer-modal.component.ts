import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { MasterList, ContextLayer, ContextLayerGroup } from '../../model/registry';


@Component( {
    selector: 'context-layer-modal',
    templateUrl: './context-layer-modal.component.html',
    styleUrls: ['./location-manager.css']
} )
export class ContextLayerModalComponent {

    contextLayerGroups: ContextLayerGroup[];


    /*
     * Called on confirm
     */
    public onSubmit: Subject<any>;

    constructor( public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onSubmit = new Subject();
    }

    confirm(): void {
        this.onSubmit.next( this.contextLayerGroups );
        this.bsModalRef.hide();
    }

}
