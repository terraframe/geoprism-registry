import { Component, OnInit, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';
import { HttpErrorResponse } from '@angular/common/http';

import { GeoObjectType, MasterList, Attribute } from '../../model/registry';

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
    onAttributeVersionChange: Subject<MasterList>;

    attr: any;
    valAdded: boolean = false;

    @Input("attribute") 
    set attribute(attribute: any) {
        this.attr = attribute;
        this.versions = this.service.getAttributeVersions(attribute.code);
    }
    versions: any;

    newVersion: {value:string, from:Date, to:string};

    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

		this.onAttributeVersionChange = new Subject();

    }

    onAddNewVersion(): void {
        console.log(this.attr);

        this.versions.versions.push({id: Math.random() ,value: this.attr, from: new Date(), to: new Date(), removable: true});

        this.valAdded = true;

        // TODO: persist new value and inject returned data into list
    }

    remove(version: any ): void {
        for(let i=0; i<this.versions.versions.length; i++){
            if(this.versions.versions[i].id === version.id){
                this.versions.versions.splice(i, 1);
                this.valAdded = false;
            }
        }
    }

    onSubmit(): void {
        // this.service.createMasterList( this.master ).then( response => {

        //     this.onMasterListChange.next( response );
        //     this.bsModalRef.hide();
        // } ).catch(( err: HttpErrorResponse) => {
        //     this.error( err );
        // } );
        
        this.onAttributeVersionChange.next( this.versions );

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
