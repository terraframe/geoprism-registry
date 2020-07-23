import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { TreeNode } from 'angular-tree-component';
import { HttpErrorResponse } from "@angular/common/http";

import { HierarchyType, HierarchyNode } from '../../../model/hierarchy';
import { GeoObjectType } from '../../../model/registry';
import { HierarchyService } from '../../../service/hierarchy.service';


@Component( {
    selector: 'add-child-to-hierarchy-modal',
    templateUrl: './add-child-to-hierarchy-modal.component.html',
    styleUrls: []
} )
export class AddChildToHierarchyModalComponent implements OnInit {

	 /*
     * parent id of the node being created
     */
    public parent: TreeNode;

    public hierarchyType: HierarchyType;

    public nodes: HierarchyNode[];

    public allGeoObjectTypes: GeoObjectType[];

    public selectedGeoObjectType: GeoObjectType;

    private selectUndefinedOptionValue: any;

	private toRoot: boolean = false;

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
        
        let parent = (this.toRoot) ? "ROOT" : this.parent.data.geoObjectType;
        this.hierarchyService.addChildToHierarchy( this.hierarchyType.code, parent, this.selectedGeoObjectType.code ).then( data => {
            this.onNodeChange.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse) => {
            this.error( err );
        } );
        
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( (err.error && (err.error.localizedMessage || err.error.message)) || err.message || "An unspecified error has occurred" );
            
            console.log(this.message);
        }
    }
}
