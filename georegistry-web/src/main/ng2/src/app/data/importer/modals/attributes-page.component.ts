import { Component, Input, Output, EventEmitter } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

import { GeoObjectType } from '../../hierarchy/hierarchy';
import { ShapefileConfiguration } from '../shapefile';

@Component( {
    selector: 'attributes-page',
    templateUrl: './attributes-page.component.html',
    styleUrls: []
} )
export class AttributesPageComponent {

    @Input() configuration: ShapefileConfiguration;
    @Output() configurationChange = new EventEmitter<ShapefileConfiguration>();
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
