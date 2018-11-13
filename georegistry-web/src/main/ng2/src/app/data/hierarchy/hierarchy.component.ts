///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit, ViewChild, ElementRef, TemplateRef } from '@angular/core';
import { Router } from '@angular/router';

import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateModalComponent } from './modals/create-modal.component';
import { CreateChildModalComponent } from './modals/create-child-modal.component';
import { ConfirmModalComponent } from './modals/confirm-modal.component';
import { ErrorModalComponent } from './modals/error-modal.component';

import { Hierarchy, HierarchyType, HierarchyNode, GeoObjectType, TreeEntity } from './hierarchy';

import { HierarchyService } from '../../service/hierarchy.service';

class Instance {
  active: boolean;
  label: string;   
}

@Component({
  
  selector: 'hierarchies',
  templateUrl: './hierarchy.component.html',
  styleUrls: []
})
export class HierarchyComponent implements OnInit {
  instance : Instance = new Instance();  
  private hierarchies: HierarchyType[];
  private geoObjectTypes: GeoObjectType[] = [];

//  private hierarchyNodes:any = [];
  private nodes = [] as HierarchyNode[];
  private currentHierarchy: HierarchyType = null;

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;


  /*
   * Tree component
   */
  @ViewChild( TreeComponent )
  private tree: TreeComponent;
  
  /*
   * Template for the delete confirmation
   */
  @ViewChild( 'confirmTemplate' ) public confirmTemplate: TemplateRef<any>;
  
  /*
   * Template for tree node menu
   */
  @ViewChild( 'nodeMenu' ) public nodeMenuComponent: ContextMenuComponent;

  /*
   * Template for leaf menu
   */
  @ViewChild( 'leafMenu' ) public leafMenuComponent: ContextMenuComponent;
  
  /* 
   * Currently clicked on id for delete confirmation modal 
   */
  current: TreeNode;
  
  
  private setNodes():void {
	  this.hierarchies.forEach(hierarchy => {
		if(hierarchy.rootGeoObjectTypes.length > 0){
		  this.nodes = hierarchy.rootGeoObjectTypes;
		}
	  })
	  
	  setTimeout(() => {
		  this.tree.treeModel.expandAll();
	  }, 1)
  }
  
  private getHierarchy(hierarchyId: string):HierarchyType {
	  let target: HierarchyType = null;
	  this.hierarchies.forEach(hierarchy => {
		  if(hierarchyId === hierarchy.code){
			  target = hierarchy;
		  }
	  });
	  
	  return target;
  }
  
  private setHierarchies(data: HierarchyType[]):void{
	  let hierarchies:HierarchyType[] = [];
	  data.forEach( (hierarchyType, index) => {
		  
		  if(hierarchyType.rootGeoObjectTypes.length > 0){
			  hierarchyType.rootGeoObjectTypes.forEach(rootGeoObjectType => {
			    this.processHierarchyNodes(rootGeoObjectType);
			  })
		  }
		  
		  hierarchies.push(hierarchyType);
		  
	  });
	  
	  this.hierarchies = hierarchies
  }
  
  /**
   * Set properties required by angular-tree-component using recursion.
   */
  private processHierarchyNodes(node: HierarchyNode){
	  node.label = this.getHierarchyLabel(node.geoObjectType);
	  
	  node.children.forEach(child => {
		  this.processHierarchyNodes(child);
	  })
  }
  
  private getHierarchyLabel(geoObjectTypeCode: string):string {
	  let label: string = null;
	  this.geoObjectTypes.forEach(function(gOT){
		  if(gOT.code === geoObjectTypeCode){
			  label = gOT.localizedLabel;
		  }
	  });
	  
	  return label;
  }

  constructor(private router: Router, private hierarchyService: HierarchyService, private modalService: BsModalService, private contextMenuService: ContextMenuService) { 
	  
  }

  ngOnInit(): void {
	  this.hierarchyService.getGeoObjectTypes([])
	    .then( types => {
		  this.geoObjectTypes = types;
        }).catch(( err: any ) => {
          this.error( err.json() );
        });
	  
	  this.hierarchyService.getHierarchyTypes([])
	    .then( types => {
		  this.setHierarchies(types);
		  
		  this.setNodes();
		  
		  this.currentHierarchy = this.hierarchies[0];
      })
  }
  
  ngAfterViewInit() {
	  
  }
  
  handleOnMenu( node: any, $event: any ): void {
	  
//	  if(node.data.children.length > 0){
//		  node.data.children
//	  }
  	
//  	if(node.data.typeLabel === "Site"){
//  		node.data.childType = "Project"
//  	}
//  	else if(node.data.typeLabel === "Project"){
//  		node.data.childType = "Mission"
//  	}
//  	else if(node.data.typeLabel === "Mission"){
//  		node.data.childType = "Collection"
//  	}
//  	else if(node.data.typeLabel === "Collection"){
//  		node.data.childType = null
//  	}
  	
  	
      this.contextMenuService.show.next( {
          contextMenu: (node.data.childType !== null ? this.nodeMenuComponent : this.leafMenuComponent),
          event: $event,
          item: node,
      } );
      $event.preventDefault();
      $event.stopPropagation();
  }
  
  treeNodeOnClick( node: TreeNode, $event: any ): void {
  	
  	node.treeModel.setFocusedNode(node);
  	
  	if(node.treeModel.isExpanded(node)){
      	node.collapse();
  	}
  	else{
      	node.treeModel.expandAll();
  	}
  }
  
  options = {
//		  allowDrag: (node:TreeNode) => node.isLeaf,
//		  allowDrop: (element:Element, { parent, index }: {parent:TreeNode,index:number}) => {
			    // return true / false based on element, to.parent, to.index. e.g.
//			    return parent.hasChildren;
//			  },
//		    allowDrag: (node:any) => {
//		        return true;
//		      },
//		      allowDrop: (node:any) => {
//		        return true;
//		      },
	      displayField: "label",
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
	            drop: (tree: TreeComponent, node: TreeNode, $event: any, {from, to}: {from:TreeNode, to:TreeNode}) => {
	            	console.log("js")
//	              to.parent.children.set(to.index, from)
//	            	to.data.children.push(from)
//	            	to.parent.data.children.push(from)
//	              tree.treeModel.update()
	              
	              console.log('drag', from, to); // from === {name: 'first'}
	              // Add a node to `to.parent` at `to.index` based on the data in `from`
	              // Then call tree.update()
	            }
	          }
  };
  
  public hierarchyOnClick(event:any, item:any) {
	  let hierarchyId = item.code;
	  
	  this.currentHierarchy = item;
	  
	  this.nodes = [];
	  
	  if(this.getHierarchy(hierarchyId).rootGeoObjectTypes.length > 0){
	    // TODO: should rootGeoObjectTypes be hardcoded to only one entry in the array?
	    this.nodes.push(this.getHierarchy(hierarchyId).rootGeoObjectTypes[0]);
	    
	    setTimeout(() => {
		  this.tree.treeModel.expandAll();
		}, 1)
	  }
	  
	  this.tree.treeModel.update();
  }
  
  createHierarchy(): void {
	  this.bsModalRef = this.modalService.show( CreateModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
          'class': 'upload-modal'
      } );
//      this.bsModalRef.content.entity = data;
//      this.bsModalRef.content.parentId = parent.data.id;
      
      ( <CreateModalComponent>this.bsModalRef.content ).onHierarchytTypeCreate.subscribe( data => {
    	  
    	  // TODO: Make sure this works
    	  this.hierarchies.push(data);
      } );
  }
  
  
  addChildToHierarchy( parent: TreeNode ): void {
      this.current = parent;
      
      this.bsModalRef = this.modalService.show( CreateChildModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
          'class': 'upload-modal'
      } );
      this.bsModalRef.content.allGeoObjectTypes = this.geoObjectTypes;
      this.bsModalRef.content.parent = parent;
      this.bsModalRef.content.hierarchyType = this.currentHierarchy;

      ( <CreateChildModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( entity => {
          const d = this.current.data;

          if ( d.children != null ) {
              d.children.push( entity );
          }
          else {
              d.children = [entity];
              d.hasChildren = true;
          }

          this.tree.treeModel.update();
      } );
  }
  
  
//  createTreeNode( parent: TreeNode ): void {
//      this.current = parent;
//
//	  this.bsModalRef = this.modalService.show( CreateModalComponent, {
//          animated: true,
//          backdrop: true,
//          ignoreBackdropClick: true,
//          'class': 'upload-modal'
//      } );
//      this.bsModalRef.content.entity = data;
//      this.bsModalRef.content.parentId = parent.data.id;
//	  
//      this.hierarchyService.newHierarchyType( "AllowedIn", parent.data.geoObjectType, "test" ).then( data => {
//
//          ( <CreateModalComponent>this.bsModalRef.content ).onHierarchytTypeCreate.subscribe( entity => {
//              const d = parent;
//
//              if ( d.children != null ) {
//                  d.children.push( entity );
//              }
//              else {
//                  d.children = [entity];
////                  d.hasChildren = true; //TODO: uncomment and fix
//              }
//
//              this.tree.treeModel.update();
//          } );
//      } ).catch(( err: any ) => {
//          this.error( err.json() );
//      } );
//  }
  
  deleteTreeNode( node: TreeNode ): void {
      this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
          animated: true,
          backdrop: true,
          ignoreBackdropClick: true,
      } );
      this.bsModalRef.content.message = 'Are you sure you want to delete [' + node.data.label + ']';
      this.bsModalRef.content.data = node;

      ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
          this.removeTreeNode( data );
      } );
  }
  
  removeTreeNode( node: TreeNode ): void {
      this.hierarchyService.deleteHierarchyType( node.data.geoObjectType ).then( response => {
          const parent = node.parent;
          let children = parent.data.children;

          parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );

          if ( parent.data.children.length === 0 ) {
              parent.data.hasChildren = false;
          }

          this.tree.treeModel.update();
      } ).catch(( err: any ) => {
          this.error( err.json() );
      } );
  }
  
  isActive(item:any) {
      return this.currentHierarchy === item;
  };
  
  onDrop($event:any) {
	    // Dropped $event.element
	  console.log("on drop")
	  
//	  this.geoObjectTypes.push($event.element.data);
	  
	  this.removeTreeNode($event.element)
  }

  allowDrop(element:Element) {
	    // Return true/false based on element
	  console.log("allow drop")
	  return true;
  }
  
  error( err: any ): void {
      // Handle error
      if ( err !== null ) {
    	  // TODO: add error modal
          this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
          this.bsModalRef.content.message = ( err.localizedMessage || err.message );
      }

  }
   
}
