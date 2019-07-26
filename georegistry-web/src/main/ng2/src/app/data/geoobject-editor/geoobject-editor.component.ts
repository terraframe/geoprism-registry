import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';

import { CascadingGeoSelector } from '../cascading-geo-selector/cascading-geo-selector'

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term, ParentTreeNode } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/geoobject-shared-attribute-editor/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/submit-change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

import { AuthService } from '../../core/auth/auth.service';

declare var acp: string;


@Component( {
    selector: 'geoobject-editor',
    templateUrl: './geoobject-editor.component.html',
    styleUrls: ['./geoobject-editor.component.css'],
    providers: [DatePipe]
} )

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorComponent implements OnInit {

    @Input() geoObjectType: GeoObjectType;

    isValid: boolean = false;

    tabIndex: number = 0;

    dataSource: Observable<any>;

    masterListId: string;

    isNewGeoObject: boolean = false;

    @Input() onSuccessCallback: Function;

    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    /*
     * GeoObject Property Editor
     */
    @ViewChild( "attributeEditor" ) attributeEditor;

    arePropertiesValid: boolean = false;

    // The current state of the GeoObject in the GeoRegistry
    goPropertiesPre: GeoObject;

    // The state of the GeoObject after our edit has been applied
    goPropertiesPost: GeoObject;

    /*
     * GeoObject Geometry Editor
     */
    @ViewChild( "geometryEditor" ) geometryEditor;

    areGeometriesValid: boolean = false;

    goGeometries: GeoObject;

    /*
     * GeoObject Cascading Parent Selector
     */
    @ViewChild( "parentSelector" ) parentSelector;

    areParentsValid: boolean = false;

    hierarchies: any;

    /*
     * The final artifacts which will be submitted
     */
    private parentTreeNode: ParentTreeNode;

    private goSubmit: GeoObject;


    constructor( private service: IOService, private modalService: BsModalService, public bsModalRef: BsModalRef, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe, authService: AuthService ) {
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {

    }

    setMasterListId( id: string ) {
        this.masterListId = id;
    }

    setOnSuccessCallback( func: Function ) {
        this.onSuccessCallback = func;
    }

    // Configures the widget to be used in a "New" context, that is to say
    // that it will be used to create a new GeoObject.
    public configureAsNew( typeCode: string ) {
        this.isNewGeoObject = true;

        this.fetchGeoObjectType( typeCode );

        this.registryService.newGeoObjectInstance( typeCode ).then( retJson => {
            this.hierarchies = retJson.hierarchies;
            this.goPropertiesPre = retJson.geoObject;
            this.goPropertiesPost = JSON.parse( JSON.stringify( this.goPropertiesPre ) );
            this.goGeometries = JSON.parse( JSON.stringify( this.goPropertiesPre ) );

            console.log( "Fetched newGeoObjectInstance", retJson );
        } );
    }

    // Configures the widget to be used in an "Edit Existing" context
    public configureAsExisting( code: string, typeCode: string ) {
        this.isNewGeoObject = false;

        this.fetchGeoObject( code, typeCode );
        this.fetchGeoObjectType( typeCode );
        this.fetchHierarchies( code, typeCode );
    }

    private fetchGeoObject( code: string, typeCode: string ) {
        this.registryService.getGeoObjectByCode( code, typeCode )
            .then( geoObject => {
                this.goPropertiesPre = geoObject;
                this.goPropertiesPost = JSON.parse( JSON.stringify( this.goPropertiesPre ) );
                this.goGeometries = JSON.parse( JSON.stringify( this.goPropertiesPre ) );

                if ( this.hierarchies != null ) {
                    this.goSubmit = this.goPropertiesPost;
                    this.goSubmit.geometry = this.goGeometries.geometry;
                    this.parentTreeNode.geoObject = this.goSubmit;

                    this.areGeometriesValid = true;
                    this.areParentsValid = true;
                    this.arePropertiesValid = true;
                    this.isValid = true;
                }

            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );
    }

    private fetchGeoObjectType( code: string ) {
        console.log( "fetching type", code );
        this.registryService.getGeoObjectTypes( [code] )
            .then( geoObjectType => {
                this.geoObjectType = geoObjectType[0];
                console.log( "Fetched GOTs", geoObjectType );
            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );
    }

    private fetchHierarchies( code: string, typeTypeCode: string ) {
        this.registryService.getHierarchiesForGeoObject( code, typeTypeCode )
            .then(( hierarchies: any ) => {

                this.hierarchies = hierarchies;
                this.parentTreeNode = CascadingGeoSelector.staticGetParents( this.hierarchies );
                this.areParentsValid = true;

                if ( this.goGeometries != null ) {
                    this.goSubmit = this.goPropertiesPost;
                    this.goSubmit.geometry = this.goGeometries.geometry;
                    this.parentTreeNode.geoObject = this.goSubmit;

                    this.areGeometriesValid = true;
                    this.areParentsValid = true;
                    this.arePropertiesValid = true;
                    this.isValid = true;
                }

            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );
    }

    getTypeAheadObservable( text, typeCode ) {
        return Observable.create(( observer: any ) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead( text, typeCode ).then( results => {
                observer.next( results );
            } );
        } );
    }

    typeaheadOnSelect( e: TypeaheadMatch, ptn: ParentTreeNode ): void {
        this.registryService.getGeoObjectByCode( e.item.code, ptn.geoObject.properties.type )
            .then( geoObject => {

                ptn.geoObject = geoObject;

            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );
    }

    private onValidChange() {
        if ( this.attributeEditor != null ) {
            this.arePropertiesValid = this.attributeEditor.getIsValid();
        }
        if ( this.geometryEditor != null ) {
            this.areGeometriesValid = this.geometryEditor.getIsValid();
        }
        if ( this.parentSelector != null ) {
            this.areParentsValid = this.parentSelector.getIsValid();
        }

        this.isValid = this.arePropertiesValid && this.areGeometriesValid && this.areParentsValid;
    }

    changePage( nextPage: number, force: boolean = false ): void {
        if ( nextPage === this.tabIndex && !force ) {
            return;
        }

        console.log( "Changing to page", nextPage );

        this.persistModelChanges();

        this.tabIndex = nextPage;

        this.onValidChange();
    }

    private persistModelChanges(): void {
        if ( this.attributeEditor != null ) {
            this.goPropertiesPost = this.attributeEditor.getGeoObject();
        }
        if ( this.geometryEditor != null ) {
            this.goGeometries = this.geometryEditor.saveDraw();
        }
        if ( this.parentSelector != null ) {
            this.hierarchies = this.parentSelector.getHierarchies();
            this.parentTreeNode = this.parentSelector.getParents();
        }

        this.goSubmit = this.goPropertiesPost;

        if ( this.goSubmit != null && this.goGeometries != null && this.goGeometries.geometry != null ) {
            this.goSubmit.geometry = this.goGeometries.geometry;
        }

        if ( this.parentTreeNode != null ) {
            this.parentTreeNode.geoObject = this.goSubmit;
        }
    }

    public error( err: any ): void {
        // TODO

        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

    public cancel(): void {
        this.bsModalRef.hide();
    }

    public submit(): void {
        if ( this.isValid ) {
            this.bsModalRef.hide();

            this.persistModelChanges();

            this.registryService.applyGeoObjectEdit( this.parentTreeNode, this.goSubmit, this.isNewGeoObject, this.masterListId )
                .then(() => {

                    if ( this.onSuccessCallback != null ) {
                        this.onSuccessCallback();
                    }

                } ).catch(( err: Response ) => {
                    this.error( err.json() );
                } );
        }
    }
}
