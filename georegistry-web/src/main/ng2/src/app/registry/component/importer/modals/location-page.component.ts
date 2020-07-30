import { Component, OnInit, Input, Output, EventEmitter, Directive } from '@angular/core';

import { ImportConfiguration } from '@registry/model/io';

import { IOService } from '@registry/service';

@Component( {

    selector: 'location-page',
    templateUrl: './location-page.component.html',
    styleUrls: []
} )
export class LocationPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Output() configurationChange = new EventEmitter<ImportConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    constructor( private service: IOService ) { }

    ngOnInit(): void {
        this.service.getTypeAncestors( this.configuration.type.code, this.configuration.hierarchy ).then( locations => {
            this.configuration.locations = locations;
        } );
    }

    onNext(): void {
        // Map the universals
        this.configurationChange.emit( this.configuration );
        this.stateChange.emit( 'NEXT' );
    }

    onBack(): void {
        this.stateChange.emit( 'BACK' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
