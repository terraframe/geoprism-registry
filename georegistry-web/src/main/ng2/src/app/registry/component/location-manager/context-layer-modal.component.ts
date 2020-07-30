import { Component, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';

import { MasterList, ContextLayer, ContextLayerGroup } from '@registry/model/registry';


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

    groupHasContextLayers(group:string): boolean {
		let hasCLayers = false;
		this.contextLayerGroups.forEach(cLayerGroup => {
			if(cLayerGroup.oid === group && cLayerGroup.contextLayers.length > 0){
				hasCLayers = true;
			}
		});
		
		return hasCLayers;
	}

    confirm(): void {
        this.onSubmit.next( this.contextLayerGroups );
        this.bsModalRef.hide();
    }

}
