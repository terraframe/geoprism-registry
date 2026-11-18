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


import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BsModalService } from 'ngx-bootstrap/modal';

import { LocalizationService } from '@shared/service/localization.service';
import { EventService } from '@shared/service/event.service';
import { HierarchyService } from '@registry/service/hierarchy.service';
import { DataSourceService } from '@registry/service/data-source.service';
import { environment } from 'src/environments/environment';

import { DataImporterComponent } from './dataimporter.component';

/** Localization stub which returns the key, so tests can find the text */
const LOCALIZATION_STUB = { decode: (key: string) => key };

describe('DataImporterComponent', () => {
  let fixture: ComponentFixture<DataImporterComponent>;
  let component: DataImporterComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataImporterComponent],
      providers: [
        { provide: LocalizationService, useValue: LOCALIZATION_STUB },
        { provide: EventService, useValue: { start: () => {}, complete: () => {} } },
        { provide: BsModalService, useValue: { show: () => ({ content: {} }) } },
        { provide: DataSourceService, useValue: { getAll: () => Promise.resolve([]) } },
        {
          provide: HierarchyService,
          useValue: { getHierarchyGroupedTypes: () => Promise.resolve({ hierarchies: [], types: [] }) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DataImporterComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  });

  function render(format: string): void {
    fixture.componentRef.setInput('format', format);
    fixture.detectChanges();
  }

  function fileInput(): HTMLInputElement {
    return element.querySelector('input[type="file"]') as HTMLInputElement;
  }

  function localizeKeys(): string[] {
    return Array.from(element.querySelectorAll('localize')).map((e) => e.getAttribute('key'));
  }

  describe('GEOPACKAGE', () => {
    beforeEach(() => render('GEOPACKAGE'));

    it('uploads to the geopackage configuration endpoint', () => {
      expect(component.uploader.options.url).toBe(environment.apiUrl + '/api/geopackage/get-configuration');
    });

    it('accepts gpkg and zip files', () => {
      const inputs = element.querySelectorAll('input[type="file"]');

      expect(inputs.length).toBe(1);
      expect(fileInput().getAttribute('accept')).toBe('.gpkg, .zip');
    });

    it('uses the geopackage file label and the shapefile import strategy label', () => {
      const keys = localizeKeys();

      expect(keys).toContain('io.geopackage');
      expect(keys).toContain('shapefile.import.import.strategy');
      expect(keys).not.toContain('io.shapefile');
      expect(keys).not.toContain('io.spreadsheet');
      expect(keys).not.toContain('spreadsheet.import.import.strategy');
    });
  });

  describe('SHAPEFILE', () => {
    beforeEach(() => render('SHAPEFILE'));

    it('still uploads to the shapefile configuration endpoint', () => {
      expect(component.uploader.options.url).toBe(
        environment.apiUrl + '/api/shapefile/get-shapefile-configuration'
      );
    });

    it('still accepts only zip files', () => {
      expect(fileInput().getAttribute('accept')).toBe('.zip');
      expect(localizeKeys()).toContain('io.shapefile');
      expect(localizeKeys()).not.toContain('io.geopackage');
    });
  });

  describe('EXCEL', () => {
    beforeEach(() => render('EXCEL'));

    it('still uploads to the excel configuration endpoint', () => {
      expect(component.uploader.options.url).toBe(environment.apiUrl + '/api/excel/get-configuration');
    });

    it('accepts xlsx and zip files', () => {
      expect(fileInput().getAttribute('accept')).toBe('.xlsx, .zip');
    });
  });
});
