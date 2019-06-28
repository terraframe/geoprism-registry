import { Component, OnInit, Input } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { ParentTreeNode, GeoObject, LocalizedValue } from '../../model/registry';
import { RegistryService } from '../../service/registry.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

@Component( {

    selector: 'cascading-geo-selector',
    templateUrl: './cascading-geo-selector.html',
} )
export class CascadingGeoSelector {

    @Input() hierarchies: any;
    
    parentMap: any = {};

    bsModalRef: BsModalRef;

    constructor( private modalService: BsModalService, private registryService: RegistryService ) {
      
    }
    
    ngOnInit(): void {
      for (var i = 0; i < this.hierarchies.length; ++i)
      {
        var hierarchy = this.hierarchies[i];
      
        for (var j = 0; j < hierarchy.parents.length; ++j)
        {
          var ptn = new ParentTreeNode();
          
          ptn.geoObject = new GeoObject();
          ptn.geoObject.properties = {
            uid: "",
            code: "",
            displayLabel: new LocalizedValue(),
            type: "",
            status: [""],
            sequence: "",
            createDate: "",
            lastUpdateDate: ""
          };
          
          hierarchy.parents[j].ptn = ptn;
        }
      }
    }
    
    getTypeAheadObservable(text, typeCode)
    {
      console.log("getTypeAheadObservable", text, typeCode);

      return Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(text, typeCode).then(results => {
                observer.next(results);
            });
        });
    }

    typeaheadOnSelect(e: TypeaheadMatch, ptn: ParentTreeNode): void {
      console.log("typeaheadOnSelect", e, ptn);

        this.registryService.getGeoObjectByCode(e.item.code, ptn.geoObject.properties.type)
            .then(geoObject => {

              ptn.geoObject = geoObject;

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
    
    public getParents(): ParentTreeNode {
      return null;
    }

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            let bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }

}
