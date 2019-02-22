import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { HierarchyType } from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';
import { LocalizationService } from '../../../core/service/localization.service';


@Component( {
    selector: 'create-hierarchy-type-modal',
    templateUrl: './create-hierarchy-type-modal.component.html',
    styleUrls: []
} )
export class CreateHierarchyTypeModalComponent implements OnInit {

    hierarchyType: HierarchyType;

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onHierarchytTypeCreate: Subject<HierarchyType>;

    constructor( private lService: LocalizationService, private hierarchyService: HierarchyService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onHierarchytTypeCreate = new Subject();

        this.hierarchyType = {
            "code": "",
            "label": this.lService.create(),
            "description": this.lService.create(),
            "rootGeoObjectTypes": []
        };
    }

    handleOnSubmit(): void {
        this.message = null;

        this.hierarchyService.createHierarchyType( JSON.stringify( this.hierarchyType ) ).then( data => {
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

            console.log( this.message );
        }
    }

}
