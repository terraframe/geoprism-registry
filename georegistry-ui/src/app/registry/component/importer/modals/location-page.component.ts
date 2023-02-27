///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

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
