import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';

import { MasterList } from '../../model/registry';

import { PublishModalComponent } from './publish-modal.component';
import { ExportFormatModalComponent } from './export-format-modal.component';

import { RegistryService } from '../../service/registry.service';
import { ProgressService } from '../../service/progress.service';
import { LocalizationService } from '../../core/service/localization.service';

import { GeoObjectEditorComponent } from '../geoobject-editor/geoobject-editor.component';

import { AuthService } from '../../core/auth/auth.service';

declare var acp: string;

@Component( {
    selector: 'master-list',
    templateUrl: './master-list.component.html',
    styleUrls: []
} )
export class MasterListComponent implements OnInit {
    message: string = null;
    list: MasterList = null;
    p: number = 1;
    current: string = '';
    filter: string = '';
    page: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 10,
        results: []
    };
    sort = { attribute: 'code', order: 'ASC' };

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    public searchPlaceholder = "";

    private isAdmin: boolean;
    private isMaintainer: boolean;
    private isContributor: boolean;


    constructor( public service: RegistryService, private pService: ProgressService, private route: ActivatedRoute, private router: Router,
        private modalService: BsModalService, private localizeService: LocalizationService, authService: AuthService ) {

        this.searchPlaceholder = localizeService.decode( "masterlist.search" );

        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get( 'oid' );

        this.service.getMasterList( oid ).then( list => {
            this.list = list;

            this.onPageChange( 1 );
        } );
    }

    onPageChange( pageNumber: number ): void {
        this.service.data( this.list.oid, pageNumber, this.page.pageSize, this.filter, this.sort ).then( page => {
            this.page = page;
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    onSearch(): void {
        this.filter = this.current;

        this.onPageChange( 1 );
    }

    onSort( attribute: { name: string, label: string } ): void {
        if ( this.sort.attribute === attribute.name ) {
            this.sort.order = ( this.sort.order === 'ASC' ? 'DESC' : 'ASC' );
        }
        else {
            this.sort = { attribute: attribute.name, order: 'ASC' };
        }

        this.onPageChange( 1 );
    }

    onEdit( data ): void {
        let editModal = this.modalService.show( GeoObjectEditorComponent, { backdrop: true } );
        editModal.content.fetchGeoObject( data.code, this.list.typeCode );
        editModal.content.fetchGeoObjectType( this.list.typeCode );
        editModal.content.setMasterListId( this.list.oid );
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            this.onPageChange( this.page.pageNumber );
        } );
    }

    onGoto( data ): void {
        const oid = data.originalOid;

        if ( oid != null && oid.length > 0 ) {
            window.open( acp + "/nav/management#/locations/" + oid, '_blank' );
        }

    }

    onPublish(): void {
        let subscription = Observable.interval( 1000 ).subscribe(() => {
            this.service.progress( this.list.oid ).then( progress => {
                this.pService.progress( progress );
            } );
        } );

        this.service.publishMasterList( this.list.oid ).finally(() => {
            subscription.unsubscribe();

            this.pService.complete();
        } ).toPromise()
            .then( response => {
                return response.json() as MasterList;
            } )
            .then( list => {
                this.list = list;

                // Refresh the resultSet
                this.onPageChange( 1 );
            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );

        this.pService.start();
    }

    onView( event: any ): void {
        event.preventDefault();

        this.bsModalRef = this.modalService.show( PublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.readonly = true;
        this.bsModalRef.content.master = this.list;
    }

    onExport(): void {
        this.bsModalRef = this.modalService.show( ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.onFormat.subscribe( format => {
            if ( format == 'SHAPEFILE' ) {
                window.location.href = acp + '/master-list/export-shapefile?oid=' + this.list.oid;
            }
            else if ( format == 'EXCEL' ) {
                window.location.href = acp + '/master-list/export-spreadsheet?oid=' + this.list.oid;
            }
        } );
    }


    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }

}
