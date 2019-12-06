import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObject, GeoObjectType, Attribute, ValueOverTime, GeoObjectOverTime, AttributeTerm } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { LocalizationService } from '../../../shared/service/localization.service';


@Component( {
    selector: 'manage-versions-modal',
    templateUrl: './manage-versions-modal.component.html',
    styleUrls: []
} )
export class ManageVersionsModalComponent implements OnInit {
    message: string = null;

    readonly: boolean = false;

    
    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
    onAttributeVersionChange: Subject<GeoObjectOverTime>;

    // attr: Attribute;
    attribute: Attribute;
    
    geoObjectType: GeoObjectType;
    
    geoObjectOverTime: GeoObjectOverTime;
    
    // attributeCode: string;

    // @Input("attribute") 
    // set attribute(attribute: Attribute) {
    //     this.attr = attribute;
    //     this.service.getGeoObjectOverTime( this.geoObject.attributes.code, this.geoObjectType.code ).then( valueOverTimeCollection => {
    //         this.geoObjectOverTime = valueOverTimeCollection;
    //     } );

    //     // this.geoObjectOverTime = this.service.getAttributeVersions( this.geoObject.properties.code, this.geoObjectType.code, this.attributeCode );
      
    // }
    // geoObjectOverTime: GeoObjectOverTime;

    newVersion: ValueOverTime;

    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

		this.onAttributeVersionChange = new Subject();

    }

    onAddNewVersion(): void {

        let vot: ValueOverTime = new ValueOverTime();
        vot.startDate = new Date();
        vot.endDate = new Date();
        vot.value = this.geoObjectOverTime.attributes[this.attribute.code].values[0].value; // TODO handle different types
        
        this.geoObjectOverTime.attributes[this.attribute.code].values.push(vot);
    }

    canAddVersion(): boolean {
        let hasVal = true;

        if(this.geoObjectOverTime){
            this.geoObjectOverTime.attributes[this.attribute.code].values.forEach(val => {
                
                let historyVal = val.value;
                if(this.attribute.type === 'local'){
                    
                    if( this.getDefaultLocaleVal(historyVal) === this.getDefaultLocaleVal(this.geoObjectOverTime.attributes[this.attribute.code]) ){
                        hasVal = false;
                    }
                }
            });
        }

        return hasVal;
    }

    getVersionData(attribute: Attribute) {
        let versions: ValueOverTime[] = [];

        this.geoObjectOverTime.attributes[attribute.code].values.forEach(vAttribute => {
            vAttribute.value.localeValues.forEach(val => {
                versions.push(val);
            })
        })
        return versions;
    }

    getDefaultLocaleVal(locale: any): string {
        let defVal = null;

        locale.localeValues.forEach(locVal => {
            if(locVal.locale === 'defaultLocale'){
               defVal = locVal.value;             
            }
      
        })

        return defVal; 
    }

    getGeoObjectTypeTermAttributeOptions( termAttributeCode: string ) {
        for ( let i = 0; i < this.geoObjectType.attributes.length; i++ ) {
            let attr: any = this.geoObjectType.attributes[i];

            if ( attr.type === "term" && attr.code === termAttributeCode ) {

                attr = <AttributeTerm>attr;
                let attrOpts = attr.rootTerm.children;

                if ( attrOpts.length > 0 ) {
                    return this.removeStatuses( JSON.parse( JSON.stringify( attrOpts ) ) );
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

    remove(version: any ): void {

        let val = this.geoObjectOverTime.attributes[this.attribute.code];

        for(let i=0; i<val.values.length; i++){
            let vals = val.values[i];

            if(vals.startDate === version.startDate){
                val.values.splice(i, 1);
            }
        }
    }

    isChangeOverTime(attr: Attribute): boolean{
        let isChangeOverTime = false;

        this.geoObjectType.attributes.forEach(attribute => {
            if(this.attribute.code === attr.code){
                isChangeOverTime = attr.isChangeOverTime
            }
        })

        return isChangeOverTime;
    }

    onSubmit(): void {
        
        this.onAttributeVersionChange.next( this.geoObjectOverTime );

        this.bsModalRef.hide();
    }

    onCancel(): void {
        this.bsModalRef.hide()
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
