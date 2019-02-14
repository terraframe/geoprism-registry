import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { HierarchyType } from '../hierarchy';
import { GeoObjectType, TreeEntity } from '../../../model/registry';
import { HierarchyService } from '../../../service/hierarchy.service';
import { RegistryService } from '../../../service/registry.service';


@Component( {
    selector: 'create-geoobjtype-modal',
    templateUrl: './create-geoobjtype-modal.component.html',
    styleUrls: []
} )
export class CreateGeoObjTypeModalComponent implements OnInit {

	public hierarchyType: HierarchyType;

	geoObjectType: GeoObjectType = {"code":"","localizedLabel":"","localizedDescription":"", "geometryType":"POINT", "isLeaf":false, "attributes":[]};

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onGeoObjTypeCreate: Subject<GeoObjectType>;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private registryService: RegistryService ) {
    	
    }

    ngOnInit(): void {
        this.onGeoObjTypeCreate = new Subject();
    }
    
    handleOnSubmit(): void {
        this.message = null;

        this.registryService.createGeoObjectType( JSON.stringify(this.geoObjectType) ).then( data => {
            this.onGeoObjTypeCreate.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
        
    }
    
    toggleIsLeaf(): void {
    	this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }
}
