import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from "@angular/common/http";

import { TreeNode, TreeComponent, TreeDropDirective } from 'angular-tree-component';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { ContextMenuService, ContextMenuComponent } from 'ngx-contextmenu';

import { CreateHierarchyTypeModalComponent } from './modals/create-hierarchy-type-modal.component';
import { AddChildToHierarchyModalComponent } from './modals/add-child-to-hierarchy-modal.component';
import { CreateGeoObjTypeModalComponent } from './modals/create-geoobjtype-modal.component';
import { ManageGeoObjectTypeModalComponent } from './modals/manage-geoobjecttype-modal.component';
import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { HierarchyType, HierarchyNode } from '../../model/hierarchy';
import { GeoObjectType } from '../../model/registry';
import { ModalTypes } from '../../../shared/model/modal'

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ErrorHandler } from '../../../shared/component/error-handler/error-handler';

class Instance {
    active: boolean;
    label: string;
}

@Component( {

    selector: 'hierarchies',
    templateUrl: './hierarchy.component.html',
    styleUrls: ['./hierarchy.css']
} )

export class HierarchyComponent implements OnInit {

    // isAdmin: boolean;
    // isMaintainer: boolean;
    // isContributor: boolean;

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
        private localizeService: LocalizationService, private registryService: RegistryService, private authService: AuthService ) {

        // this.admin = authService.isAdmin();
        // this.isMaintainer = this.isAdmin || service.isMaintainer();
		// this.isContributor = this.isAdmin || this.isMaintainer || service.isContributer();

    }

    ngOnInit(): void {
        this.refreshAll( null );
    }

    ngAfterViewInit() {

    }

    isRA(): boolean {
        return this.authService.isRA();
    }

    isOrganizationRA(orgCode: string): boolean {
        return this.authService.isOrganizationRA(orgCode);
    }

    public refreshAll( desiredHierarchy ) {
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

            this.setNodesOnInit( desiredHierarchy );
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    public excludeHierarchyTypeDeletes( hierarchy: HierarchyType ) {
        return ( this.hierarchyTypeDeleteExclusions.indexOf( hierarchy.code ) !== -1 );
    }

    public excludeGeoObjectTypeDeletes( geoObjectType: GeoObjectType ) {
        return ( this.geoObjectTypeDeleteExclusions.indexOf( geoObjectType.code ) !== -1 );
    }

    private setNodesOnInit( desiredHierarchy ): void {

        let index = -1;

        if ( desiredHierarchy != null ) {
            index = this.hierarchies.findIndex( h => h.code === desiredHierarchy.code );
        }
        else if ( this.hierarchies.length > 0 ) {
            index = 0;
        }

        if ( index > -1 ) {
            let hierarchy = this.hierarchies[index];

            this.nodes = hierarchy.rootGeoObjectTypes;

            this.currentHierarchy = hierarchy;

            setTimeout(() => {
                if ( this.tree ) {
                    this.tree.treeModel.expandAll();
                }
            }, 1 )
        }
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
        if (this.isOrganizationRA(this.currentHierarchy.organizationCode))
        {
          this.contextMenuService.show.next( {
              contextMenu: ( node.data.childType !== null ? this.nodeMenuComponent : this.leafMenuComponent ),
              event: $event,
              item: node,
          } );
          $event.preventDefault();
          $event.stopPropagation();
        }
        else
        {
          $event.preventDefault();
          $event.stopPropagation();
        }
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

            this.hierarchies.push( data );
        } );
    }

    public deleteHierarchyType( obj: HierarchyType ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + obj.label.localizedValue + ']';
        this.bsModalRef.content.data = obj.code;
        this.bsModalRef.content.type = "DANGER";
        this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.removeHierarchyType( data );
        } );
    }

    public editHierarchyType( obj: HierarchyType, readOnly: boolean ): void {
        this.bsModalRef = this.modalService.show( CreateHierarchyTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'upload-modal'
        } );
        this.bsModalRef.content.edit = true;
        this.bsModalRef.content.readOnly = readOnly;
        this.bsModalRef.content.hierarchyType = obj;
        this.bsModalRef.content.onHierarchytTypeCreate.subscribe( data => {
            let pos = this.getHierarchyTypePosition( data.code );

            this.hierarchies[pos].label = data.label;
            this.hierarchies[pos].description = data.description;
        } );
    }

    public removeHierarchyType( code: string ): void {
        this.hierarchyService.deleteHierarchyType( code ).then( response => {

            let pos = this.getHierarchyTypePosition( code );
            this.hierarchies.splice( pos, 1 );

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
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

    public deleteGeoObjectType( obj: GeoObjectType ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + obj.label.localizedValue + ']';
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
            //        this.tree.treeModel.update();
            //this.setNodesOnInit();

            this.refreshAll( this.currentHierarchy );

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    public manageGeoObjectType( geoObjectType: GeoObjectType, readOnly: boolean ): void {

        this.bsModalRef = this.modalService.show( ManageGeoObjectTypeModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
            'class': 'manage-geoobjecttype-modal'
        } );

        geoObjectType.attributes.sort(( a, b ) => {
            if ( a.label.localizedValue < b.label.localizedValue ) return -1;
            else if ( a.label.localizedValue > b.label.localizedValue ) return 1;
            else return 0;
        } );
        this.bsModalRef.content.geoObjectType = geoObjectType;
        this.bsModalRef.content.readOnly = readOnly;

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
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + node.data.label + ']';
        this.bsModalRef.content.data = node;

        ( <ConfirmModalComponent>this.bsModalRef.content ).onConfirm.subscribe( data => {
            this.removeTreeNode( data );
        } );
    }

    public removeTreeNode( node: TreeNode ): void {
        this.hierarchyService.removeFromHierarchy( this.currentHierarchy.code, node.parent.data.geoObjectType, node.data.geoObjectType ).then( data => {

            if ( node.parent.data.geoObjectType == null ) {
                this.nodes = [];
                // this.refreshAll(null);
                //return;
            }

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

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    public isActive( item: any ) {
        return this.currentHierarchy === item;
    };

    public onDrop( $event: any ) {
        // Dropped $event.element
        this.removeTreeNode( $event.element )
    }

    public allowDrop( element: Element ) {
        // Return true/false based on element
        return true;
    }

    public error( err: HttpErrorResponse ): void {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
    }

}
