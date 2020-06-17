import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from "@angular/common/http";

import { GeoObjectType } from '../../../model/registry';

import { RegistryService } from '../../../service/registry.service';
import { LocalizationService } from '../../../../shared/service/localization.service';
import { Organization } from '../../../../shared/model/core';


@Component( {
    selector: 'create-geoobjtype-modal',
    templateUrl: './create-geoobjtype-modal.component.html',
    styleUrls: []
} )
export class CreateGeoObjTypeModalComponent implements OnInit {

    geoObjectType: GeoObjectType;
    organizations: any = [];
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
            "organizationCode": "",
            "attributes": []
        };

        this.registryService.getOrganizations().then(orgs => {
          if (orgs.length === 1)
          {
            this.geoObjectType.organizationCode = orgs[0].code;
          }
          this.organizations = orgs;
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    handleOnSubmit(): void {
        this.message = null;

        this.registryService.createGeoObjectType( JSON.stringify( this.geoObjectType ) ).then( data => {
            this.onGeoObjTypeCreate.next( data );
            this.bsModalRef.hide();
        } ).catch(( err: HttpErrorResponse) => {
            this.error( err );
        } );

    }

    toggleIsLeaf(): void {
        this.geoObjectType.isLeaf = !this.geoObjectType.isLeaf;
    }

    toggleIsGeometryEditable(): void {
        this.geoObjectType.isGeometryEditable = !this.geoObjectType.isGeometryEditable;
    }

    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );

            console.log( this.message );
        }
    }
}
