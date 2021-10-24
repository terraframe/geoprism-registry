import { Component, OnInit, Input, Output, EventEmitter, Directive } from '@angular/core';

import { ImportConfiguration } from '@registry/model/io';

import { IOService } from '@registry/service';

@Component({

    selector: 'location-page',
    templateUrl: './location-page.component.html',
    styleUrls: []
})
export class LocationPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Input() property: string = 'type';
    @Input() includeChild: boolean = false;

    @Output() configurationChange = new EventEmitter<ImportConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    constructor(private service: IOService) { }

    ngOnInit(): void {
        this.service.getTypeAncestors(this.configuration[this.property].code, this.configuration.hierarchy, true, this.includeChild).then(locations => {
            this.configuration.locations = locations;
        });
    }

    onNext(): void {
        // Map the universals
        this.configurationChange.emit(this.configuration);
        this.stateChange.emit('NEXT');
    }

    onBack(): void {
        this.stateChange.emit('BACK');
    }

    onCancel(): void {
        this.stateChange.emit('CANCEL');
    }
}
