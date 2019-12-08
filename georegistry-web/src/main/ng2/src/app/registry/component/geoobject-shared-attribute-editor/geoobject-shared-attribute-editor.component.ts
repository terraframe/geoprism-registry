import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';
import { LocalizedValue } from '../../../shared/model/core';

import { ManageVersionsModalComponent } from './manage-versions-modal.component';

import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObjectOverTime, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { ToEpochDateTimePipe } from '../../pipe/to-epoch-date-time.pipe';

import Utils from '../../utility/Utils';

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
    @Input() preGeoObject: GeoObjectOverTime = null;

    calculatedPreObject: any = {};

    /*
	 * The state of the GeoObject being modified
	 */
    @Input() postGeoObject: GeoObjectOverTime = null;

    calculatedPostObject: any = {};

    @Input() geoObjectType: GeoObjectType;

    @Input() allowCodeEdit: boolean = false;

    @Input() attributeExcludes: string[] = [];

    @Input() forDate: Date = new Date();

    @Input() readOnly: boolean = false;

    @Input() isNew: boolean = false;
    
    @Output() valid = new EventEmitter<boolean>();

    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    isValid: boolean = true;

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
            this.isValid = ( result === "VALID" || result === "DISABLED" );
            
            this.valid.emit( this.isValid );
        } );

        if ( this.attributeExcludes != null ) {
            this.geoObjectAttributeExcludes.push.apply( this.geoObjectAttributeExcludes, this.attributeExcludes );
        }

        this.calculate();
        
        
        let geomAttr = null;
        for (var i = 0; i < this.geoObjectType.attributes.length; ++i)
        {
          if (this.geoObjectType.attributes[i].code === 'geometry')
          {
            geomAttr = this.geoObjectType.attributes[i];
          }
        }
        if (geomAttr == null)
        {
          let geometry: Attribute = new Attribute("geometry", "geometry", new LocalizedValue("Geometry", null), new LocalizedValue("Geometry", null), true, false, false);
          this.geoObjectType.attributes.push(geometry);
        }
    }

    ngOnChanges( changes: SimpleChanges ) {
        if ( changes['forDate'] ) {
            this.calculate();
        }
    }

    calculate(): void {
        this.calculatedPreObject = this.calculateCurrent( this.preGeoObject );
        this.calculatedPostObject = this.calculateCurrent( this.postGeoObject );
    }

    calculateCurrent( goot: GeoObjectOverTime ): any {
        const object = {};

        const time = this.forDate.getTime();

        for ( let i = 0; i < this.geoObjectType.attributes.length; ++i ) {
            let attr = this.geoObjectType.attributes[i];

            if ( attr.isChangeOverTime ) {
                let values = goot.attributes[attr.code].values;

                values.forEach( vot => {

                    const startDate = Date.parse( vot.startDate );
                    const endDate = Date.parse( vot.endDate );

                    if ( time >= startDate && time <= endDate ) {

                        if ( attr.type === 'local' ) {
                            object[attr.code] = JSON.parse( JSON.stringify( vot.value ) );
                        }
                        else {
                            object[attr.code] = vot.value;
                        }

                    }
                } );
            }
            else {
                object[attr.code] = goot.attributes[attr.code];
            }
        }

        return object;
    }


    onManageAttributeVersions( attribute: Attribute ): void {
        this.bsModalRef = this.modalService.show( ManageVersionsModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );

        // TODO: sending the properties like this is wrong
        this.bsModalRef.content.geoObjectOverTime = this.postGeoObject;
        this.bsModalRef.content.geoObjectType = this.geoObjectType;
        this.bsModalRef.content.isNewGeoObject = this.isNew;
        // this.bsModalRef.content.attributeCode = attribute.code;
        this.bsModalRef.content.attribute = attribute;
        // this.bsModalRef.content.attribute = this.preGeoObject.properties[attribute.code];
        this.bsModalRef.content.onAttributeVersionChange.subscribe( versionObj => {
            this.calculate();
        } );
        this.bsModalRef.content.tfInit();
    }
    
    //onManageGeometryVersions(): void {
    //  let geometry: Attribute = new Attribute("geometry", "geometry", null, null, true, false, false);
    //  this.onManageAttributeVersions(geometry);
    //}

    isDifferentText( attribute: Attribute ): boolean {
        if ( this.calculatedPostObject[attribute.code] == null && this.calculatedPreObject[attribute.code] != null ) {
            return true;
        }

        return ( this.calculatedPostObject[attribute.code] && this.calculatedPostObject[attribute.code].trim() !== this.calculatedPreObject[attribute.code] );
    }

    isDifferentValue( attribute: Attribute ): boolean {
        if ( this.calculatedPostObject[attribute.code] == null && this.calculatedPreObject[attribute.code] != null ) {
            return true;
        }

        return ( this.calculatedPostObject[attribute.code] && this.calculatedPostObject[attribute.code] !== this.calculatedPreObject[attribute.code] );
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
                    return Utils.removeStatuses( JSON.parse( JSON.stringify( attrOpts ) ) );;
                }
            }
        }

        return null;
    }

    isStatusChanged( post, pre ) {

        if ( pre != null && post == null ) {
            return true;
        }

        if ( pre == null || post == null || pre.length == 0 || post.length == 0 ) {
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
        return this.postGeoObject;

        //        // The front-end uses the 'yyyy-mm-dd' date format. Our backend expects dates in epoch format.
        //        var submitGO = JSON.parse( JSON.stringify( this.postGeoObject ) );
        //        for ( var i = 0; i < this.geoObjectType.attributes.length; ++i ) {
        //            var attr = this.geoObjectType.attributes[i];
        //
        //            if ( attr.type === "date" && this.postGeoObject.properties[attr.code] != null ) {
        //                var parts = this.postGeoObject.properties[attr.code].split( '-' );
        //                var date = new Date( parts[0], parts[1] - 1, parts[2] );
        //
        //                submitGO.properties[attr.code] = date.getTime();
        //            }
        //        }
        //
        //        return submitGO;
    }
}
