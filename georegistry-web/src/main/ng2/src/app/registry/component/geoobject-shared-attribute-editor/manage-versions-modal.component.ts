import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObject, GeoObjectType, MasterList, Attribute, ValueOverTime, GeoObjectOverTime } from '../../model/registry';

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

    attr: any;
    
    geoObjectType: GeoObjectType;
    
    geoObject: GeoObject;
    
    attributeCode: string;

    @Input("attribute") 
    set attribute(attribute: Attribute) {
        this.attr = attribute;
        this.service.getGeoObjectOverTime( this.geoObject.properties.code, this.geoObjectType.code ).then( valueOverTimeCollection => {
            this.geoObjectOverTime = valueOverTimeCollection;
        } );

        // this.geoObjectOverTime = this.service.getAttributeVersions( this.geoObject.properties.code, this.geoObjectType.code, this.attributeCode );
      
    }
    geoObjectOverTime: GeoObjectOverTime;

    newVersion: ValueOverTime;

    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

		this.onAttributeVersionChange = new Subject();

    }

    onAddNewVersion(): void {

        let vot: ValueOverTime = new ValueOverTime();
        vot.startDate = new Date();
        vot.endDate = new Date();
        vot.value = this.geoObject.properties[this.attr.code];
        
        this.geoObjectOverTime.attributes[this.attr.code].values.push(vot);

        // TODO: persist new value and inject returned data into list
    }

    canAddVersion(): boolean {
        let hasVal = true;

        if(this.geoObjectOverTime && this.geoObject){
            this.geoObjectOverTime.attributes[this.attr.code].values.forEach(val => {
                
                let historyVal = val.value;
                if(this.attr.type === 'local'){
                    
                    if( this.getDefaultLocaleVal(historyVal) === this.getDefaultLocaleVal(this.geoObject.properties[this.attr.code]) ){
                        hasVal = false;
                    }
                }
            });
        }

        return hasVal;
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

    remove(version: any ): void {
        // for(let i=0; i<this.geoObjectOverTime.length; i++){
        //     if(this.geoObjectOverTime[i].startDate === version.startDate){
        //         this.geoObjectOverTime.splice(i, 1);
        //         this.valAdded = false;
        //     }
        // }
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
