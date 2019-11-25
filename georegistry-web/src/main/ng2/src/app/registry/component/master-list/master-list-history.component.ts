import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, ParamMap } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList, MasterListVersion } from '../../model/registry';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { PublishModalComponent } from './publish-modal.component';

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
    list: MasterList = null;
    forDate: string = '';

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

        this.service.getMasterListHistory( oid ).then( list => {
            this.list = list;
        } );
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

        this.service.createMasterListVersion( this.list.oid, this.forDate ).then( version => {
            this.list.versions.push( version );

        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    onViewMetadata( event: any ): void {
        event.preventDefault();

        this.bsModalRef = this.modalService.show( PublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.readonly = true;
        this.bsModalRef.content.master = this.list;
    }


    onView( version: MasterListVersion ): void {
        event.preventDefault();

        this.router.navigate( ['/registry/master-list/', version.oid] )
    }

    onDelete( version: MasterListVersion ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + version.forDate + ']';
        this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );

        this.bsModalRef.content.onConfirm.subscribe( data => {
            this.service.deleteMasterListVersion( version.oid ).then( response => {
                this.list.versions = this.list.versions.filter(( value, index, arr ) => {
                    return value.oid !== version.oid;
                } );

            } ).catch(( err: HttpErrorResponse ) => {
                this.error( err );
            } );
        } );
    }


    error( err: HttpErrorResponse ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.error.localizedMessage || err.error.message || err.message );
        }
    }

}
