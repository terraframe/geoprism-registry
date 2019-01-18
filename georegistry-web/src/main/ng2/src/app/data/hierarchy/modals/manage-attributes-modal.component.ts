import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs/Subject';
import { TreeNode } from 'angular-tree-component';
import { ConfirmModalComponent } from '../../../core/modals/confirm-modal.component';

import { TreeEntity, HierarchyType, GeoObjectType, Attribute } from '../hierarchy';
import { HierarchyService } from '../../../service/hierarchy.service';
import { GeoObjTypeModalService } from '../../../service/geo-obj-type-modal.service';



@Component( {
    selector: 'manage-attributes-modal',
    templateUrl: './manage-attributes-modal.component.html',
    styleUrls: []
} )
export class ManageAttributesModalComponent implements OnInit {

    geoObjectType: GeoObjectType = null;
    message: string = null;
    modalState: string = "MANAGE-ATTRIBUTES";

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onDeleteAttribute: Subject<boolean>;

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private modalService: BsModalService, private geoObjTypeModalService: GeoObjTypeModalService ) {
      geoObjTypeModalService.modalState.subscribe(state => {
        this.modalState = state;
      });
    }

    ngOnInit(): void {
        this.onDeleteAttribute = new Subject();
    }

    ngOnDestroy(){

    //   this.geoObjTypeModalService.modalStateSource.unsubscribe();
      this.onDeleteAttribute.unsubscribe();
    }

    defineAttributeModal(): void {

        this.geoObjTypeModalService.setState("DEFINE-ATTRIBUTE");

    //   this.bsModalRef = this.modalService.show( DefineAttributeModalComponent, {
    //       animated: true,
    //       backdrop: true,
    //       ignoreBackdropClick: true,
    //       'class': 'upload-modal'
    //   } );
    //   this.bsModalRef.content.geoObjectType = this.geoObjectType;
      
    //   ( <DefineAttributeModalComponent>this.bsModalRef.content ).onAddAttribute.subscribe( data => {
    //       this.geoObjectType.attributes.push(data);
	//   } );

    }

    editAttribute(attr: Attribute, e: any): Attribute {

        return attr;
    }

    removeAttributeType(attr: Attribute, e: any): void {

      this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
		  animated: true,
		  backdrop: true,
		  ignoreBackdropClick: true,
	  } );
	  this.bsModalRef.content.message = 'Are you sure you want to delete [' + attr.localizedLabel + ']';
      this.bsModalRef.content.data = {'attributeType': attr, 'geoObjectType': this.geoObjectType};

	  ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
		  this.deleteAttributeType( data.geoObjectType.code, data.attributeType );
	  } );
    }

    deleteAttributeType(geoObjectTypeCode: string, attr: Attribute): void {

          this.hierarchyService.deleteAttributeType( geoObjectTypeCode, attr ).then( data => {
            this.onDeleteAttribute.next( data );

            if(data){
              this.geoObjectType.attributes.splice(this.geoObjectType.attributes.indexOf(attr), 1);
            }

        	this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
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
