import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { TypeaheadMatch } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';

import { ImportConfiguration, LocationProblem } from '@registry/model/io';
import { IOService } from '@registry/service';

@Component( {

    selector: 'location-problem',
    templateUrl: './location-problem.component.html',
    styleUrls: []
} )
export class LocationProblemComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Input() problem: LocationProblem;
    @Input() index: number;
    @Output() onError: EventEmitter<any> = new EventEmitter<any>();

    //    show: boolean;
    dataSource: Observable<any>;
    hasSynonym: boolean;

    entityLabel: string;
    entityId: string;

    constructor( private service: IOService ) {
        this.dataSource = Observable.create(( observer: any ) => {
            this.service.getGeoObjectSuggestions( this.entityLabel, this.problem.type, this.problem.parent, this.configuration.hierarchy ).then( results => {
                observer.next( results );
            } );
        } );
    }

    ngOnInit(): void {
        this.entityLabel = null;
        this.entityId = null;
        this.hasSynonym = false;
    }

    typeaheadOnSelect( e: TypeaheadMatch ): void {
        this.entityId = e.item.id;
        this.hasSynonym = ( this.entityId != null );
    }

    createSynonym(): void {
        if ( this.hasSynonym ) {
            this.onError.emit( null );

            this.service.createGeoObjectSynonym( this.entityId, this.problem.label ).then( response => {
                this.problem.resolved = true;
                this.problem.action = {
                    name: 'SYNONYM',
                    synonymId: response.synonymId,
                    vOid: response.vOid, 
                    label: response.label
                };
            } ).catch( e => {
                this.onError.emit( e.error );
            } );
        }
    }

    ignoreDataAtLocation(): void {
        let locationLabel = this.problem.label;
        let universal = this.problem.type;

        this.problem.resolved = true;

        this.problem.action = {
            name: 'IGNOREATLOCATION',
            label: locationLabel,
        };
    }

    undoAction(): void {
        let locationLabel = this.problem.label;
        let universal = this.problem.type;

        if ( this.problem.resolved ) {
            let action = this.problem.action;

            if ( action.name == 'IGNOREATLOCATION' ) {
                this.problem.resolved = false;
                this.problem.action = null;
            }
            else if ( action.name == 'SYNONYM' ) {
                this.onError.emit( null );

                this.service.deleteGeoObjectSynonym( action.synonymId, action.vOid ).then( response => {
                    this.problem.resolved = false;
                    this.problem.action = null;

                    this.entityLabel = null;
                    this.hasSynonym = ( this.entityLabel != null );
                } ).catch( e => {
                    this.onError.emit( e.error );
                } );
            }

        }
    }
}
