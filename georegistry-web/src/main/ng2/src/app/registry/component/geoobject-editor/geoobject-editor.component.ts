import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { RegistryService } from '@registry/service';
import { LocalizationService, AuthService } from '@shared/service';


import { GeoObjectType, GeoObjectOverTime, HierarchyOverTime, ParentTreeNode, ImportError } from '@registry/model/registry';


import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';


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

    isGeometryEditable: boolean;

    tabIndex: number = 0;

    dataSource: Observable<any>;

    masterListId: string;
    notes: string;

    isNewGeoObject: boolean = false;

    @Input() onSuccessCallback: Function;
    
    submitFunction: Function = null;

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

//    /*
//     * GeoObject Geometry Editor
//     */
//    @ViewChild( "geometryEditor" ) geometryEditor;
//
//    areGeometriesValid: boolean = false;

    /*
     * GeoObject Cascading Parent Selector
     */
    @ViewChild( "parentSelector" ) parentSelector;

    areParentsValid: boolean = false;

    hierarchies: HierarchyOverTime[];

    /*
     * Date in which the modal is shown for
     */
    dateStr: string = null;

    /*
     * Date in which the modal is shown for
     */
    forDate: Date = null;
    
    isEditingGeometries: boolean = false;

    /*
     * The final artifacts which will be submitted
     */
    private goSubmit: GeoObjectOverTime;

    constructor( private modalService: BsModalService, public bsModalRef: BsModalRef,
        private registryService: RegistryService, private localizeService: LocalizationService, 
        authService: AuthService ) {
        
        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();

        this.forDate = new Date();

        const day = this.forDate.getUTCDate();
        this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    ngOnInit(): void {

    }

    findVotWithStartDate( votArray, startDate ): any {
      for (let i: number = 0; i < votArray.length; ++i)
      {
        if (votArray[i].startDate === startDate)
        {
          return votArray[i];
        }
      }
      
      return null;
    }

    setMasterListId( id: string ) {
        this.masterListId = id;
    }

    handleDateChange(): void {
        this.forDate = new Date( Date.parse( this.dateStr ) );
    }

    setOnSuccessCallback( func: Function ) {
        this.onSuccessCallback = func;
    }

    // Configures the widget to be used in a "New" context, that is to say
    // that it will be used to create a new GeoObject.
    public configureAsNew( typeCode: string, dateStr: string, isGeometryEditable: boolean ) {
        this.isNewGeoObject = true;
        this.dateStr = dateStr;
        this.forDate = new Date( Date.parse( this.dateStr ) );
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObjectType( typeCode );
        this.fetchLocales();

        this.registryService.newGeoObjectOverTime( typeCode ).then( retJson => {
            this.goPropertiesPre = new GeoObjectOverTime(this.geoObjectType, retJson.geoObject.attributes);
            this.goPropertiesPost = new GeoObjectOverTime(this.geoObjectType, JSON.parse( JSON.stringify( this.goPropertiesPre ) ).attributes);

            this.hierarchies = retJson.hierarchies;
        } );
    }

    // Configures the widget to be used to resolve an ImportError
    public configureFromImportError( importError: ImportError, historyId: string, dateStr: string, isGeometryEditable: boolean ) {
        let typeCode = importError.object.geoObject.attributes.type;
        this.isNewGeoObject = importError.object.isNew;
        this.dateStr = dateStr;
        this.forDate = new Date( Date.parse( dateStr ) );
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObjectType( typeCode );
        this.fetchLocales();
        
        if (importError.object != null && importError.object.parents != null && importError.object.parents.length > 0)
        {
          this.hierarchies = importError.object.parents;
          this.areParentsValid = true;
        }
        else
        {
          this.registryService.newGeoObjectOverTime( typeCode ).then( retJson => {
            this.hierarchies = retJson.hierarchies;
          } );
          this.areParentsValid = false;
        }
        
        // TODO : Maybe we should ask the server for the pre object, if it exists.
        this.goPropertiesPre = new GeoObjectOverTime(this.geoObjectType, importError.object.geoObject.attributes);
        this.goPropertiesPost = new GeoObjectOverTime(this.geoObjectType, importError.object.geoObject.attributes);
        
        this.submitFunction = () => {
          let config = {
            historyId : historyId,
            importErrorId: importError.id,
            resolution: 'APPLY_GEO_OBJECT',
            parentTreeNode: this.hierarchies,
            geoObject: this.goSubmit,
            isNew: importError.object.isNew
          }
        
          this.registryService.submitErrorResolve( config )
            .then(() => {
  
              if ( this.onSuccessCallback != null ) {
                  this.onSuccessCallback();
              }
  
            } ).catch(( err: HttpErrorResponse ) => {
              this.error( err );
            } );
        }
    }

    // Configures the widget to be used in an "Edit Existing" context
    public configureAsExisting( code: string, typeCode: string, dateStr: string, isGeometryEditable: boolean ) {
        this.isNewGeoObject = false;
        this.dateStr = dateStr;
        this.forDate = new Date( Date.parse( this.dateStr ) );
        this.isGeometryEditable = isGeometryEditable;

        this.fetchGeoObject( code, typeCode );
        this.fetchGeoObjectType( typeCode );
        this.fetchHierarchies( code, typeCode );
        this.fetchLocales();
    }

    private fetchGeoObject( code: string, typeCode: string ) {
        this.registryService.getGeoObjectOverTime( code, typeCode ).then( geoObject => {
            this.goPropertiesPre = new GeoObjectOverTime(this.geoObjectType, JSON.parse( JSON.stringify( geoObject ) ).attributes);
            this.goPropertiesPost = new GeoObjectOverTime(this.geoObjectType, JSON.parse( JSON.stringify( this.goPropertiesPre ) ).attributes);
            //this.goPropertiesPost = JSON.parse( JSON.stringify( this.goPropertiesPre ) );
            
            this.goSubmit = this.goPropertiesPost;

//            this.areGeometriesValid = true;
            this.arePropertiesValid = true;
            this.isValid = true;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    private fetchLocales() {
        this.registryService.getLocales().then( locales => {
            this.localizeService.setLocales( locales );
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    private fetchGeoObjectType( code: string ) {
        this.registryService.getGeoObjectTypes( [code], null )
            .then( geoObjectType => {
                this.geoObjectType = geoObjectType[0];
                
                if (this.goPropertiesPre != null)
                {
                  this.goPropertiesPre.geoObjectType = this.geoObjectType;
                }
                if (this.goPropertiesPost != null)
                {
                  this.goPropertiesPost.geoObjectType = this.geoObjectType;
                }

                if ( !this.geoObjectType.isGeometryEditable ) {
//                    this.areGeometriesValid = true;
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
//        if ( this.geometryEditor != null ) {
//            this.areGeometriesValid = this.geometryEditor.getIsValid();
//        }
        if ( this.parentSelector != null ) {
            this.areParentsValid = this.parentSelector.getIsValid();
        }
        
//        this.isValid = this.arePropertiesValid && this.areGeometriesValid && this.areParentsValid;
        this.isValid = this.arePropertiesValid && this.areParentsValid;
    }

    changePage( nextPage: number, force: boolean = false ): void {
        if ( nextPage === this.tabIndex && !force ) {
            return;
        }


        this.persistModelChanges();

        this.tabIndex = nextPage;

        this.onValidChange();
        
        if (nextPage === 2)
        {
          this.isEditingGeometries = true;
        }
        else
        {
          this.isEditingGeometries = false;
        }
    }

    private persistModelChanges(): void {
        if ( this.attributeEditor != null ) {
          this.goPropertiesPost = this.attributeEditor.getGeoObject();
        }
        if ( this.parentSelector != null ) {
          this.hierarchies = this.parentSelector.getHierarchies();
        }

        this.goSubmit = this.goPropertiesPost;

        //        if ( this.parentTreeNode != null ) {
        //            this.parentTreeNode.geoObject = this.goSubmit;
        //        }
    }

    public error( err: HttpErrorResponse ): void {
          this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
          this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
    }

    public cancel(): void {
      this.bsModalRef.hide();
    }

    public submit(): void {
      if ( this.isValid ) {
        this.bsModalRef.hide();
    
        this.persistModelChanges();
        
        if (this.submitFunction == null)
        {
          this.registryService.applyGeoObjectEdit( this.hierarchies, this.goSubmit, this.isNewGeoObject, this.masterListId, this.notes )
            .then(() => {
  
              if ( this.onSuccessCallback != null ) {
                  this.onSuccessCallback();
              }
  
            } ).catch(( err: HttpErrorResponse ) => {
              this.error( err );
            } );
        }
        else
        {
          this.submitFunction();
        }
      }
    }
}
