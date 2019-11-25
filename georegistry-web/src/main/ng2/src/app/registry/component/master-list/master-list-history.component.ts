import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList } from '../../model/registry';

import { PublishModalComponent } from './publish-modal.component';
import { ExportFormatModalComponent } from './export-format-modal.component';

import { RegistryService } from '../../service/registry.service';
import { ProgressService } from '../../../shared/service/progress.service';
import { LocalizationService } from '../../../shared/service/localization.service';

import { GeoObjectEditorComponent } from '../geoobject-editor/geoobject-editor.component';

import { AuthService } from '../../../shared/service/auth.service';

declare var acp: string;

@Component( {
    selector: 'master-list-history',
    templateUrl: './master-list-history.component.html',
    styleUrls: []
} )
export class MasterListHistoryComponent implements OnInit {
    message: string = null;
    history: any = null;
    publishMLVersion: {forDate: Date};

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

    /*
     * Reference to the modal current showing
    */
    private bsModalRef: BsModalRef;

    private isAdmin: boolean;
    private isMaintainer: boolean;
    private isContributor: boolean;


    constructor( public service: RegistryService, private pService: ProgressService, private route: ActivatedRoute, private router: Router,
        private modalService: BsModalService, private localizeService: LocalizationService, authService: AuthService ) {

        this.isAdmin = authService.isAdmin();
        this.isMaintainer = this.isAdmin || authService.isMaintainer();
        this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        const oid = this.route.snapshot.paramMap.get( 'oid' );

        // this.service.getMasterListHistory( oid ).then( listHist => {
        //     this.history = listHist;

        //     // this.onPageChange( 1 );
        // } );

        this.history = this.service.getMasterListHistory(oid);
        
    }


    onEdit( data ): void {
        // let editModal = this.modalService.show( GeoObjectEditorComponent, { backdrop: true } );
        // editModal.content.configureAsExisting( data.code, this.list.typeCode );
        // editModal.content.setMasterListId( this.list.oid );
        // editModal.content.setOnSuccessCallback(() => {
        //     // Refresh the page
        //     this.onPageChange( this.page.pageNumber );
        // } );
    }


    onPublish(): void {
        this.message = null;

        // let subscription = Observable.interval( 1000 ).subscribe(() => {
        //     this.service.progress( this.list.oid ).then( progress => {
        //         this.pService.progress( progress );
        //     } );
        // } );

        // this.service.publishMasterList( this.list.oid ).finally(() => {
        //     subscription.unsubscribe();

        //     this.pService.complete();
        // } ).toPromise()
        //     .then( list => {
        //         this.list = list;
        //         this.list.attributes.forEach( attribute => {
        //             attribute.isCollapsed = true;
        //         } );

        //         // Refresh the resultSet
        //         this.onPageChange( 1 );
        //     } ).catch(( err: HttpErrorResponse ) => {
        //         this.error( err );
        //     } );

        // this.pService.start();
    }

    onView( oid: string ): void {
        event.preventDefault();

        this.router.navigate(['/registry/master-list/', oid])
    }


    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
