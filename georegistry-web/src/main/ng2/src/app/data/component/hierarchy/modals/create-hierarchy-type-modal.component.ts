import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from "@angular/common/http";

import { HierarchyType } from '../../../model/hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';
import { LocalizationService } from '../../../../shared/service/localization.service';


@Component( {
    selector: 'create-hierarchy-type-modal',
    templateUrl: './create-hierarchy-type-modal.component.html',
    styleUrls: []
} )
export class CreateHierarchyTypeModalComponent implements OnInit {

    hierarchyType: HierarchyType;

    message: string = null;

    edit: boolean = false;

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

        if ( this.edit ) {
            this.hierarchyService.updateHierarchyType( JSON.stringify( this.hierarchyType ) ).then( data => {
                this.onHierarchytTypeCreate.next( data );
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse) => {
                this.error( err );
            } );
        }
        else {
            this.hierarchyService.createHierarchyType( JSON.stringify( this.hierarchyType ) ).then( data => {
                this.onHierarchytTypeCreate.next( data );
                this.bsModalRef.hide();
            } ).catch(( err: HttpErrorResponse) => {
                this.error( err );
            } );
        }
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );

            console.log( this.message );
        }
    }

}
