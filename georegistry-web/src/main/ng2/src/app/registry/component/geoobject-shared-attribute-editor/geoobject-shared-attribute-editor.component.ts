import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';
import { ManageVersionsModalComponent } from './manage-versions-modal.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../pipe/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component( {
    selector: 'geoobject-shared-attribute-editor',
    templateUrl: './geoobject-shared-attribute-editor.component.html',
    styleUrls: ['./geoobject-shared-attribute-editor.css'],
    providers: [DatePipe]
} )

/**
 * This component is shared between:
 * - crtable (create-update-geo-object action detail)
 * - change-request (for submitting change requests)
 * - master list geoobject editing widget
 * 
 * Be wary of changing this component for one usecase and breaking other usecases!
 */
export class GeoObjectSharedAttributeEditorComponent implements OnInit {

    private bsModalRef: BsModalRef;

    /*
	 * The current state of the GeoObject in the GeoRegistry
	 */
    @Input() preGeoObject: GeoObject = null;

	/*
	 * The state of the GeoObject after our Change Request has been approved 
	 */
    @Input() postGeoObject: any = {};

    @Input() geoObjectType: GeoObjectType;

    @Input() isValid: boolean = true;

    @Output() valid = new EventEmitter<boolean>();

    @Input() allowCodeEdit: boolean = false;

    @Input() attributeExcludes: string[] = [];

    @Input() readOnly: boolean = false;

    @Input() isNew: boolean = false;

    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

    @ViewChild( "attributeForm" ) attributeForm;

    constructor( private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private datePipe: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe ) {

    }

    ngOnInit(): void {
        this.preGeoObject = JSON.parse( JSON.stringify( this.preGeoObject ) ); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.
        if ( this.postGeoObject == null ) {
            this.postGeoObject = JSON.parse( JSON.stringify( this.preGeoObject ) ); // Object.assign is a shallow copy. We want a deep copy.
        }
        else {
            this.postGeoObject = JSON.parse( JSON.stringify( this.postGeoObject ) ); // We're about to heavily modify this object. We don't want to muck with the original copy they sent us.
        }

        this.attributeForm.statusChanges.subscribe( result => {
            this.isValid = ( result === "VALID" );
            this.valid.emit( this.isValid );
        }
        );

        if ( this.attributeExcludes != null ) {
            this.geoObjectAttributeExcludes.push.apply( this.geoObjectAttributeExcludes, this.attributeExcludes );
        }

        // Convert types from server to frontend
        for ( var i = 0; i < this.geoObjectType.attributes.length; ++i ) {
            var attr = this.geoObjectType.attributes[i];

            // The front-end uses the date format 'yyyy-mm-dd'. Our backend expects dates in epoch format.
            if ( attr.type === "date" && this.preGeoObject.properties[attr.code] != null ) {
                var date = this.datePipe.transform( this.preGeoObject.properties[attr.code], 'yyyy-MM-dd' );

                this.preGeoObject.properties[attr.code] = date;

                if ( this.postGeoObject.properties[attr.code] != null ) {
                    var postDate = this.datePipe.transform( this.postGeoObject.properties[attr.code], 'yyyy-MM-dd' );

                    this.postGeoObject.properties[attr.code] = postDate;
                }
            }
            // Sometimes booleans come back from the server as "false" instead of an actual boolean
            else if ( attr.type === "boolean" && this.preGeoObject.properties[attr.code] !== null ) {
                var val = this.preGeoObject.properties[attr.code];
                var bool = ( val === "true" ) || ( val === true );

                this.preGeoObject.properties[attr.code] = bool;

                if ( this.postGeoObject.properties[attr.code] != null ) {
                    var postVal = this.postGeoObject.properties[attr.code];
                    var postBool = ( postVal === "true" ) || ( postVal === true );

                    this.postGeoObject.properties[attr.code] = postBool;
                }
            }
        }
    }


    onManageAttributeVersions(attribute: Attribute): void {
        this.bsModalRef = this.modalService.show( ManageVersionsModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

        // TODO: sending the properties like this is wrong
        this.bsModalRef.content.geoObject = this.preGeoObject;
        this.bsModalRef.content.geoObjectType = this.geoObjectType;
        this.bsModalRef.content.attributeCode = attribute.code;
        this.bsModalRef.content.attribute = attribute;
        // this.bsModalRef.content.attribute = this.preGeoObject.properties[attribute.code];
        this.bsModalRef.content.onAttributeVersionChange.subscribe( versionObj => {
            console.log(versionObj)

            // TODO: set the version on the GeoObject attribute
        } );
    }

    onSelectPropertyOption( event: any, option: any ): void {
        this.currentTermOption = JSON.parse( JSON.stringify( this.modifiedTermOption ) );
    }

    getGeoObjectTypeTermAttributeOptions( termAttributeCode: string ) {
        for ( let i = 0; i < this.geoObjectType.attributes.length; i++ ) {
            let attr: any = this.geoObjectType.attributes[i];

            if ( attr.type === "term" && attr.code === termAttributeCode ) {

                attr = <AttributeTerm>attr;
                let attrOpts = attr.rootTerm.children;

                if ( attrOpts.length > 0 ) {
                    return this.removeStatuses( JSON.parse( JSON.stringify( attrOpts ) ) );;
                }
            }
        }

        return null;
    }
    removeStatuses( arr: any[] ) {
        var newI = -1;
        for ( var i = 0; i < arr.length; ++i ) {
            if ( arr[i].code === "CGR:Status-New" ) {
                newI = i;
                break;
            }
        }
        if ( newI != -1 ) {
            arr.splice( newI, 1 );
        }


        var pendI = 0;
        for ( var i = 0; i < arr.length; ++i ) {
            if ( arr[i].code === "CGR:Status-Pending" ) {
                pendI = i;
                break;
            }
        }
        if ( pendI != -1 ) {
            arr.splice( pendI, 1 );
        }

        return arr;
    }

    isStatusChanged( post, pre ) {
        if ( pre.length == 0 || post.length == 0 ) {
            return false;
        }

        var preCompare = pre;
        if ( Array.isArray( pre ) ) {
            preCompare = pre[0];
        }

        var postCompare = post;
        if ( Array.isArray( post ) ) {
            postCompare = post[0];
        }

        return preCompare !== postCompare;
    }

    getTypeDefinition( key: string ): string {
        // let attrs = this.geoObjectType.attributes;


        // attrs.attributes.forEach(attr => {
        for ( let i = 0; i < this.geoObjectType.attributes.length; i++ ) {
            let attr = this.geoObjectType.attributes[i];

            if ( attr.code === key ) {
                return attr.type;
            }
        }

        return null;
    }

    public getIsValid(): boolean {
        return this.isValid;
    }

    public getGeoObject(): any {
        // The front-end uses the 'yyyy-mm-dd' date format. Our backend expects dates in epoch format.
        var submitGO = JSON.parse( JSON.stringify( this.postGeoObject ) );
        for ( var i = 0; i < this.geoObjectType.attributes.length; ++i ) {
            var attr = this.geoObjectType.attributes[i];

            if ( attr.type === "date" && this.postGeoObject.properties[attr.code] != null ) {
                var parts = this.postGeoObject.properties[attr.code].split( '-' );
                var date = new Date( parts[0], parts[1] - 1, parts[2] );

                submitGO.properties[attr.code] = date.getTime();
            }
        }

        return submitGO;
    }
}
