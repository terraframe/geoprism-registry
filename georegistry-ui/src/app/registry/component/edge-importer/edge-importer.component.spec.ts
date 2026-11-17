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

import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { FileUploadModule } from 'ng2-file-upload';
import { BsModalService } from 'ngx-bootstrap/modal';

import { LocalizationService, EventService } from '@shared/service';
import { RegistryService } from '@registry/service';
import { GraphTypeService } from '@registry/service/graph-type.service';
import { DataSourceService } from '@registry/service/data-source.service';
import { BusinessTypeService } from '@registry/service/business-type.service';
import { ConceptClassService } from '@registry/service/concept-class.service';

import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { DateFieldComponent } from '@shared/component/form-fields/date-field/date-field.component';

import { EdgeImporterComponent } from './edge-importer.component';

/**
 * Replaces the page container, whose header and side nav need the whole
 * application's configuration
 */
@Component({ selector: 'page-container', template: '<ng-content></ng-content>', standalone: true })
class PageContainerStubComponent {}

/** Localization stub which returns the key, so no localization data is needed */
const LOCALIZATION_STUB = { decode: (key: string) => key, localize: (bundle: string, key: string) => key };

/** Service stub whose lookups return no types */
const EMPTY_LIST_STUB = { getAll: () => Promise.resolve([]) };

describe('EdgeImporterComponent', () => {
  let component: EdgeImporterComponent;
  let fixture: ComponentFixture<EdgeImporterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EdgeImporterComponent],
      providers: [
        { provide: LocalizationService, useValue: LOCALIZATION_STUB },
        { provide: EventService, useValue: { start: () => {}, complete: () => {}, registerSessionListener: () => {} } },
        { provide: BsModalService, useValue: { show: () => ({ content: {} }) } },
        { provide: RegistryService, useValue: { getGeoObjectTypes: () => Promise.resolve([]) } },
        { provide: GraphTypeService, useValue: EMPTY_LIST_STUB },
        { provide: DataSourceService, useValue: EMPTY_LIST_STUB },
        { provide: BusinessTypeService, useValue: EMPTY_LIST_STUB },
        { provide: ConceptClassService, useValue: EMPTY_LIST_STUB },
      ],
    })
      .overrideComponent(EdgeImporterComponent, {
        set: { imports: [PageContainerStubComponent, LocalizeComponent, FormsModule, DateFieldComponent, FileUploadModule] },
      })
      .compileComponents();

    fixture = TestBed.createComponent(EdgeImporterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
