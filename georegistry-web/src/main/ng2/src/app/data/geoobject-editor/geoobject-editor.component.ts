import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, Input, Output, EventEmitter } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { DatePipe } from '@angular/common';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';
import { AttributeInputComponent } from '../hierarchy/geoobjecttype-management/attribute-input.component';

import { HierarchyService } from '../../service/hierarchy.service';
import { RegistryService } from '../../service/registry.service';
import { ChangeRequestService } from '../../service/change-request.service';


import { IOService } from '../../service/io.service';
import { GeoObjectType, GeoObject, Attribute, AttributeTerm, AttributeDecimal, Term, ParentTreeNode } from '../../model/registry';

import { GeoObjectAttributeExcludesPipe } from '../../data/geoobject-shared-attribute-editor/geoobject-attribute-excludes.pipe';
import { ToEpochDateTimePipe } from '../../data/submit-change-request/to-epoch-date-time.pipe';

import { Observable } from 'rxjs';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { mergeMap } from 'rxjs/operators';

declare var acp: string;


@Component({
    selector: 'geoobject-editor',
    templateUrl: './geoobject-editor.component.html',
    styleUrls: ['./geoobject-editor.component.css'],
    providers: [DatePipe]
})

/**
 * This component is used in the master list when editing a row. In the future it will also be used by the navigator and has
 * potential to also be used in the submit change request and manage change requests.
 */
export class GeoObjectEditorComponent implements OnInit {
    
  /*
	 * The current state of the GeoObject in the GeoRegistry
	 */
    @Input() preGeoObject: GeoObject = null;
	
	/*
	 * The state of the GeoObject after our edit has been applied 
	 */
    @Input() postGeoObject: any = {};

    @Input() geoObjectType: GeoObjectType;
    
    @ViewChild("attributeEditor") attributeEditor;
    
    isValid: boolean = false;
    
    tabIndex: number = 0;
    
    parentTreeNode: ParentTreeNode;
    
    private dataSource: Observable<any>;
    
    private masterListId: string;
    
    @Input() onSuccessCallback: Function;

    constructor(private service: IOService, private modalService: BsModalService, public bsModalRef: BsModalRef, private changeDetectorRef: ChangeDetectorRef,
            private registryService: RegistryService, private elRef: ElementRef, private changeRequestService: ChangeRequestService,
            private date: DatePipe, private toEpochDateTimePipe: ToEpochDateTimePipe) {
    }
    
    ngOnInit(): void {
      // TODO : Remove this code when its actually being used for real
      if (this.preGeoObject == null)
      {
        // this.fetchGeoObject("855 01090201", "Cambodia_Village");
        // this.fetchGeoObjectType("Cambodia_Village");
      }
    }
    
    setMasterListId(id: string)
    {
      this.masterListId = id;
    }
    
    setOnSuccessCallback(func: Function)
    {
      this.onSuccessCallback = func;
    }
    
    private fetchGeoObject(code: string, typeCode: string)
    {
      this.registryService.getGeoObjectByCode(code, typeCode)
            .then(geoObject => {
                this.preGeoObject = geoObject;
                this.postGeoObject = JSON.parse(JSON.stringify(this.preGeoObject)); // Object.assign is a shallow copy. We want a deep copy.
                
                this.fetchParents(geoObject);
            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
    
    private fetchGeoObjectType(code: string)
    {
      this.registryService.getGeoObjectTypes([code])
            .then(geoObjectType => {
                this.geoObjectType = geoObjectType[0];

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
    
    private fetchParents(go: GeoObject)
    {
      this.registryService.getParentGeoObjects(go.properties.uid, go.properties.type, [], true)
        .then( (ptn: ParentTreeNode) => {
        
          if (ptn != null && ptn.parents != null && ptn.parents.length > 0)
          {
            this.parentTreeNode = ptn;
          }
          
        }).catch((err: Response) => {
            this.error(err.json());
        });
    }
    
    getTypeAheadObservable(text, typeCode)
    {
      return Observable.create((observer: any) => {
            this.registryService.getGeoObjectSuggestionsTypeAhead(text, typeCode).then(results => {
                observer.next(results);
            });
        });
    }
    
    typeaheadOnSelect(e: TypeaheadMatch, ptn: ParentTreeNode): void {
        this.registryService.getGeoObjectByCode(e.item.code, ptn.geoObject.properties.type)
            .then(geoObject => {
            
              ptn.geoObject = geoObject;

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
    
    private onValidChange(newValid: boolean)
    {
      if (this.preGeoObject == null) {
        this.isValid = false;
        return;
      }
      this.isValid = newValid;
    }
    
    public error(err: any): void {
        // TODO
        
        // Handle error
        //if (err !== null) {
        //    this.bsModalRef = this.modalService.show(ErrorModalComponent, { backdrop: true });
        //    this.bsModalRef.content.message = (err.localizedMessage || err.message);
        //}
    }
    
    public cancel(): void {
      this.bsModalRef.hide();
    }
    
    public submit(): void {
      this.bsModalRef.hide();
      
      this.registryService.applyGeoObjectEdit(this.parentTreeNode, this.postGeoObject, this.masterListId)
          .then( () => {
          
              if (this.onSuccessCallback != null)
              {
                this.onSuccessCallback();
              }

            }).catch((err: Response) => {
                this.error(err.json());
            });
    }
}
