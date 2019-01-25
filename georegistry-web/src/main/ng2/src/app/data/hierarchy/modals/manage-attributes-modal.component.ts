import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { ConfirmModalComponent } from '../../../core/modals/confirm-modal.component';

import { GeoObjectType, Attribute, ManageAttributeState } from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';



@Component( {
    selector: 'manage-attributes-modal',
    templateUrl: './manage-attributes-modal.component.html',
    styleUrls: []
} )
export class ManageAttributesModalComponent implements OnInit {

    geoObjectType: GeoObjectType = null;
    message: string = null;
    modalState: ManageAttributeState = {"state":"MANAGE-ATTRIBUTES", "attribute":""};

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onDeleteAttribute: Subject<boolean>;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, public confirmBsModalRef: BsModalRef, private modalService: BsModalService ) {
    }

    ngOnInit(): void {
        this.onDeleteAttribute = new Subject();
    }

    ngOnDestroy(){
      this.onDeleteAttribute.unsubscribe();
    }

    defineAttributeModal(): void {
        this.modalState = {"state":"DEFINE-ATTRIBUTE", "attribute":""};
    }

    editAttribute(attr: Attribute, e: any): void {
        this.modalState = {"state":"EDIT-ATTRIBUTE", "attribute":attr};
    }

    removeAttributeType(attr: Attribute, e: any): void {

      this.confirmBsModalRef = this.modalService.show( ConfirmModalComponent, {
		  animated: true,
		  backdrop: true,
		  ignoreBackdropClick: true,
	  } );
	  this.confirmBsModalRef.content.message = 'Are you sure you want to delete [' + attr.localizedLabel + ']';
      this.confirmBsModalRef.content.data = {'attributeType': attr, 'geoObjectType': this.geoObjectType};

	  ( <ConfirmModalComponent>this.confirmBsModalRef.content ).onConfirm.subscribe( data => {
          this.deleteAttributeType( data.geoObjectType.code, data.attributeType );
	  } );
    }

    deleteAttributeType(geoObjectTypeCode: string, attr: Attribute): void {

          this.hierarchyService.deleteAttributeType( geoObjectTypeCode, attr.name ).then( data => {
            this.onDeleteAttribute.next( data );

            if(data){
              this.geoObjectType.attributes.splice(this.geoObjectType.attributes.indexOf(attr), 1);
            }

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onModalStateChange(state: any): void {
        this.modalState = state;
    }

    close(): void {
        this.bsModalRef.hide();
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
