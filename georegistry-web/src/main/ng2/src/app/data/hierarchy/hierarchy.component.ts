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

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from './modals/error-modal.component';

import { Hierarchy, HierarchyType, HierarchyNode, GeoObjectType } from './hierarchy';

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
  private hierarchies: Hierarchy[];
  private geoObjectTypes: GeoObjectType[] = [];

//  private hierarchyNodes:any = [];
  private nodes: any = [];
  private currentHierarchy:any = null;

  /*
   * Reference to the modal current showing
  */
  private bsModalRef: BsModalRef;

  /*
   * Tree component
   */
  @ViewChild( TreeComponent )
  private tree: TreeComponent;
  
  private setNodes():void {
	  this.nodes = this.hierarchies
	  
	  setTimeout(() => {
		  this.tree.treeModel.expandAll();
	  }, 1)
  }
  
  private getHierarchy(hierarchyId: string):Hierarchy {
	  let target: Hierarchy = null;
	  this.hierarchies.forEach(hierarchy => {
		  if(hierarchyId === hierarchy.id){
			  target = hierarchy;
		  }
	  });
	  
	  return target;
  }
  
  private setHierarchies(data: HierarchyType[]):void{
	  let hierarchies:Hierarchy[] = [];
	  data.forEach( hierarchyType => {
		  // TODO: Assuming only 1 root. Change if support is needed for more
		  let type = hierarchyType.rootGeoObjectTypes[0];
		  
		  this.processHierarchyNodes(type);
		  
		  hierarchies.push(type);
	  });
	  
	  this.hierarchies = hierarchies
  }
  
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

  constructor(private router: Router, private hierarchyService: HierarchyService, private modalService: BsModalService) { 
	  
  }

  ngOnInit(): void {
	  this.hierarchyService.getGeoObjectTypes("")
	    .then( types => {
		  this.geoObjectTypes = types;
		  
		  this.hierarchyService.getHierarchyTypes(["com.runwaysdk.system.gis.geo.AllowedIn"])
		    .then( types => {
			  this.setHierarchies(types);
			  
			  this.setNodes();
			  
			  this.currentHierarchy = this.hierarchies[0];
	        })
	        
        }).catch(( err: any ) => {
          this.error( err.json() );
        });
  }
  
  ngAfterViewInit() {
	  
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
		  allowDrag: (node:TreeNode) => node.isLeaf,
		  allowDrop: (element:Element, { parent, index }: {parent:TreeNode,index:number}) => {
			    // return true / false based on element, to.parent, to.index. e.g.
			    return parent.hasChildren;
			  },
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
	  let hierarchyId = item.id;
	  
	  this.currentHierarchy = item;
	  
	  this.nodes = [];
	  
	  this.nodes.push(this.getHierarchy(hierarchyId))
	  
	  this.tree.treeModel.update();
  }
  
  createTreeNode( parent: TreeNode ): void {
//      this.current = parent;

      this.hierarchyService.newChild( parent.data.id ).then( data => {

//          ( <CreateModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( entity => {
//              const d = parent;
//
//              if ( d.children != null ) {
//                  d.children.push( entity );
//              }
//              else {
//                  d.children = [entity];
//                  d.hasChildren = true;
//              }
//
//              this.tree.treeModel.update();
//          } );
      } ).catch(( err: any ) => {
          this.error( err.json() );
      } );
  }
  
  removeTreeNode( node: TreeNode ): void {
//      this.hierarchyService.remove( node.data.id ).then( response => {
          const parent = node.parent;
          let children = parent.data.children;

          parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );

          if ( parent.data.children.length === 0 ) {
              parent.data.hasChildren = false;
          }

          this.tree.treeModel.update();
//      } ).catch(( err: any ) => {
//          this.error( err.json() );
//      } );
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
