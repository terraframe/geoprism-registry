import { Input, Component, OnInit, OnDestroy, ViewChild, ElementRef, TemplateRef, ChangeDetectorRef, ViewEncapsulation, HostListener } from '@angular/core';
import { HttpHeaders, HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs';

import { GeoObjectOverTime, GeoObjectType } from '@registry/model/registry';
import { AbstractAction } from '@registry/model/crtable';

import { RegistryService, ChangeRequestService } from '@registry/service';

import { ComponentCanDeactivate } from "@shared/service";

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

import { ActionDetailComponent } from '../action-detail-modal.component';

declare var acp: any;
declare var $: any;

@Component( {

    selector: 'crtable-detail-create-geo-object',
    templateUrl: './detail.component.html',
    styleUrls: ['./crtable-detail-create-geo-object.css'],
    encapsulation: ViewEncapsulation.None
} )
export class CreateUpdateGeoObjectDetailComponent implements ComponentCanDeactivate, ActionDetailComponent {

    @Input() action: any;

    preGeoObject: GeoObjectOverTime = null;

    postGeoObject: GeoObjectOverTime = null;

    geoObjectType: GeoObjectType = null;

    readOnly: boolean = true;

    @ViewChild( "attributeEditor" ) attributeEditor;

    @ViewChild( "geometryEditor" ) geometryEditor;

    bsModalRef: BsModalRef;

    /*
     * Date in which the modal is shown for
     */
    dateStr: string = null;

    /*
     * Date in which the modal is shown for
     */
    forDate: Date = null;


    constructor( private router: Router, private changeRequestService: ChangeRequestService, private modalService: BsModalService, private registryService: RegistryService ) {

        this.forDate = new Date();

        const day = this.forDate.getUTCDate();
        this.dateStr = this.forDate.getUTCFullYear() + "-" + ( this.forDate.getUTCMonth() + 1 ) + "-" + ( day < 10 ? "0" : "" ) + day;
    }

    ngOnInit(): void {

        this.postGeoObject = this.action.geoObjectJson;
        this.geoObjectType = this.action.geoObjectType;

        if ( this.isNew() ) {
            this.preGeoObject = this.postGeoObject;
        }

        this.onSelect( this.action );
    }

    isNew(): boolean {
        return ( this.action.actionType === "net.geoprism.registry.action.geoobject.CreateGeoObjectAction" );
    }

    handleDateChange(): void {
        this.forDate = new Date( Date.parse( this.dateStr ) );
    }

    applyAction() {
        var action = JSON.parse( JSON.stringify( this.action ) );
        action.geoObjectJson = this.attributeEditor.getGeoObject();

        if ( this.geometryEditor != null ) {
            action.geoObjectJson.geometry = this.geometryEditor.saveDraw().geometry;
        }

        this.changeRequestService.applyAction( action ).then( response => {
            this.endEdit();
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    onSelect( action: AbstractAction ) {

        // There are multiple ways we could show a diff of an object.
        //
        // This line will show a diff only when a person is typing so as to show the
        // change they are creating.
        //
        // The method below (getGeoObjectByCode) will compare what is in the database
        // at that time with the change request. This will only track state compared to
        // what is currently in the database which isn't necessarily the original change.
        //
        // A third option which is NOT implemented yet would store the state of a geoobject
        // (original and target) with the change request so as to manage state at time of
        // the change request submission.
        //
        // Display diff when a user is changing a value
        // this.preGeoObject = JSON.parse(JSON.stringify(this.postGeoObject));

        // Display diff of what's in the database
        if (
            this.action.actionType === "net.geoprism.registry.action.geoobject.UpdateGeoObjectAction"
            //    && typeof this.postGeoObject.properties.createDate !== 'undefined'
        ) {
            this.registryService.getGeoObjectOverTime( this.postGeoObject.attributes.code, this.geoObjectType.code ).then( geoObject => {
                this.preGeoObject = geoObject;

            } ).catch(( err: HttpErrorResponse ) => {
                console.log( "Error", err );
                this.error( err );
            } );
        }
    }

    // Big thanks to https://stackoverflow.com/questions/35922071/warn-user-of-unsaved-changes-before-leaving-page
    @HostListener( 'window:beforeunload' )
    canDeactivate(): Observable<boolean> | boolean {
        if ( !this.readOnly ) {
            //event.preventDefault();
            //event.returnValue = 'Are you sure?';
            //return 'Are you sure?';

            return false;
        }

        return true;
    }

    afterDeactivate( isDeactivating: boolean ) {
        if ( isDeactivating && !this.readOnly ) {
            this.unlockActionSync();
        }
    }

    startEdit(): void {
        this.lockAction();
    }

    public endEdit(): void {
        this.unlockAction();
    }

    lockAction() {
        this.changeRequestService.lockAction( this.action.oid ).then( response => {
            this.readOnly = false;
            if ( this.geometryEditor != null ) {
                this.geometryEditor.enableEditing( true );
            }
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    unlockAction() {
        this.changeRequestService.unlockAction( this.action.oid ).then( response => {
            this.readOnly = true;
            if ( this.geometryEditor != null ) {
                this.geometryEditor.enableEditing( false );
            }
        } ).catch(( err: HttpErrorResponse ) => {
            this.error( err );
        } );
    }

    // https://stackoverflow.com/questions/4945932/window-onbeforeunload-ajax-request-in-chrome
    unlockActionSync() {
        $.ajax( {
            url: acp + '/changerequest/unlockAction',
            method: "POST",
            data: { actionId: this.action.oid },
            success: function( a ) {

            },
            async: false
        } );
    }
    // https://www.tivix.com/blog/making-promises-in-a-synchronous-manner
    // unlockActionSync()
    // {
    //   console.log("UnlockActionSync", "Checkpoint 1");
    //
    //   let makeMeLookSync = fn => {
    //     console.log("UnlockActionSync", "Checkpoint 2");
    //
    //     let iterator = fn();
    //     let loop = result => {
    //       console.log("UnlockActionSync", "Checkpoint OuterSnycLoop", result.done, result.value);
    //
    //       !result.done && result.value.then(res => {
    //         console.log("UnlockActionSync", "Checkpoint InnerSnycLoop");
    //         loop(iterator.next(res));
    //       })
    //     };
    //
    //     console.log("UnlockActionSync", "Checkpoint Loop Start", iterator);
    //     loop(iterator.next());
    //   };
    //
    //   var crs = this.changeRequestService;
    //   var actionOid = this.action.oid;
    //
    //   makeMeLookSync(function* () {
    //     console.log("UnlockActionSync Checkpoint", "Start MakeMeLookSync");
    //
    //     yield crs.unlockAction(actionOid).then( response => {
    //       this.readOnly = true;
    //     } ).catch(( err: HttpErrorResponse ) => {
    //       this.error( err );
    //     } );
    //
    //     console.log("UnlockActionSync Checkpoint", "End MakeMeLookSync");
    //   });
    // }

    public error( err: HttpErrorResponse ): void {
            this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
    }

}
