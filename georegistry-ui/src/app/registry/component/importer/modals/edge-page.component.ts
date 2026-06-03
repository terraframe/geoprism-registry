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

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

import { EdgeImportConfiguration, ImportConfiguration } from '@registry/model/io';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor } from '@angular/common';
import { LocalizeComponent } from '../../../../shared/component/localize/localize.component';

@Component({
    selector: 'edge-page',
    templateUrl: './edge-page.component.html',
    styleUrls: [],
    standalone: true,
    imports: [LocalizeComponent, NgIf, FormsModule, NgFor]
})
export class EdgePageComponent implements OnInit {

    @Input() configuration: EdgeImportConfiguration;
    @Output() configurationChange = new EventEmitter<ImportConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    textAttrs: { code: string, label: string }[];

    sourceTypes: { code: string, label: string }[];
    targetTypes: { code: string, label: string }[];

    public initialized: boolean = false;

    constructor() {
    }

    ngOnInit(): void {
        this.configuration.edgeSourceStrategy = 'CODE';
        this.configuration.edgeSourceTypeStrategy = 'FIXED_TYPE';
        this.configuration.edgeTargetStrategy = 'CODE';
        this.configuration.edgeTargetTypeStrategy = 'FIXED_TYPE';


        console.log(this.configuration);

        this.sourceTypes = [
            ...this.configuration.sourceTypes
        ];

        this.targetTypes = [
            ...this.configuration.targetTypes
        ];

        this.textAttrs = [
            ...this.configuration.sheet.attributes['text'].map(a => ({ code: a, label: a }))
        ];

        this.initialized = true;
    }

    getTypeOptions(field: string, strategy: string) {
        if (strategy === 'FIXED_TYPE') {
            return (field === "SOURCE") ? this.sourceTypes : this.targetTypes;
        } else {
            return this.textAttrs;
        }
    }

    onSubmit(): void {
        this.configurationChange.emit(this.configuration);
        this.stateChange.emit('NEXT');
    }

    onCancel(): void {
        this.stateChange.emit('CANCEL');
    }
}
