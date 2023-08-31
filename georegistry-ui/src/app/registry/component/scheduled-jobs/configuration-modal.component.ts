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

import { Component, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { BsModalService } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from "@angular/common/http";
import { Router } from '@angular/router';

import { LocalizationService } from '@shared/service';
import { ErrorHandler, SuccessModalComponent, ConfirmModalComponent } from '@shared/component';

import { ImportConfiguration } from '@registry/model/io';

import { IOService } from '@registry/service';
import { ImportStrategy } from '@registry/model/constants';
import { RegistryCacheService } from '@registry/service/registry-cache.service';
import { GeoObjectType } from '@registry/model/registry';

@Component({
    selector: 'configuration-modal',
    templateUrl: './configuration-modal.component.html',
    styleUrls: []
})
export class ConfigurationModalComponent implements OnInit {

    configuration: any;

    type: GeoObjectType;

    strategy: any;

    constructor(private service: IOService, public bsModalRef: BsModalRef,
        private localizationService: LocalizationService,
        private cacheService: RegistryCacheService) {
    }

    ngOnInit(): void {
    }

    init(configuration: any): void {
        this.configuration = configuration;

        const strategies = [
            { strategy: ImportStrategy.NEW_AND_UPDATE, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_AND_UPDATE") },
            { strategy: ImportStrategy.NEW_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.NEW_ONLY") },
            { strategy: ImportStrategy.UPDATE_ONLY, label: this.localizationService.decode("etl.import.ImportStrategy.UPDATE_ONLY") }
        ];

        this.strategy = strategies.find(s => s.strategy === this.configuration.importStrategy);

        this.cacheService.getTypeCache().waitOnTypes().then((types: GeoObjectType[]) => {
            const index = types.findIndex(t => t.code === configuration.type.code);

            if(index != null) {
                this.type = types[index];
            }
        });

    }
}
