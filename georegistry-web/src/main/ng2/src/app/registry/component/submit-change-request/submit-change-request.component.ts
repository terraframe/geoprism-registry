import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

import { ErrorHandler, ErrorModalComponent, SuccessModalComponent } from '@shared/component';
import { LocalizationService, AuthService } from '@shared/service';

import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';
import { IOService, RegistryService, HierarchyService, ChangeRequestService } from '@registry/service';
import { GeoObjectType, GeoObjectOverTime, Attribute, AttributeTerm, AttributeDecimal, Term } from '@registry/model/registry';



declare var acp: string;

@Component( {
    selector: 'submit-change-request',
    templateUrl: './submit-change-request.component.html',
    styleUrls: []
} )
export class SubmitChangeRequestComponent implements OnInit {

    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;

    geoObjectType: GeoObjectType;

    geoObjectTypes: GeoObjectType[] = [];

    typeaheadLoading: boolean;

    typeaheadNoResults: boolean;

    geoObjectId: string = "";

    reason: string = "";

    dataSource: Observable<any>;

    @ViewChild( "attributeEditor" ) attributeEditor;

    @ViewChild( "geometryEditor" ) geometryEditor;

	/*
	 * The current state of the GeoObject in the GeoRegistry
	 */
    preGeoObject: GeoObjectOverTime = null;

	/*
	 * The state of the GeoObject after our Change Request has been approved 
	 */
    postGeoObject: GeoObjectOverTime = null;

    isValid: boolean = false;

    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];

    constructor( private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private date: DatePipe, private localizeService: LocalizationService,
        private authService: AuthService ) {

        this.dataSource = Observable.create(( observer: any ) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead( this.geoObjectId, this.geoObjectType.code ).then( results => {
                observer.next( results );
            } );
        } );
    }

    ngOnInit(): void {
        this.registryService.getGeoObjectTypes( [], null ).then( types => {
            
            var myOrgTypes = [];
            for (var i = 0; i < types.length; ++i)
            {
              if (this.authService.isGeoObjectTypeRC(types[i].organizationCode, types[i].code))
              {
                myOrgTypes.push(types[i]);
              }
            }
            this.geoObjectTypes = myOrgTypes;

            this.geoObjectTypes.sort(( a, b ) => {
                if ( a.label.localizedValue.toLowerCase() < b.label.localizedValue.toLowerCase() ) return -1;
                else if ( a.label.localizedValue.toLowerCase() > b.label.localizedValue.toLowerCase() ) return 1;
                else return 0;
            } );

            let pos = this.getGeoObjectTypePosition( "ROOT" );
            if ( pos ) {
                this.geoObjectTypes.splice( pos, 1 );
            }

            // this.currentGeoObjectType = this.geoObjectTypes[1];

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );

    }



    private onValidChange( newValid: boolean ) {
        if ( this.preGeoObject == null ) {
            this.isValid = false;
            return;
        }

        if ( this.geometryEditor != null && !this.geometryEditor.getIsValid() ) {
            this.isValid = false;
            return;
        }

        if ( this.attributeEditor != null && !this.attributeEditor.getIsValid() ) {
            this.isValid = false;
            return;
        }

        this.isValid = true;
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

    changeTypeaheadLoading( e: boolean ): void {
        this.typeaheadLoading = e;
    }

    typeaheadOnSelect( e: TypeaheadMatch ): void {
        this.registryService.getGeoObjectOverTime( e.item.code, this.geoObjectType.code ).then( geoObject => {
                this.preGeoObject = geoObject;
                this.postGeoObject = JSON.parse( JSON.stringify( this.preGeoObject ) ); // Object.assign is a shallow copy. We want a deep copy.

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
    }

    submit(): void {

        let goSubmit: GeoObjectOverTime = this.attributeEditor.getGeoObject();

        if ( this.geometryEditor != null ) {
            let goGeometries: GeoObjectOverTime = this.geometryEditor.saveDraw();
//            goSubmit.geometry = goGeometries.geometry;
        }

        let actions = [{
            "actionType": "geoobject/update", // TODO: account for create
            "apiVersion": "1.0-SNAPSHOT", // TODO: make dynamic
            "createActionDate": new Date().getTime(),
            "geoObject": goSubmit,
            "contributorNotes": this.reason
        }]

        this.changeRequestService.submitChangeRequest( JSON.stringify( actions ) )
            .then( geoObject => {
                this.cancel();

                this.bsModalRef = this.modalService.show( SuccessModalComponent, { backdrop: true } );
                this.bsModalRef.content.message = this.localizeService.decode( "change.request.success.message" );

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );

        this.isValid = false;
    }

    cancel(): void {
        this.isValid = false;
        this.preGeoObject = null;
        this.postGeoObject = null;
        this.geoObjectId = null;
        this.geoObjectType = null;
        this.reason = null;
    }

    public error( err: any ): void {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ErrorHandler.getMessageFromError(err);
    }
}