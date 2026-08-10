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

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { IdMapping, ImportConfiguration } from '@registry/model/io';

import { NgFor, NgIf } from '@angular/common';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { FormsModule } from '@angular/forms';
import { SourceAuthority } from '@registry/model/source';
import { v4 as uuid } from "uuid";
import { UniqueAuthorityValidatorDirective } from './unique-value-validator.directive';

@Component({
    selector: 'id-mapping-page',
    templateUrl: './id-mapping-page.component.html',
    styleUrls: [],
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, LocalizeComponent, UniqueAuthorityValidatorDirective]
})
export class IdMappingPageComponent implements OnInit {

    @Input() configuration: ImportConfiguration;
    @Input() hasNext: boolean = false;
    @Input() hasBack: boolean = false;
    @Input() authorities: SourceAuthority[] = [];

    @Output() configurationChange = new EventEmitter<ImportConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    constructor() { }

    ngOnInit(): void {
        if (this.configuration.ids != null) {
            this.configuration.ids = []
        }
    }

    onAdd(): void {
        this.configuration.ids.push({
            id: uuid(),
            authority: "",
            function: {
                type: "basic",
                target: ""
            }
        })
    }

    onRemove(mapping: IdMapping): void {
        const index = this.configuration.ids.findIndex(m => m.id === mapping.id);

        if (index !== -1) {
            this.configuration.ids.splice(index, 1);
        }
    }

    onNext(): void {
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
