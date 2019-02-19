import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/change-request/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component({
    selector: 'change-request',
    templateUrl: './change-request.component.html',
    styleUrls: ['./change-request.css'],
    providers: [DatePipe]
})

export class ChangeRequestComponent implements OnInit {

    objectKeys = Object.keys;
    @Input() standAlone = true;

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    geoObjectTypes: GeoObjectType[] = [];
    @Input() geoObjectType: GeoObjectType;
    geoObjectId: string = "";
    @Input() currentGeoObject: GeoObject = null;
    @Input() modifiedGeoObject: GeoObject;
    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    reason: string = "";
    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];
    asyncSelected: string;
    typeaheadLoading: boolean;
    typeaheadNoResults: boolean;
    dataSource: Observable<any>;


    constructor(private service: IOService, private modalService: BsModalService, private changeDetectorRef: ChangeDetectorRef,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
        private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe) {

        this.dataSource = Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(this.geoObjectId, this.geoObjectType.code).then(results => {
                observer.next(results);
            });
        });
    }

    ngOnInit(): void {
        if(!this.currentGeoObject && !this.geoObjectType){
          this.registryService.getGeoObjectTypes([])
            .then(types => {
                this.geoObjectTypes = types;

                this.geoObjectTypes.sort((a, b) => {
                    if (a.localizedLabel.toLowerCase() < b.localizedLabel.toLowerCase()) return -1;
                    else if (a.localizedLabel.toLowerCase() > b.localizedLabel.toLowerCase()) return 1;
                    else return 0;
                });

                let pos = this.getGeoObjectTypePosition("ROOT");
                if (pos) {
                    this.geoObjectTypes.splice(pos, 1);
                }

                // this.currentGeoObjectType = this.geoObjectTypes[1];

            }).catch((err: Response) => {
                this.error(err.json());
            });
        }
    }

    private getGeoObjectTypePosition(code: string): number {
        for (let i = 0; i < this.geoObjectTypes.length; i++) {
            let obj = this.geoObjectTypes[i];
            if (obj.code === code) {
                return i;
            }
        }

        return null;
    }

    // onSelectGeoObjectType(event: any): void {
    //     let selectedGeoObjectTypeCode = event.target.value;

    // }

    onSelectPropertyOption(event: any, option:any): void {
        this.currentTermOption = JSON.parse(JSON.stringify(this.modifiedTermOption));
    }

    changeTypeaheadLoading(e: boolean): void {
        this.typeaheadLoading = e;
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {

        this.registryService.getGeoObjectByCode(e.item.code, this.geoObjectType.code)
            .then(geoObject => {

                // let term: Term = new Term("termCode", "term label", "term description");
                // term.addChild(new Term("optCode", "opt label", "opt description"))
                // term.addChild(new Term("optCode2", "opt label 2", "opt description 2"))

                // let test = {
                //     "testText": "test text",
                //     "testInteger": 3,
                //     "testBoolean": true,
                //     "testFloat": 1.111,
                //     "testCharacter": "Test Character Value",
                //     "testTerm": [
                //         "testOpt1"
                //     ],
                //     // "testTerm": term,
                //     "testDate": this.date.transform(1550475376, 'yyyy-dd-MM')
                // };

                // geoObject.properties = Object.assign({}, geoObject.properties, test);

                // for (var key in geoObject.properties) {
                //     if (geoObject.properties.hasOwnProperty(key)) {
                //         console.log(key + " -> " + geoObject.properties[key]);
                //         let def = this.getTypeDefinition(key);
                        
                //         if(def === "term"){
                //           let opts = this.getGeoObjectTypeTermAttributeOptions(key);
                //           if(opts.length > 0){
                //             this.allTermOptions = opts;
                //           }
                //         //   break;
                //         }
                //     }
                // }

                this.currentGeoObject = geoObject;
                this.modifiedGeoObject = JSON.parse(JSON.stringify(geoObject));

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }

    getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
        for (let i=0; i<this.geoObjectType.attributes.length; i++) {
            let attr: any = this.geoObjectType.attributes[i];

            if(attr.type === "term" && attr.code === termAttributeCode){

                attr = <AttributeTerm> attr;
                let attrOpts = attr.rootTerm.children;

                if(attrOpts.length > 0){
                    return attrOpts;
                }
            }
        }

        return null;
    }

    getTypeDefinition(key: string): string {
        // let attrs = this.geoObjectType.attributes;


        // attrs.attributes.forEach(attr => {
        for(let i=0; i<this.geoObjectType.attributes.length; i++){
            let attr = this.geoObjectType.attributes[i];

         if (attr.code === key) {
                return attr.type;
            }
        }

        return null;
    }

    isFormValid(): boolean {
        return true;
    }

    submit(): void {

        // Convert all dates from input element (date type) format to epoch time
        // for(let i=0; i<this.geoObjectType.attributes.length; i++){
        //     let attr = this.geoObjectType.attributes[i];

        //     if(attr.type === "date" && this.geoObjectAttributeExcludes.indexOf(attr.code) === -1){
        //         let propInGeoObj = this.modifiedGeoObject.properties[attr.code];

        //         if(propInGeoObj && propInGeoObj.length > 0){
        //             let formatted = new Date(propInGeoObj);

        //             let formattedStr = formatted.getFullYear() + "-" + formatted.getMonth() + "-" + formatted.getDay() + " AD " + formatted.getHours() + "-" + formatted.getMinutes() + "-" + formatted.getSeconds() + "-" + formatted.getMilliseconds() + " -" + formatted.getTimezoneOffset()

        //             // Required format: yyyy-MM-dd G HH-mm-ss-SS Z 
        //             // Example = "2019-02-17 AD 15-16-29-00 -0700"
        //             this.modifiedGeoObject.properties[attr.code] = formattedStr;
        //         }
        //     }
        // }

        let toDelete = [];
        for (var key in this.modifiedGeoObject.properties) {
            if (this.modifiedGeoObject.properties.hasOwnProperty(key)) {
                console.log(key + " -> " + this.modifiedGeoObject.properties[key]);
                if(!this.modifiedGeoObject.properties[key] || this.modifiedGeoObject.properties[key].length < 1){
                    toDelete.push(key);
                }
            }
        }

        toDelete.forEach(key => {
            delete this.modifiedGeoObject.properties[key];
        })


        let submitObj = [{  
            "actionType":"geoobject/update", // TODO: account for create
            "apiVersion":"1.0-SNAPSHOT", // TODO: make dynamic
            "createActionDate":new Date().getTime(), 
            "geoObject":this.modifiedGeoObject,
            "reason":this.reason
        }]

        this.changeRequestService.submitChangeRequest(JSON.stringify(submitObj))
	    .then( geoObject => {
            this.cancel();
	    }).catch(( err: Response ) => {
	      this.error( err.json() );
	    });
    }

    cancel(): void {
        this.currentGeoObject = null;
        this.geoObjectId = null;
        this.geoObjectType = null;
        this.modifiedTermOption = null;
        this.modifiedGeoObject = null;
        this.reason = null;
    }

    public getGeoObject(): any {
    	return this.modifiedGeoObject;
    }
    
    public error(err: any): void {
        // Handle error
        if (err !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = (err.localizedMessage || err.message);
        }
    }
}
