import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from "@angular/common/http";

import { ErrorModalComponent } from '../../../shared/component/modals/error-modal.component';

import { IOService } from '../../service/io.service';
import { AuthService } from '../../../shared/service/auth.service';

declare var acp: string;

@Component( {

    selector: 'data-export',
    templateUrl: './data-export.component.html',
    styleUrls: []
} )
export class DataExportComponent implements OnInit {

    /*
     * List of geo object types from the system
     */
    types: { label: string, code: string }[]

    /*
     * Currently selected code
     */
    code: string = null;

    /*
     * List of the hierarchies this type is part of
     */
    hierarchies: { label: string, code: string }[] = [];

    /*
     * Currently selected hierarchy
     */
    hierarchy: string = null;

    /*
     * Currently selected format
     */
    format: string = null;


    /*
     * Reference to the modal current showing
     */
    bsModalRef: BsModalRef;


    constructor( private service: IOService, private modalService: BsModalService, private authService: AuthService ) { }

    ngOnInit(): void {
        this.service.listGeoObjectTypes( false ).then( types => {
        
            //this.types = types;
            
            var myOrgTypes = [];
            for (var i = 0; i < types.length; ++i)
            {
              if (this.authService.isOrganizationRA(types[i].orgCode))
              {
                myOrgTypes.push(types[i]);
              }
            }
            this.types = myOrgTypes;

        } ).catch(( err: HttpErrorResponse) => {
            this.error( err );
        } );
    }

    onChange( code: string ): void {

        if ( code != null && code.length > 0 ) {
            this.service.getHierarchiesForType( code, false ).then( hierarchies => {
                this.hierarchies = hierarchies;
                this.hierarchy = null;
            } ).catch(( err: HttpErrorResponse) => {
                this.error( err );
            } );
        }
        else {
            this.hierarchies = [];
            this.hierarchy = null;
        }

    }

    onExport(): void {

        if ( this.format == 'SHAPEFILE' ) {
            window.location.href = acp + '/shapefile/export-shapefile?type=' + this.code + '&hierarchyType=' + this.hierarchy;
        }
        else if ( this.format == 'EXCEL' ) {
            window.location.href = acp + '/excel/export-spreadsheet?type=' + this.code + '&hierarchyType=' + this.hierarchy;
        }
    }

    public error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( (err.error && (err.error.localizedMessage || err.error.message)) || err.message || "An unspecified error has occurred" );
        }
    }
}
