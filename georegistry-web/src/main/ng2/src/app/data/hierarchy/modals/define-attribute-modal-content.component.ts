import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import {NgControl, Validators, FormBuilder} from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ButtonsModule } from 'ngx-bootstrap/buttons';

import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { TreeEntity, HierarchyType, GeoObjectType, Attribute, AttributeTerm, Term, TermOption, ManageAttributeState } from '../hierarchy';
import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { HierarchyService } from '../../../service/hierarchy.service';

import { TermOptionInputComponent} from '../form-inputs/term-option-input.component';

import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';




@Component( {
    selector: 'define-attribute-modal-content',
    templateUrl: './define-attribute-modal-content.component.html',
    styleUrls: [],
    animations: [
        trigger('openClose', 
            [
            transition(
                ':enter', [
                style({ 'opacity': 0}),
                animate('500ms', style({ 'opacity': 1}))
                ]
            ),
            transition(
                ':leave', [
                style({ 'opacity': 1}),
                animate('0ms', style({'opacity': 0})),
                
                ]
            )]
      )
    ]
} )
export class DefineAttributeModalContentComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;
    @Input() modalState: ManageAttributeState;
    @Output() modalStateChange = new EventEmitter<ManageAttributeState>();
    message: string = null;
    newAttribute: Attribute = null;

    terms: Term[] = [];
    root: string = null;

    /*
    * Tree component
    */
    @ViewChild( TreeComponent )
    private tree: TreeComponent;

    /*
    * Template for tree node menu
    */
    @ViewChild( 'selectRootMenu' ) public selectRootMenuComponent: ContextMenuComponent;

    /*
    * Template for leaf menu
    */
    @ViewChild( 'leafMenu' ) public leafMenuComponent: ContextMenuComponent;


     options = {
	      displayField: "localizedLabel",
		  actionMapping: {
	            mouse: {
	                click : ( tree: TreeComponent, node: TreeNode, $event: any ) => {
	                    this.treeNodeOnClick( node, $event );
	                },
	                contextMenu: ( tree: any, node: any, $event: any ) => {
	                    this.handleOnMenu( node, $event );
	                }
	            }
	        }
    };

    public treeNodeOnClick( node: TreeNode, $event: any ): void {
  	
        //node.treeModel.setFocusedNode(node);
        
        if(node.treeModel.isExpanded(node)){
            node.collapse();
        }
        else{
            node.treeModel.expandAll();
        }
    }

    public handleOnMenu( node: any, $event: any ): void {
	  
        this.contextMenuService.show.next( {
            contextMenu: this.selectRootMenuComponent,
            event: $event,
            item: node,   
        } );
        $event.preventDefault();
        $event.stopPropagation();
    }

    public selectAsRoot(node: any): void {
        if(this.newAttribute instanceof AttributeTerm){
          this.newAttribute.setRootTerm(node.data.code);
        }
        
        this.tree.treeModel.setFocusedNode(node);
    }

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef,
      private contextMenuService: ContextMenuService ) {
    }

    ngOnInit(): void {

        this.setAttribute("character");

        this.hierarchyService.getTerms()
	      .then( terms => {
              this.terms = terms;
		  
	      }).catch(( err: any ) => {
	        this.error( err.json() );
          });

    }

    ngAfterViewInit() {
      window.setTimeout(() =>{
        if(this.tree){
          this.tree.treeModel.expandAll();
        }
      }, 1000)
    }

    ngOnDestroy(){
    }

    handleOnSubmit(): void {
        
        this.hierarchyService.addAttributeType( this.geoObjectType.code, this.newAttribute ).then( data => {
            this.geoObjectType.attributes.push(data);

            this.modalStateChange.emit({"state":"MANAGE-ATTRIBUTES", "attribute":""});
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    setAttribute(type:string): void {
        if(type === 'term'){
            this.newAttribute = new AttributeTerm("", type, "", "", false);
        }
        else{
            this.newAttribute = new Attribute("", type, "", "", false);
        }
    }

    
    cancel(): void {
        this.modalStateChange.emit({"state":"MANAGE-ATTRIBUTES", "attribute":""});
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
