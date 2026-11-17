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

import { EdgeImportConfiguration } from '@registry/model/io';
import { LocalizationService } from '@shared/service/localization.service';

import { EdgePageComponent } from './edge-page.component';

/** Localization stub which returns the key, so tests can find the text */
const LOCALIZATION_STUB = { decode: (key: string) => key };

function configuration(): EdgeImportConfiguration {
  return {
    fileName: 'edges.json',
    sheet: {
      name: 'json',
      attributes: { text: ['source', 'sourceType', 'target', 'targetType', 'note'], numeric: [], date: [], boolean: [] },
      sample: [
        { source: 'DAM-1', sourceType: 'Dam', target: 'DAM-2', targetType: 'Dam', note: 'first' },
        { source: 'DAM-2', sourceType: 'Dam', target: 'DAM-3', targetType: 'Dam' },
      ],
    },
    sourceTypes: [{ code: 'Dam', label: 'Dam' }],
    targetTypes: [{ code: 'Dam', label: 'Dam' }],
  } as any;
}

describe('EdgePageComponent', () => {
  let fixture: ComponentFixture<EdgePageComponent>;
  let component: EdgePageComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EdgePageComponent],
      providers: [{ provide: LocalizationService, useValue: LOCALIZATION_STUB }],
    }).compileComponents();

    fixture = TestBed.createComponent(EdgePageComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;

    fixture.componentRef.setInput('configuration', configuration());
    fixture.detectChanges();
  });

  it('labels the source and target columns', () => {
    component.configuration.edgeSource = 'source';
    component.configuration.edgeTarget = 'target';

    expect(component.getMappedFields('source')).toEqual(['io.edge.source']);
    expect(component.getMappedFields('target')).toEqual(['io.edge.target']);
    expect(component.getMappedFields('note')).toEqual([]);
  });

  it('labels type columns only when the type is read from a column', () => {
    const config = component.configuration;
    config.edgeSourceType = 'sourceType';
    config.edgeTargetType = 'targetType';

    // Fixed type is the default: the values are type codes, not columns
    expect(config.edgeSourceTypeStrategy).toBe('FIXED_TYPE');
    expect(component.getMappedFields('sourceType')).toEqual([]);

    config.edgeSourceTypeStrategy = 'CODE';
    config.edgeTargetTypeStrategy = 'CODE';

    expect(component.getMappedFields('sourceType')).toEqual(['io.edge.sourceType']);
    expect(component.getMappedFields('targetType')).toEqual(['io.edge.targetType']);
  });

  it('does not label a column which shares its name with a fixed type code', () => {
    const config = component.configuration;
    config.edgeSourceTypeStrategy = 'FIXED_TYPE';
    config.edgeSourceType = 'Dam';

    expect(component.getMappedFields('Dam')).toEqual([]);
  });

  it('lists every field mapped to one column', () => {
    const config = component.configuration;
    config.edgeSource = 'source';
    config.edgeTarget = 'source';
    config.edgeSourceTypeStrategy = 'CODE';
    config.edgeSourceType = 'source';

    expect(component.getMappedFields('source')).toEqual(['io.edge.source', 'io.edge.sourceType', 'io.edge.target']);
  });

  it('is bound to the component, so it can be passed to the preview', () => {
    component.configuration.edgeSource = 'source';

    const unbound = component.getMappedFields;

    expect(unbound('source')).toEqual(['io.edge.source']);
  });

  it('shows the preview and highlights the chosen columns', async () => {
    component.configuration.edgeSource = 'source';
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const preview = element.querySelector('import-data-preview');

    expect(preview).not.toBeNull();
    expect(Array.from(preview.querySelectorAll('th .column-name')).map((e) => e.textContent.trim())).toEqual(['source', 'sourceType', 'target', 'targetType', 'note']);
    expect(preview.querySelector('th.mapped .column-name').textContent.trim()).toBe('source');
    expect(preview.querySelectorAll('tbody tr').length).toBe(2);
  });

  it('shows the preview for a file without a sample', () => {
    const config = configuration();
    delete config.sheet.sample;

    fixture = TestBed.createComponent(EdgePageComponent);
    fixture.componentRef.setInput('configuration', config);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.data-preview-empty')).not.toBeNull();
  });
});
