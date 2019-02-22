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

import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';

import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateHierarchyTypeModalComponent } from './modals/create-hierarchy-type-modal.component';
import { AddChildToHierarchyModalComponent } from './modals/add-child-to-hierarchy-modal.component';
import { CreateGeoObjTypeModalComponent } from './modals/create-geoobjtype-modal.component';
import { ManageGeoObjectTypeModalComponent } from './modals/manage-geoobjecttype-modal.component';
import { ConfirmModalComponent } from '../../core/modals/confirm-modal.component';
import { ErrorModalComponent } from '../../core/modals/error-modal.component';

import { LocalizationService } from '../../core/service/localization.service';

import { HierarchyType, HierarchyNode } from './hierarchy';
import { GeoObjectType } from '../../model/registry';
import { ModalTypes } from '../../core/modals/modal'

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';

class Instance {
    active: boolean;
    label: string;
}

@Component( {

    selector: 'hierarchies',
    templateUrl: './hierarchy.component.html',
    styleUrls: []
} )
export class HierarchyComponent implements OnInit {
    instance: Instance = new Instance();
    hierarchies: HierarchyType[];
    geoObjectTypes: GeoObjectType[] = [];
    nodes = [] as HierarchyNode[];
    currentHierarchy: HierarchyType = null;

    hierarchyTypeDeleteExclusions: string[] = ['AllowedIn', 'IsARelationship'];
    geoObjectTypeDeleteExclusions: string[] = ['ROOT'];


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


    constructor( private hierarchyService: HierarchyService, private modalService: BsModalService,
        private contextMenuService: ContextMenuService, private changeDetectorRef: ChangeDetectorRef,
        private localizeService: LocalizationService, private registryService: RegistryService ) {

    }

    ngOnInit(): void {
        this.registryService.init().then( response => {
            this.localizeService.setLocales( response.locales );

            this.geoObjectTypes = response.types;

            this.geoObjectTypes.sort(( a, b ) => {
                if ( a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase() ) return -1;
                else if ( a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase() ) return 1;
                else return 0;
            } );

            let pos = this.getGeoObjectTypePosition( "ROOT" );
            if ( pos ) {
                this.geoObjectTypes.splice( pos, 1 );
            }

            this.setHierarchies( response.hierarchies );

            this.setNodesOnInit();
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    ngAfterViewInit() {

    }

    public excludeHierarchyTypeDeletes( hierarchy: HierarchyType ) {
        return ( this.hierarchyTypeDeleteExclusions.indexOf( hierarchy.code ) !== -1 );
    }

    public excludeGeoObjectTypeDeletes( geoObjectType: GeoObjectType ) {
        return ( this.geoObjectTypeDeleteExclusions.indexOf( geoObjectType.code ) !== -1 );
    }

    private setNodesOnInit(): void {
        for ( let i = 0; i < this.hierarchies.length; i++ ) {
            let hierarchy = this.hierarchies[i];
            if ( hierarchy.rootGeoObjectTypes.length > 0 ) {
                this.nodes = hierarchy.rootGeoObjectTypes;
                this.currentHierarchy = hierarchy;
                break;
            }
        }

        setTimeout(() => {
            if ( this.tree ) {
                this.tree.treeModel.expandAll();
            }
        }, 1 )
    }

    private setNodesForHierarchy( hierarchyType: HierarchyType ): void {
        for ( let i = 0; i < this.hierarchies.length; i++ ) {
            let hierarchy = this.hierarchies[i];
            if ( hierarchy.code === hierarchyType.code ) {
                this.nodes = hierarchyType.rootGeoObjectTypes;
                this.currentHierarchy = hierarchy;
                break;
            }
        }

        setTimeout(() => {
            this.tree.treeModel.expandAll();
        }, 1 )
    }

    private getHierarchy( hierarchyId: string ): HierarchyType {
        let target: HierarchyType = null;
        this.hierarchies.forEach( hierarchy => {
            if ( hierarchyId === hierarchy.code ) {
                target = hierarchy;
            }
        } );

        return target;
    }

    private setHierarchies( data: HierarchyType[] ): void {
        let hierarchies: HierarchyType[] = [];
        data.forEach(( hierarchyType, index ) => {

            if ( hierarchyType.rootGeoObjectTypes.length > 0 ) {
                hierarchyType.rootGeoObjectTypes.forEach( rootGeoObjectType => {
                    this.processHierarchyNodes( rootGeoObjectType );
                } )
            }

            hierarchies.push( hierarchyType );

        } );

        this.hierarchies = hierarchies

        this.hierarchies.sort(( a, b ) => {
            if ( a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase() ) return -1;
            else if ( a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase() ) return 1;
            else return 0;
        } );
    }

    private updateHierarchy( code: string, rootGeoObjectTypes: HierarchyNode[] ): void {
        this.hierarchies.forEach( hierarchy => {
            if ( hierarchy.code === code ) {
                hierarchy.rootGeoObjectTypes = rootGeoObjectTypes;
            }
        } )
    }

    /**
     * Set properties required by angular-tree-component using recursion.
     */
    private processHierarchyNodes( node: HierarchyNode ) {
        node.label = this.getHierarchyLabel( node.geoObjectType );

        node.children.forEach( child => {
            this.processHierarchyNodes( child );
        } )
    }

    private getHierarchyLabel( geoObjectTypeCode: string ): string {
        let label: string = null;
        this.geoObjectTypes.forEach( function( gOT ) {
            if ( gOT.code === geoObjectTypeCode ) {
                label = gOT.label.localizedValue;
            }
        } );

        return label;
    }

    public handleOnMenu( node: any, $event: any ): void {

        this.contextMenuService.show.next( {
            contextMenu: ( node.data.childType !== null ? this.nodeMenuComponent : this.leafMenuComponent ),
            event: $event,
            item: node,
        } );
        $event.preventDefault();
        $event.stopPropagation();
    }

    public treeNodeOnClick( node: TreeNode, $event: any ): void {

        node.treeModel.setFocusedNode( node );

        if ( node.treeModel.isExpanded( node ) ) {
            node.collapse();
        }
        else {
            node.treeModel.expandAll();
        }
    }

    options = {
        //		  allowDrag: (node:TreeNode) => node.isLeaf,
        //		  allowDrop: (element:Element, { parent, index }: {parent:TreeNode,index:number}) => {
        // return true / false based on element, to.parent, to.index. e.g.
        //			    return parent.hasChildren;
        //			  },
        displayField: "label",
        actionMapping: {
            mouse: {
                click: ( tree: TreeComponent, node: TreeNode, $event: any ) => {
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

    public hierarchyOnClick( event: any, item: any ) {
        let hierarchyId = item.code;

        this.currentHierarchy = item;

        this.nodes = [];

        if ( this.getHierarchy( hierarchyId ).rootGeoObjectTypes.length > 0 ) {
            // TODO: should rootGeoObjectTypes be hardcoded to only one entry in the array?
            this.nodes.push( this.getHierarchy( hierarchyId ).rootGeoObjectTypes[0] );

            setTimeout(() => {
                if ( this && this.tree ) {
                    this.tree.treeModel.expandAll();
                }
            }, 1 )
        }

        if ( this.tree ) {
            this.tree.treeModel.update();
        }
    }

    public createHierarchy(): void {
        this.bsModalRef = this.modalService.show( CreateHierarchyTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );

        ( <CreateHierarchyTypeModalComponent>this.bsModalRef.content ).onHierarchytTypeCreate.subscribe( data => {

            // TODO: Make sure this works
            this.hierarchies.push( data );
        } );
    }

    public deleteHierarchyType( obj: HierarchyType ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + '[' + obj.label + ']';
        this.bsModalRef.content.data = obj.code;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.removeHierarchyType( data );
        } );
    }

    public removeHierarchyType( code: string ): void {
        this.hierarchyService.deleteHierarchyType( code ).then( response => {

            let pos = this.getHierarchyTypePosition( code );
            this.hierarchies.splice( pos, 1 );

        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    public createGeoObjectType(): void {
        this.bsModalRef = this.modalService.show( CreateGeoObjTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.hierarchyType = this.currentHierarchy;

        ( <CreateGeoObjTypeModalComponent>this.bsModalRef.content ).onGeoObjTypeCreate.subscribe( data => {
            this.geoObjectTypes.push( data );
        } );
    }

    public createRootGeoObjectType(): void {

        this.bsModalRef = this.modalService.show( CreateGeoObjTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.hierarchyType = this.currentHierarchy;

        ( <CreateGeoObjTypeModalComponent>this.bsModalRef.content ).onGeoObjTypeCreate.subscribe( data => {
            this.geoObjectTypes.push( data );
        } );
    }

    public deleteGeoObjectType( obj: GeoObjectType ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + '[' + obj.label + ']';
        this.bsModalRef.content.data = obj.code;
        this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );
        this.bsModalRef.content.type = ModalTypes.danger;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.removeGeoObjectType( data );
        } );
    }

    public removeGeoObjectType( code: string ): void {
        this.registryService.deleteGeoObjectType( code ).then( response => {

            let pos = this.getGeoObjectTypePosition( code );
            this.geoObjectTypes.splice( pos, 1 );

            //          const parent = node.parent;
            //          let children = parent.data.children;
            //
            //          parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );
            //
            //          if ( parent.data.children.length === 0 ) {
            //              parent.data.hasChildren = false;
            //          }
            //
            //          this.tree.treeModel.update();
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    public manageGeoObjectType( geoObjectType: GeoObjectType ): void {

        this.bsModalRef = this.modalService.show( ManageGeoObjectTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'manage-geoobjecttype-modal'
        } );

        geoObjectType.attributes.sort(( a, b ) => {
            if ( a.label < b.label ) return -1;
            else if ( a.label > b.label ) return 1;
            else return 0;
        } );
        this.bsModalRef.content.geoObjectType = geoObjectType;

        ( <ManageGeoObjectTypeModalComponent>this.bsModalRef.content ).onGeoObjectTypeSubmitted.subscribe( data => {

            let position = this.getGeoObjectTypePosition( data.code );
            if ( position ) {
                this.geoObjectTypes[position] = data;
            }
        } );
    }

    private getHierarchyTypePosition( code: string ): number {
        for ( let i = 0; i < this.hierarchies.length; i++ ) {
            let obj = this.hierarchies[i];
            if ( obj.code === code ) {
                return i;
            }
        }
    }

    private getGeoObjectTypePosition( code: string ): number {
        for ( let i = 0; i < this.geoObjectTypes.length; i++ ) {
            let obj = this.geoObjectTypes[i];
            if ( obj.code === code ) {
                return i;
            }
        }

        return null;
    }

    public addChildAndRootToHierarchy(): void {
        const that = this;

        this.bsModalRef = this.modalService.show( AddChildToHierarchyModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.allGeoObjectTypes = this.geoObjectTypes;
        this.bsModalRef.content.parent = "ROOT";
        this.bsModalRef.content.toRoot = true;
        this.bsModalRef.content.hierarchyType = this.currentHierarchy;
        this.bsModalRef.content.nodes = this.nodes;

        ( <AddChildToHierarchyModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( hierarchyType => {

            that.processHierarchyNodes( hierarchyType.rootGeoObjectTypes[0] );
            that.updateHierarchy( hierarchyType.code, hierarchyType.rootGeoObjectTypes )

            that.setNodesForHierarchy( hierarchyType );

            if ( this.tree ) {
                this.tree.treeModel.update();
            }
        } );
    }

    public addChildToHierarchy( parent: TreeNode ): void {
        const that = this;
        that.current = parent;

        this.bsModalRef = this.modalService.show( AddChildToHierarchyModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.allGeoObjectTypes = this.geoObjectTypes;
        this.bsModalRef.content.parent = parent;
        this.bsModalRef.content.toRoot = false;
        this.bsModalRef.content.hierarchyType = this.currentHierarchy;
        this.bsModalRef.content.nodes = this.nodes;

        ( <AddChildToHierarchyModalComponent>this.bsModalRef.content ).onNodeChange.subscribe( hierarchyType => {
            const d = that.current.data;


            that.processHierarchyNodes( hierarchyType.rootGeoObjectTypes[0] );
            that.updateHierarchy( hierarchyType.code, hierarchyType.rootGeoObjectTypes )

            that.setNodesForHierarchy( hierarchyType );

            if ( this.tree ) {
                this.tree.treeModel.update();
            }
        } );
    }

    public deleteTreeNode( node: TreeNode ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + '[' + node.data.label + ']';
        this.bsModalRef.content.data = node;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.removeTreeNode( data );
        } );
    }

    public removeTreeNode( node: TreeNode ): void {
        this.hierarchyService.removeFromHierarchy( this.currentHierarchy.code, node.parent.data.geoObjectType, node.data.geoObjectType ).then( data => {
            const parent = node.parent;
            let children = parent.data.children;

            // Update the tree
            parent.data.children = children.filter(( n: any ) => n.id !== node.data.id );
            if ( parent.data.children.length === 0 ) {
                parent.data.hasChildren = false;
            }
            this.tree.treeModel.update();

            // Update the available GeoObjectTypes
            this.changeDetectorRef.detectChanges()

        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    public isActive( item: any ) {
        return this.currentHierarchy === item;
    };

    public onDrop( $event: any ) {
        // Dropped $event.element
        console.log( "on drop" )

        this.removeTreeNode( $event.element )
    }

    public allowDrop( element: Element ) {
        // Return true/false based on element
        return true;
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            // TODO: add error modal
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }

    }

}
