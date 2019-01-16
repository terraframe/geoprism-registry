import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Input } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations'
import {NgControl, Validators, FormBuilder} from '@angular/forms';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { ButtonsModule } from 'ngx-bootstrap/buttons';

import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { TreeEntity, HierarchyType, GeoObjectType, Attribute, Term } from '../hierarchy';
import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { HierarchyService } from '../../../service/hierarchy.service';
import { GeoObjTypeModalService } from '../../../service/geo-obj-type-modal.service';

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
    message: string = null;
    newAttribute: Attribute = null;

    terms: Term[] = [];

    root: string = null;

    public onAddAttribute: Subject<Attribute>;

    modalState: string = null;

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
	        },
	        mouse: {
//	            drop: (tree: TreeComponent, node: TreeNode, $event: any, {from, to}: {from:TreeNode, to:TreeNode}) => {
//	              console.log('drag', from, to); // from === {name: 'first'}
//	              // Add a node to `to.parent` at `to.index` based on the data in `from`
//	              // Then call tree.update()
//	            }
	          }
    };

    public treeNodeOnClick( node: TreeNode, $event: any ): void {
  	
        node.treeModel.setFocusedNode(node);
        
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
        this.root = node.data.code;
    }

    constructor( private hierarchyService: HierarchyService, public bsModalRef: BsModalRef, private geoObjTypeModalService: GeoObjTypeModalService,
        private contextMenuService: ContextMenuService ) {

      geoObjTypeModalService.modalState.subscribe(state => {
        this.modalState = state;
      });
    }

    ngOnInit(): void {
        this.onAddAttribute = new Subject();

        this.newAttribute = { localizedDescription:"", localizedLabel:"", name:"", type:"character", isDefault:false};

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

    handleOnSubmit(): void {
        
        this.hierarchyService.addAttributeType( this.geoObjectType.code, this.newAttribute ).then( data => {
            this.geoObjectType.attributes.push(data);
            // this.onAddAttribute.next( data );
        	this.geoObjTypeModalService.setState("MANAGE-ATTRIBUTES");
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    setAttributeType(type: string): void {
        this.newAttribute.type = type;
    }

    cancel(): void {
        this.geoObjTypeModalService.setState("MANAGE-ATTRIBUTES");
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
            
            console.log(this.message);
        }
    }

}
