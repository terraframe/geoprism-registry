import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { GeoObjectType } from '../../hierarchy/hierarchy';
import { ShapefileConfiguration } from '../shapefile';

import { ShapefileService } from '../../../service/shapefile.service';

@Component( {
    selector: 'shapefile-modal',
    templateUrl: './shapefile-modal.component.html',
    styleUrls: []
} )
export class ShapefileModalComponent implements OnInit {

    configuration: ShapefileConfiguration;
    message: string = null;
    state: string = 'MAP';

    constructor( private service: ShapefileService, public bsModalRef: BsModalRef ) {
    }

    ngOnInit(): void {
    }

    onStateChange( event: string ): void {
        if ( event === 'BACK' ) {
            this.handleBack();
        }
        else if ( event === 'NEXT' ) {
            this.handleNext();
        }
        else if ( event === 'CANCEL' ) {
            this.handleCancel();
        }
    }

    handleBack(): void {
        if ( this.state === 'LOCATION' ) {
            this.state = 'MAP';
        }
    }

    handleNext(): void {
        if ( this.state === 'MAP' ) {
            this.state = 'LOCATION';
        }
        else if ( this.state === 'LOCATION' ) {
            this.handleSubmit();
        }
    }

    handleSubmit(): void {
        this.service.importShapefile( this.configuration ).then( response => {
            this.bsModalRef.hide()
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

    }

    handleCancel(): void {
        this.service.cancelImport( this.configuration ).then( response => {
            this.bsModalRef.hide()
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }
}
