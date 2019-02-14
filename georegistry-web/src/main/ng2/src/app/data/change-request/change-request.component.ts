import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term } from '../../model/registry';

import { Observable} from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;

@Component( {
    selector: 'change-request',
    templateUrl: './change-request.component.html',
    styleUrls: []
} )
export class ChangeRequestComponent implements OnInit {

    objectKeys = Object.keys;
    
    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;

    geoObjectTypes: GeoObjectType[] = [];
    currentGeoObjectType: GeoObjectType;
    currentGeoObjectId: string = "";
    currentGeoObject: GeoObject;
    modifiedCurrentGeoObject: GeoObject;
    modifiedAttribute: Attribute;

    reason: string = "";

    ///////////////

    asyncSelected: string;
  typeaheadLoading: boolean;
  typeaheadNoResults: boolean;
  dataSource: Observable<any>;

  /////////////////////


    constructor( private service: IOService, private modalService: BsModalService, private hierarchyService: HierarchyService,
                private registryService: RegistryService, private elRef:ElementRef) {

        this.dataSource = Observable.create((observer: any) => {
            // Runs on every search
            observer.next(this.asyncSelected);
        }).pipe(
                mergeMap((token: string) => this.getStatesAsObservable(token))
            );
    }

    ngOnInit(): void {
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
        console.log(event)
        let selectedGeoObjectTypeCode = event.target.value;

    }

    onSelectGeoObject(event: any): void {

        let termAttr = new AttributeTerm("testTermCode", "term", "Test Term", "Test Description", false);
        termAttr.setRootTerm( new Term("code", "label", "desc") )
        // TODO: how are term options represented?

        this.currentGeoObject =   {"type":"Feature",
                        "geometry":{
                            "type":"MultiPolygon",
                            "coordinates":[[105.96858200372009,13.230785670678037],[105.9699149999099,13.230781744931829]]
                        },
                            "properties":{
                                "uid":"245f851a-6089-494e-b743-00000027b225",
                                "sequence":"",
                                "code":"855 100401",
                                "localizedDisplayLabel":"Boeng Char",
                                "lastUpdateDate":"2019-02-13 AD 17-16-12-00 -0700",
                                "type":"Cambodia_Commune",
                                "createDate":"2019-02-13 AD 17-16-12-00 -0700",
                                "status":["CGR:Status-Active"],
                                "testText": new Attribute("testTextCode", "text", "Test Text", "Test Description", false),
                                "testInteger": new Attribute("testIntCode", "integer", "Test Integer", "Test Description", false),
                                "testDecimal": new AttributeDecimal("testTextCode", "float", "Test Decimal", "Test Description", false),
                                "testDate": new Attribute("testDateCode", "date", "Test Date", "Test Description", false),
                                "testBoolean": new Attribute("testBooleanCode", "boolean", "Test Boolean", "Test Description", false),
                                "testTerm": termAttr
                            }
                        }
        this.modifiedCurrentGeoObject = JSON.parse(JSON.stringify(this.currentGeoObject));

        // this.registryService.getGeoObjectByCode(this.currentGeoObjectId, this.currentGeoObjectType.code)
        // .then( geoObject => {
        //         console.log(geoObject)
        // }).catch(( err: Response ) => {
        //     this.error( err.json() );
        // });
    }

    getStatesAsObservable(token: string): Promise<any> {
 
        return this.registryService.getGeoObjectSuggestions(this.currentGeoObjectId, this.currentGeoObjectType.code, "parent", "hierarchy")
        .then( geoObjects => {
                
        }).catch(( err: Response ) => {
            this.error( err.json() );
        });
      
  }
 
  changeTypeaheadLoading(e: boolean): void {
    this.typeaheadLoading = e;
  }
 
  typeaheadOnSelect(e: TypeaheadMatch): void {
    console.log('Selected value: ', e.value);
  }

  isAttribute(prop: any): boolean {
      console.log("is?")

      if(prop.type && prop.type === "term"){
          return true;
      }

      return false;
  }

  isFormValid(): boolean {
      return true;
  }

  editProp(prop:any, event:any): void {
    console.log("edit")

    this.modifiedAttribute = prop;

    // let elem = <HTMLInputElement> document.getElementById("mod-"+propKey);
    // elem.disabled = false;


  }

  cancel(): void {
      this.currentGeoObject = null;
      this.currentGeoObjectId = null;
      this.currentGeoObjectType = null;
  }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
