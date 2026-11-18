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
import { BsModalRef } from 'ngx-bootstrap/modal';

import { LocalizationService } from '@shared/service/localization.service';
import { EventService } from '@shared/service/event.service';

import { ReuploadModalComponent } from './reupload-modal.component';

/** Localization stub which returns the key, so tests can find the text */
const LOCALIZATION_STUB = { decode: (key: string) => key };

describe('ReuploadModalComponent', () => {
  let fixture: ComponentFixture<ReuploadModalComponent>;
  let component: ReuploadModalComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReuploadModalComponent],
      providers: [
        { provide: LocalizationService, useValue: LOCALIZATION_STUB },
        { provide: EventService, useValue: { start: () => {}, complete: () => {} } },
        { provide: BsModalRef, useValue: { hide: () => {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReuploadModalComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  });

  function withFormat(formatType: string): void {
    component.job = { configuration: { formatType: formatType } } as any;
    fixture.detectChanges();
  }

  describe('getAcceptableFileFormats', () => {
    it('accepts gpkg and zip files for GEOPACKAGE', () => {
      withFormat('GEOPACKAGE');
      expect(component.getAcceptableFileFormats()).toBe('.gpkg,.zip');
    });

    it('accepts zip files for SHAPEFILE', () => {
      withFormat('SHAPEFILE');
      expect(component.getAcceptableFileFormats()).toBe('.zip');
    });

    it('accepts xlsx and zip files for EXCEL', () => {
      withFormat('EXCEL');
      expect(component.getAcceptableFileFormats()).toBe('.xlsx,.zip');
    });

    it('accepts json and zip files for JSON', () => {
      withFormat('JSON');
      expect(component.getAcceptableFileFormats()).toBe('.json,.zip');
    });
  });

  it('restricts the file picker to the accepted formats', () => {
    for (const [format, accept] of [
      ['GEOPACKAGE', '.gpkg,.zip'],
      ['SHAPEFILE', '.zip'],
      ['EXCEL', '.xlsx,.zip'],
      ['JSON', '.json,.zip'],
    ]) {
      withFormat(format);

      const input = element.querySelector('input[type="file"]') as HTMLInputElement;

      expect(input.getAttribute('accept')).withContext(format).toBe(accept);
    }
  });

  it('labels the file input as a geopackage file for GEOPACKAGE', () => {
    withFormat('GEOPACKAGE');

    const keys = Array.from(element.querySelectorAll('localize')).map((e) => e.getAttribute('key'));

    expect(keys).toContain('io.geopackage');
    expect(keys).not.toContain('io.shapefile');
    expect(keys).not.toContain('io.spreadsheet');
  });
});
