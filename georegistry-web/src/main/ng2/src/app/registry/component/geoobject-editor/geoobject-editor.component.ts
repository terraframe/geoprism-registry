import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';

import { CascadingGeoSelector } from '../cascading-geo-selector/cascading-geo-selector'

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObjectOverTime, HierarchyOverTime, Attribute, AttributeTerm, AttributeDecimal, Term, ParentTreeNode } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../pipe/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

import { AuthService } from '../../../shared/service/auth.service';

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
    goPropertiesPre: GeoObjectOverTime;

    // The state of the GeoObject after our edit has been applied
    goPropertiesPost: GeoObjectOverTime;

    /*
     * GeoObject Geometry Editor
     */
    @ViewChild( "geometryEditor" ) geometryEditor;

    areGeometriesValid: boolean = false;

    goGeometries: GeoObjectOverTime;

    /*
     * GeoObject Cascading Parent Selector
     */
    @ViewChild( "parentSelector" ) parentSelector;

    areParentsValid: boolean = false;

    hierarchies: HierarchyOverTime[];

    /*
     * Date in which the modal is shown for
     */
    forDate: Date = new Date();

    /*
     * The final artifacts which will be submitted
     */
    private goSubmit: GeoObjectOverTime;


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
            this.goPropertiesPre = retJson.geoObject;
            this.goPropertiesPost = JSON.parse( JSON.stringify( this.goPropertiesPre ) );
            this.goGeometries = JSON.parse( JSON.stringify( this.goPropertiesPre ) );

            this.hierarchies = retJson.hierarchies;
        } );
    }

    // Configures the widget to be used in an "Edit Existing" context
    public configureAsExisting( code: string, typeCode: string, forDate: string ) {
        this.isNewGeoObject = false;
        this.forDate = new Date( Date.parse( forDate ) );

        this.fetchGeoObject( code, typeCode );
        this.fetchGeoObjectType( typeCode );
        this.fetchHierarchies( code, typeCode );
    }

    private fetchGeoObject( code: string, typeCode: string ) {
        this.registryService.getGeoObjectOverTime( code, typeCode )
            .then( geoObject => {
                this.goPropertiesPre = geoObject;
                this.goPropertiesPost = JSON.parse( JSON.stringify( this.goPropertiesPre ) );
                this.goGeometries = JSON.parse( JSON.stringify( this.goPropertiesPre ) );

                this.goSubmit = this.goPropertiesPost;

                //                    this.goSubmit.geometry = this.goGeometries.geometry;

                this.areGeometriesValid = true;
                this.arePropertiesValid = true;
                this.isValid = true;
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
    }

    private fetchGeoObjectType( code: string ) {
        this.registryService.getGeoObjectTypes( [code] )
            .then( geoObjectType => {
                this.geoObjectType = geoObjectType[0];

                if ( !this.geoObjectType.isGeometryEditable ) {
                    this.areGeometriesValid = true;
                }

            } ).catch(( err: HttpErrorResponse ) => {
                console.log( err );
                //                this.error( err );
            } );
    }

    private fetchHierarchies( code: string, typeTypeCode: string ) {
        this.registryService.getHierarchiesForGeoObject( code, typeTypeCode )
            .then(( hierarchies: any ) => {                
                this.hierarchies = hierarchies;
                
                //                this.parentTreeNode = CascadingGeoSelector.staticGetParents( this.hierarchies );
                this.areParentsValid = true;

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
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

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
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
        }

        this.goSubmit = this.goPropertiesPost;

        //        if ( this.goSubmit != null && this.goGeometries != null && this.goGeometries.geometry != null ) {
        //            this.goSubmit.geometry = this.goGeometries.geometry;
        //        }
        //
        //        if ( this.parentTreeNode != null ) {
        //            this.parentTreeNode.geoObject = this.goSubmit;
        //        }
    }

    public error( err: HttpErrorResponse ): void {
        // TODO

        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

    public cancel(): void {
        this.bsModalRef.hide();
    }

    public submit(): void {
        if ( this.isValid ) {
            this.bsModalRef.hide();

            this.persistModelChanges();

            this.registryService.applyGeoObjectEdit( this.hierarchies, this.goSubmit, this.isNewGeoObject, this.masterListId )
                .then(() => {

                    if ( this.onSuccessCallback != null ) {
                        this.onSuccessCallback();
                    }

                } ).catch(( err: HttpErrorResponse ) => {
                    this.error( err );
                } );
        }
    }
}
