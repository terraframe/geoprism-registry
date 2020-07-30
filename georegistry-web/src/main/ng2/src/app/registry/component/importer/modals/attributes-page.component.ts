import { Component, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { ImportConfiguration } from '@registry/model/io';

@Component( {
    selector: 'attributes-page',
    templateUrl: './attributes-page.component.html',
    styleUrls: []
} )
export class AttributesPageComponent {

    @Input() configuration: ImportConfiguration;
    @Output() configurationChange = new EventEmitter<ImportConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    constructor() {
    }

    onNext(): void {
        this.configurationChange.emit( this.configuration );
        this.stateChange.emit( 'NEXT' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }
}
