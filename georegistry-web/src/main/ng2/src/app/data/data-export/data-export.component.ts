import { Component, OnInit, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { ErrorModalComponent } from '../../core/modals/error-modal.component';

import { IOService } from '../../service/io.service';

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
    private types: { label: string, code: string }[]

    /*
     * Currently selected code
     */
    private code: string = null;

    /*
     * List of the hierarchies this type is part of
     */
    private hierarchies: { label: string, code: string }[] = [];

    /*
     * Currently selected hierarchy
     */
    private hierarchy: string = null;

    /*
     * Currently selected format
     */
    private format: string = null;


    /*
     * Reference to the modal current showing
     */
    private bsModalRef: BsModalRef;


    constructor( private service: IOService, private modalService: BsModalService ) { }

    ngOnInit(): void {
        this.service.listGeoObjectTypes( false ).then( types => {
            this.types = types;

        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onChange( code: string ): void {

        if ( code != null && code.length > 0 ) {
            this.service.getHierarchiesForType( code, false ).then( hierarchies => {
                this.hierarchies = hierarchies;
                this.hierarchy = null;
            } ).catch(( err: any ) => {
                this.error( err.json() );
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

    public error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.bsModalRef = this.modalService.show( ErrorModalComponent, { backdrop: true } );
            this.bsModalRef.content.message = ( err.localizedMessage || err.message );
        }
    }
}
