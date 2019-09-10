import { Component, OnInit } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { MasterList } from '../../model/registry';

import { PublishModalComponent } from './publish-modal.component';
import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

@Component( {
    selector: 'master-list-manager',
    templateUrl: './master-list-manager.component.html',
    styleUrls: ['./master-list-manager.css']
} )
export class MasterListManagerComponent implements OnInit {
    message: string = null;
    lists: { label: string, oid: string, createDate: string, lastUpdateDate: string }[];
    /*
     * Reference to the modal current showing
    */
    bsModalRef: BsModalRef;
    
    isAdmin: boolean;
    isMaintainer: boolean;
    isContributor: boolean;

    constructor( public service: RegistryService, private modalService: BsModalService, private localizeService: LocalizationService, authService: AuthService ) {
      this.isAdmin = authService.isAdmin();
      this.isMaintainer = this.isAdmin || authService.isMaintainer();
      this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
    }

    ngOnInit(): void {
        this.service.getMasterLists().then( response => {

            this.localizeService.setLocales( response.locales );

            this.lists = response.lists;
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }

    onCreate(): void {
        this.bsModalRef = this.modalService.show( PublishModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.onMasterListChange.subscribe( list => {
            this.lists.push( { label: list.displayLabel.localizedValue, oid: list.oid, createDate: list.createDate, lastUpdateDate: list.lastUpdateDate } );
        } );
    }

    onEdit( pair: { label: string, oid: string } ): void {
        this.service.getMasterList( pair.oid ).then( list => {

            this.bsModalRef = this.modalService.show( PublishModalComponent, {
                animated: true,
                backdrop: true,
                ignoreBackdropClick: true,
            } );
            this.bsModalRef.content.edit = true;
            this.bsModalRef.content.master = list;
            this.bsModalRef.content.onMasterListChange.subscribe( ret => {
                pair.label = ret.displayLabel.localizedValue;
            } );
        } ).catch(( err: Response ) => {
            this.error( err.json() );
        } );
    }


    deleteMasterList( list: { label: string, oid: string } ): void {
        this.bsModalRef = this.modalService.show( ConfirmModalComponent, {
            animated: true,
            backdrop: true,
            ignoreBackdropClick: true,
        } );
        this.bsModalRef.content.message = this.localizeService.decode( "confirm.modal.verify.delete" ) + ' [' + list.label + ']';
        this.bsModalRef.content.submitText = this.localizeService.decode( "modal.button.delete" );

        this.bsModalRef.content.onConfirm.subscribe( data => {
            this.service.deleteMasterList( list.oid ).then( response => {
                this.lists = this.lists.filter(( value, index, arr ) => {
                    return value.oid !== list.oid;
                } );

            } ).catch(( err: Response ) => {
                this.error( err.json() );
            } );
        } );
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }

}
