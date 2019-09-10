import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { GeoObjectType } from '../../../model/registry';

import { RegistryService } from '../../../service/registry.service';
import { LocalizationService } from '../../../../shared/service/localization.service';


@Component( {
    selector: 'create-geoobjtype-modal',
    templateUrl: './create-geoobjtype-modal.component.html',
    styleUrls: []
} )
export class CreateGeoObjTypeModalComponent implements OnInit {

    geoObjectType: GeoObjectType;

    message: string = null;

    /*
     * Observable subject for TreeNode changes.  Called when create is successful 
     */
    public onGeoObjTypeCreate: Subject<GeoObjectType>;

    constructor( private lService: LocalizationService, private registryService: RegistryService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {
        this.onGeoObjTypeCreate = new Subject();

        this.geoObjectType = {
            "code": "",
            "label": this.lService.create(),
            "description": this.lService.create(),
            "geometryType": "POINT",
            "isLeaf": false,
            "isGeometryEditable": true,
            "attributes": []
        };
    }

    handleOnSubmit(): void {
        this.message = null;

        this.registryService.createGeoObjectType( JSON.stringify( this.geoObjectType ) ).then( data => {
            this.onGeoObjTypeCreate.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

    }

    toggleIsLeaf(): void {
        this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
    }

    toggleIsGeometryEditable(): void {
        this.geoObjectType.isGeometryEditable = !this.geoObjectType.isGeometryEditable;
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );

            console.log( this.message );
        }
    }
}
