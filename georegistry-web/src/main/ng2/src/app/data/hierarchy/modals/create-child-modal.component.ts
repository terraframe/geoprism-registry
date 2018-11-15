import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';

import { TreeEntity, HierarchyType, GeoObjectType, HierarchyNode } from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';


@Component( {
    selector: 'create-child-modal',
    templateUrl: './create-child-modal.component.html',
    styleUrls: []
} )
export class CreateChildModalComponent implements OnInit {

	 /*
     * parent id of the node being created
     */
    public parent: TreeNode;

    public hierarchyType: HierarchyType;

    public nodes: HierarchyNode[];

    public allGeoObjectTypes: GeoObjectType[];

    public selectedGeoObjectType: GeoObjectType;

    private selectUndefinedOptionValue: any;

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onNodeChange: Subject<HierarchyType>;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef ) {
    }

    ngOnInit(): void {
        this.onNodeChange = new Subject();
    }
    
    onSelect(value: string): void {
    	this.allGeoObjectTypes.forEach(gObj => {
    		if(gObj.code === value){
    			this.selectedGeoObjectType = gObj;
    		}
    	})
    }
    
    handleOnSubmit(): void {
        this.message = null;

        this.hierarchyService.addChildToHierarchy( this.hierarchyType.code, this.parent.data === "ROOT" ? this.parent.data.geoObjectType : "ROOT", this.selectedGeoObjectType.code ).then( data => {
            this.onNodeChange.next( data );
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
