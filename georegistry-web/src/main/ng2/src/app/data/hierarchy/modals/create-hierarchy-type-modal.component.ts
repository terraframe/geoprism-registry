import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { HierarchyType } from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';


@Component( {
    selector: 'create-hierarchy-type-modal',
    templateUrl: './create-hierarchy-type-modal.component.html',
    styleUrls: []
} )
export class CreateHierarchyTypeModalComponent implements OnInit {

    hierarchyType: HierarchyType = {"code":"","localizedLabel":"","localizedDescription":"","rootGeoObjectTypes":[]};

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onHierarchytTypeCreate: Subject<HierarchyType>;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onHierarchytTypeCreate = new Subject();
    }

    handleOnSubmit(): void {
        this.message = null;
        
        this.hierarchyService.createHierarchyType( JSON.stringify(this.hierarchyType) ).then( data => {
        	this.onHierarchytTypeCreate.next( data );
        	this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
