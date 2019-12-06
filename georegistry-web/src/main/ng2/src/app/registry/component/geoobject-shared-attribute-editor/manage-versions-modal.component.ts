import { Component, OnInit, Input, ChangeDetectorRef, HostBinding } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';
import {
  trigger,
  state,
  style,
  animate,
  transition,
  group,
  query,
  stagger
} from '@angular/animations';

import { GeoObject, GeoObjectType, Attribute, ValueOverTime, GeoObjectOverTime, AttributeTerm } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { LocalizationService } from '../../../shared/service/localization.service';


@Component( {
    selector: 'manage-versions-modal',
    templateUrl: './manage-versions-modal.component.html',
    styleUrls: [],
    host: { '[@fadeInOut]': 'true' },
    animations: [
        [
            trigger('fadeInOut', [
                transition('void => *', [
                    style({
                        opacity: 0
                    }),
                    animate('1000ms')
                ]),
                transition('* => void', [
                    style({
                        opacity: 0
                    }),
                    animate('1000ms')
                ])
            ])
        ]]
} )
export class ManageVersionsModalComponent implements OnInit {
    message: string = null;

    readonly: boolean = false;

    
    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
    onAttributeVersionChange: Subject<GeoObjectOverTime>;

    // attr: Attribute;
    @Input() attribute: Attribute;
    
    @Input() geoObjectType: GeoObjectType;
    
    @Input() geoObjectOverTime: GeoObjectOverTime;
    
    goGeometries: GeoObjectOverTime;
    
    isNewGeoObject: boolean = false;
    
    newVersion: ValueOverTime;
    
    editingGeometry: number = -1;

    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, public changeDetectorRef: ChangeDetectorRef ) { }

    ngOnInit(): void {

		this.onAttributeVersionChange = new Subject();
    }
    
    tfInit(): void {
      if (this.attribute.code === 'geometry' && this.geoObjectOverTime.attributes[this.attribute.code].values.length == 1)
	  {
	    this.editingGeometry = 0;
	  }
    }

    onDateChange(event: any, vAttribute, Attribute): any {
        let dt = new Date(event);
        let vAttributes = this.geoObjectOverTime.attributes[this.attribute.code].values;

        vAttribute.startDate = this.formatDateString(dt);

        this.snapDates(vAttributes);

        this.changeDetectorRef.detectChanges()

    }

    snapDates(attributeArr: ValueOverTime[]){

        // Sort the data
        attributeArr.sort(function(a, b){
      
            let first: any = new Date(a.startDate);
            let next: any = new Date(b.startDate);
            return first - next;
        });


        let lastStartDate: Date;
        let lastEndDate: Date;
        for (let i = attributeArr.length - 1; i >=0 ; i--) {
            let vAttr = attributeArr[i];
            
            // Only change those older than the most recent
            if(i < attributeArr.length - 1){

                vAttr.endDate = this.formatDateString(new Date(new Date(lastStartDate).getTime() - 1));
            }
            else{
                // This should be the last entry in the array ONLY
                // Set end date to infinity
                vAttr.endDate = this.formatDateString(new Date('5000-12-31'));
            }

            lastStartDate = new Date(vAttr.startDate);
            lastEndDate = new Date(vAttr.endDate);
        }
    }

    formatDateString(dateObj: Date): string{
        const day = dateObj.getUTCDate();

        return dateObj.getUTCFullYear() + "-" + ( dateObj.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    onAddNewVersion(): void {

        let vot: ValueOverTime = new ValueOverTime();
        vot.startDate = this.formatDateString(new Date());
        vot.endDate = this.formatDateString(new Date());
        
        let attributeType = null;
        for (var i = 0; i < this.geoObjectType.attributes.length; ++i)
        {
          if (this.geoObjectType.attributes[i].code === this.attribute.code)
          {
            attributeType = this.geoObjectType.attributes[i].type;
          }
        }
        
        if (this.isNewGeoObject)
        {
        	if (attributeType === "local")
	        {
	          vot.value = {"localizedValue":"new thing","localeValues":[{"locale":"defaultLocale","value":"new thing"},{"locale":"km_KH","value":null}]};
	        }
	        else if (attributeType === 'geometry')
	        {
	          vot.value = {"type":"MultiPolygon", "coordinates":[]};
	        }
        }
        else
        {
	      vot.value = this.geoObjectOverTime.attributes[this.attribute.code].values[0].value;
        }
        
        this.geoObjectOverTime.attributes[this.attribute.code].values.push(vot);
        
        if (this.attribute.code === 'geometry')
		{
		  this.editingGeometry = this.geoObjectOverTime.attributes[this.attribute.code].values.length - 1;
		}

        this.snapDates(this.geoObjectOverTime.attributes[this.attribute.code].values);
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
    
    editGeometry(index: number) {
      this.editingGeometry = index;
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
