import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/change-request/geoobject-attribute-excludes.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component({
    selector: 'change-request',
    templateUrl: './change-request.component.html',
    styleUrls: ['./change-request.css']
})

export class ChangeRequestComponent implements OnInit {

    objectKeys = Object.keys;
    @Input() standAlone = true;

    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    geoObjectTypes: GeoObjectType[] = [];
    @Input() geoObjectType: GeoObjectType = {
            "code": "Cambodia_Commune",
            "localizedLabel": "Commune",
            "localizedDescription": "",
            "geometryType": "MULTIPOLYGON",
            "isLeaf": false,
            "attributes": [
                {
                    "code": "uid",
                    "type": "character",
                    "localizedLabel": "UID",
                    "localizedDescription": "The internal globally unique identifier ID",
                    "isDefault": true
                },
                {
                    "code": "sequence",
                    "type": "integer",
                    "localizedLabel": "Sequence",
                    "localizedDescription": "The sequence number of the GeoObject that is incremented when the object is updated",
                    "isDefault": true
                },
                {
                    "code": "code",
                    "type": "character",
                    "localizedLabel": "Code",
                    "localizedDescription": "Human readable unique identified",
                    "isDefault": true
                },
                {
                    "code": "localizedDisplayLabel",
                    "type": "character",
                    "localizedLabel": "Localized Display Label",
                    "localizedDescription": "Localized locaiton ",
                    "isDefault": true
                },
                {
                    "code": "lastUpdateDate",
                    "type": "date",
                    "localizedLabel": "Date Last Updated",
                    "localizedDescription": "The date the object was updated",
                    "isDefault": true
                },
                {
                    "code": "type",
                    "type": "character",
                    "localizedLabel": "Type",
                    "localizedDescription": "The type of the GeoObject",
                    "isDefault": true
                },
                {
                    "code": "createDate",
                    "type": "date",
                    "localizedLabel": "Date Created",
                    "localizedDescription": "The date the object was created",
                    "isDefault": true
                },



                {
                    "code": "testCharacter",
                    "type": "character",
                    "localizedLabel": "Test Character",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                },
                {
                    "code": "testInteger",
                    "type": "integer",
                    "localizedLabel": "Test Integer",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                },
                {
                    "code": "testFloat",
                    "type": "float",
                    "localizedLabel": "Test Float",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                },
                {
                    "code": "testDate",
                    "type": "date",
                    "localizedLabel": "Test Date",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                },
                {
                    "code": "testCharacter",
                    "type": "character",
                    "localizedLabel": "Test Character",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                },
                {
                    "code": "testBoolean",
                    "type": "boolean",
                    "localizedLabel": "Test Boolean",
                    "localizedDescription": "The date the object was created",
                    "isDefault": false
                }


            ]
        }
    geoObjectId: string = "";
    @Input() currentGeoObject: GeoObject = null;
    modifiedGeoObject: GeoObject;
    modifiedTermOption: Term = null;
    currentTermOption: Term = null;
    reason: string = "";
    enabledInput: string;
    geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate", "status"];

    asyncSelected: string;
    typeaheadLoading: boolean;
    typeaheadNoResults: boolean;
    dataSource: Observable<any>;


    constructor(private service: IOService, private modalService: BsModalService,
        private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService) {

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

    onSelectGeoObjectType(event: any): void {
        let selectedGeoObjectTypeCode = event.target.value;

    }

    onSelectPropertyOption(event: any): void {
        this.currentTermOption = JSON.parse(JSON.stringify(this.modifiedTermOption));
    }

    changeTypeaheadLoading(e: boolean): void {
        this.typeaheadLoading = e;
    }

    typeaheadOnSelect(e: TypeaheadMatch): void {

        // this.modifiedGeoObject = e.value ;

        this.registryService.getGeoObjectByCode(e.item.code, this.geoObjectType.code)
            .then(geoObject => {

                let term: Term = new Term("termCode", "term label", "term description");
                term.addChild(new Term("optCode", "opt label", "opt description"))
                term.addChild(new Term("optCode2", "opt label 2", "opt description 2"))

                let test = {
                    "testInteger": 3,
                    "testBoolean": false,
                    "testFloat": 1.111,
                    "testCharacter": "Test Character Value",
                    // "testTerm": [
                    //     "CGR:Status-Pending"
                    // ],
                    "testTerm": term,
                    "testDate": "2019-02-16 AD 17-15-38-17 -0700"
                };

                geoObject.properties = Object.assign({}, geoObject.properties, test);

                this.currentGeoObject = geoObject;
                this.modifiedGeoObject = JSON.parse(JSON.stringify(geoObject))

            }).catch((err: Response) => {
                this.error(err.json());
            });
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

    isAttribute(prop: any): boolean {
        for(let i=0; i<this.geoObjectType.attributes.length; i++){
            let attr = this.geoObjectType.attributes[i];
            if(attr.code === prop){
                if(attr instanceof Attribute){
                    // if(typeof prop === 'object' && prop != null && !Array.isArray(prop)) {
                    //     return true;
                    // }
                    return true;
                }
            }
        }

        return false;
    }

    isAttributeTerm(prop: Term): boolean {
        // (prop instanceof Term) doesn't work when Term obj isn't instantiated in Angular
        // so we have to check the object ourselves.
        if (prop.children && prop != null) {
            return true;
        }

        return false;
    }

    isArray(obj: any) {
        return Array.isArray(obj)
    }


    isFormValid(): boolean {
        return true;
    }

    editProp(prop: any, elementId: string, event: any): void {
        this.enabledInput = elementId;

        let elem = <HTMLInputElement>document.getElementById(elementId);
        elem.disabled = !elem.disabled;

        if (prop.type === "term") {
            if (this.modifiedTermOption) {
                this.modifiedTermOption = null;
            }

            this.modifiedTermOption = prop;
        }
    }

    submit(): void {

        let submitObj = [{  
            "actionType":"geoobject/update", // TODO: account for create
            "apiVersion":"1.0-SNAPSHOT", // TODO: make dynamic
            "createActionDate":new Date().getTime(), 
            "geoObject":this.modifiedGeoObject
        }]

        this.changeRequestService.submitChangeRequest(JSON.stringify(submitObj));
    }

    cancel(): void {
        this.currentGeoObject = null;
        this.geoObjectId = null;
        this.geoObjectType = null;
        this.modifiedTermOption = null;
        this.modifiedGeoObject = null;
        this.reason = null;
    }

    public error(err: any): void {
        // Handle error
        if (err !== null) {
            this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
            this.bsModalRef.content.message = (err.localizedMessage || err.message);
        }
    }
}
