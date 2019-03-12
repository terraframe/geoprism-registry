import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subject } from 'rxjs/Subject';

import { GeoObjectType, MasterList } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { LocalizationService } from '../../core/service/localization.service';

@Component( {
    selector: 'publish-modal',
    templateUrl: './publish-modal.component.html',
    styleUrls: []
} )
export class PublishModalComponent implements OnInit {
    message: string = null;
    master: MasterList;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
    public onMasterListChange: Subject<MasterList>;


    /*
     * List of geo object types from the system
     */
    private types: { label: string, code: string }[]


    constructor( private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef ) { }

    ngOnInit(): void {

        this.onMasterListChange = new Subject();

        this.iService.listGeoObjectTypes( true ).then( types => {
            this.types = types;
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );

        this.master = {
            oid: '',
            typeCode: '',
            displayLabel: this.lService.create(),
            code: '',
            representativityDate: null,
            publishDate: null,
            listAbstract: '',
            process: '',
            progress: '',
            accessConstraints: '',
            useConstraints: '',
            acknowledgements: '',
            disclaimer: '',
            contactName: '',
            organization: '',
            telephoneNumber: '',
            email: '',
            hierarchies: []
        };

    }


    onChange(): void {

        if ( this.master.typeCode != null && this.master.typeCode.length > 0 ) {
            this.iService.getHierarchiesForType( this.master.typeCode, true ).then( hierarchies => {
                this.master.hierarchies = hierarchies;
            } ).catch(( err: any ) => {
                this.error( err.json() );
            } );
        }
        else {
            this.master.hierarchies = [];
        }
    }

    onSubmit(): void {
        this.service.createMasterList( this.master ).then( response => {

            this.onMasterListChange.next( response );
            this.bsModalRef.hide();
        } ).catch(( err: any ) => {
            this.error( err.json() );
        } );
    }

    onCancel(): void {
        this.bsModalRef.hide()
    }

    error( err: any ): void {
        // Handle error
        if ( err !== null ) {
            this.message = ( err.localizedMessage || err.message );
        }
    }

}
