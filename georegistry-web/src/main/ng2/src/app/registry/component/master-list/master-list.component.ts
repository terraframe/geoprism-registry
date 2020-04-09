import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterListVersion } from '../../model/registry';

import { ExportFormatModalComponent } from './export-format-modal.component';

import { RegistryService } from '../../service/registry.service';
import { ProgressService } from '../../../shared/service/progress.service';
import { LocalizationService } from '../../../shared/service/localization.service';

import { GeoObjectEditorComponent } from '../geoobject-editor/geoobject-editor.component';

import { AuthService } from '../../../shared/service/auth.service';

declare var acp: string;

@Component( {
    selector: 'master-list',
    templateUrl: './master-list.component.html',
    styleUrls: []
} )
export class MasterListComponent implements OnInit {
    message: string = null;
    list: MasterListVersion = null;
    p: number = 1;
    current: string = '';
    filter: { attribute: string, value: string, label: string }[] = [];
    selected: string[] = [];
    page: any = {
        count: 0,
        pageNumber: 1,
        pageSize: 100,
        results: []
    };
    sort = { attribute: 'code', order: 'ASC' };
    isPublished: boolean = true;

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
        this.isPublished = (this.route.snapshot.paramMap.get( 'published' ) == "true");

        this.service.getMasterListVersion( oid ).then( version => {
            this.list = version;
            this.list.attributes.forEach( attribute => {
                attribute.isCollapsed = true;
            } );

            this.onPageChange( 1 );
        } );
    }

    onPageChange( pageNumber: number ): void {

        this.message = null;

        this.service.data( this.list.oid, pageNumber, this.page.pageSize, this.filter, this.sort ).then( page => {
            this.page = page;
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    //    onSearch(): void {
    //        this.filter = this.current;
    //
    //        this.onPageChange( 1 );
    //    }

    onSort( attribute: { name: string, label: string } ): void {
        if ( this.sort.attribute === attribute.name ) {
            this.sort.order = ( this.sort.order === 'ASC' ? 'DESC' : 'ASC' );
        }
        else {
            this.sort = { attribute: attribute.name, order: 'ASC' };
        }

        this.onPageChange( 1 );
    }

    clearFilters(): void {
        this.list.attributes.forEach( attr => {
            attr.search = null;
        } );

        this.filter = [];
        this.selected = [];

        this.onPageChange( 1 );
    }

    toggleFilter( attribute: any ): void {
        attribute.isCollapsed = !attribute.isCollapsed;
    }

    getValues( attribute: any ): void {
        return Observable.create(( observer: any ) => {
            this.message = null;

            // Get the valid values
            this.service.values( this.list.oid, attribute.search, attribute.name, attribute.base, this.filter ).then( options => {
                options.unshift( { label: '[' + this.localizeService.decode( "masterlist.nofilter" ) + ']', value: null } );

                observer.next( options );
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        } );
    }


    handleDateChange( attribute: any ): void {
        attribute.isCollapsed = true;

        // Remove the current attribute filter if it exists
        this.filter = this.filter.filter( f => f.attribute !== attribute.base );
        this.selected = this.selected.filter( s => s !== attribute.base );

        if ( attribute.value != null && ( attribute.value.start !== '' || attribute.value.end !== '' ) ) {

            let label = '[' + attribute.label + '] : [';

            if ( attribute.value.start != null ) {
                label += attribute.value.start;
            }

            if ( attribute.value.start != null && attribute.value.end != null ) {
                label += ' - ';
            }

            if ( attribute.value.end != null ) {
                label += attribute.value.end;
            }

            label += ']';

            this.filter.push( { attribute: attribute.base, value: attribute.value, label: label } );
            this.selected.push( attribute.base );
        }

        this.onPageChange( 1 );
    }

    handleInputChange( attribute: any ): void {
        attribute.isCollapsed = true;

        // Remove the current attribute filter if it exists
        this.filter = this.filter.filter( f => f.attribute !== attribute.base );
        this.selected = this.selected.filter( s => s !== attribute.base );

        if ( attribute.value != null && attribute.value !== '' ) {
            const label = '[' + attribute.label + '] : ' + '[' + attribute.value + ']';

            this.filter.push( { attribute: attribute.base, value: attribute.value, label: label } );
            this.selected.push( attribute.base );
        }

        this.onPageChange( 1 );
    }

    handleListChange( e: TypeaheadMatch, attribute: any ): void {
        attribute.value = e.item;
        attribute.isCollapsed = true;

        // Remove the current attribute filter if it exists
        this.filter = this.filter.filter( f => f.attribute !== attribute.base );
        this.selected = this.selected.filter( s => s !== attribute.base );

        this.list.attributes.forEach( attr => {
            if ( attr.base === attribute.base ) {
                attr.search = '';
            }
        } );

        if ( attribute.value.value != null && attribute.value.value !== '' ) {
            const label = '[' + attribute.label + '] : ' + '[' + attribute.value.label + ']';

            this.filter.push( { attribute: attribute.base, value: e.item.value, label: label } );
            this.selected.push( attribute.base );
            attribute.search = e.item.label;
        }
        else {
            attribute.search = '';
        }

        this.onPageChange( 1 );
    }

    isFilterable( attribute: any ): boolean {
        return attribute.type !== 'none' && ( attribute.dependency.length === 0 || this.selected.indexOf( attribute.base ) !== -1 || this.selected.filter( value => attribute.dependency.includes( value ) ).length > 0 );
    }

    onEdit( data ): void {
        let editModal = this.modalService.show( GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true } );
        editModal.content.configureAsExisting( data.code, this.list.typeCode, this.list.forDate, this.list.isGeometryEditable );
        editModal.content.setMasterListId( this.list.oid );
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            this.onPageChange( this.page.pageNumber );
        } );
    }

    onGoto( data ): void {
        const oid = data.code;

        if ( oid != null && oid.length > 0 ) {
            window.open( acp + "/nav/management#/locations/" + oid, '_blank' );
        }

    }

    onPublish(): void {
        this.message = null;

        let subscription = Observable.interval( 1000 ).subscribe(() => {
            this.service.progress( this.list.oid ).then( progress => {
                this.pService.progress( progress );
            } );
        } );

        this.service.publishMasterList( this.list.oid ).finally(() => {
            subscription.unsubscribe();

            this.pService.complete();
        } ).toPromise()
            .then( list => {
                this.list = list;
                this.list.attributes.forEach( attribute => {
                    attribute.isCollapsed = true;
                } );

                // Refresh the resultSet
                this.onPageChange( 1 );
            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );

        this.pService.start();
    }

    onNewGeoObject(): void {
        let editModal = this.modalService.show( GeoObjectEditorComponent, { backdrop: true, ignoreBackdropClick: true } );
        //editModal.content.fetchGeoObject( data.code, this.list.typeCode );
        editModal.content.configureAsNew( this.list.typeCode, this.list.forDate, this.list.isGeometryEditable );
        editModal.content.setMasterListId( this.list.oid );
        editModal.content.setOnSuccessCallback(() => {
            // Refresh the page
            this.onPageChange( this.page.pageNumber );
        } );
    }

    onExport(): void {
        this.bsModalRef = this.modalService.show( ExportFormatModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.onFormat.subscribe( format => {
            if ( format == 'SHAPEFILE' ) {
                window.location.href = acp + '/master-list/export-shapefile?oid=' + this.list.oid + "&filter=" + encodeURIComponent( JSON.stringify( this.filter ) );
            }
            else if ( format == 'EXCEL' ) {
                window.location.href = acp + '/master-list/export-spreadsheet?oid=' + this.list.oid + "&filter=" + encodeURIComponent( JSON.stringify( this.filter ) );
            }
        } );
    }


    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
