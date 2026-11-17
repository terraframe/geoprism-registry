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

import { ImportSheet } from '@registry/model/io';
import { LocalizationService } from '@shared/service/localization.service';

import { ImportDataPreviewComponent, getImportBaseTypeKey, getSampleValue } from './import-data-preview.component';

/** Localization stub which returns the key, so tests can find the text */
const LOCALIZATION_STUB = { decode: (key: string) => key };

function sheet(sample: { [column: string]: string }[], attributes: Partial<ImportSheet['attributes']> = {}): ImportSheet {
  return {
    name: 'Sheet1',
    attributes: { text: [], numeric: [], date: [], boolean: [], ...attributes },
    sample: sample,
  } as ImportSheet;
}

describe('getSampleValue', () => {
  it('returns the value of the column', () => {
    expect(getSampleValue({ a: '1' }, 'a')).toBe('1');
  });

  it('returns undefined for a blank column, even when named like a built-in property', () => {
    const row = { code: 'A' };

    for (const column of ['constructor', 'toString', 'valueOf', 'hasOwnProperty', '__proto__', 'isPrototypeOf', 'toLocaleString']) {
      expect(getSampleValue(row, column)).withContext(column).toBeUndefined();
    }
  });

  it('returns values of columns named like built-in properties', () => {
    // Rows parsed from JSON have these as own properties
    const row = JSON.parse('{"constructor":"ctor","toString":"ts","__proto__":"proto"}');

    expect(getSampleValue(row, 'constructor')).toBe('ctor');
    expect(getSampleValue(row, 'toString')).toBe('ts');
    expect(getSampleValue(row, '__proto__')).toBe('proto');
  });

  it('handles missing rows and columns', () => {
    expect(getSampleValue(null, 'a')).toBeUndefined();
    expect(getSampleValue({ a: '1' }, null)).toBeUndefined();
    expect(getSampleValue(Object.create(null), 'a')).toBeUndefined();
  });
});

describe('getImportBaseTypeKey', () => {
  it('maps each base type to a localization key', () => {
    expect(getImportBaseTypeKey('text')).toBe('data.type.label.text');
    expect(getImportBaseTypeKey('numeric')).toBe('io.attributes.type.numeric');
    expect(getImportBaseTypeKey('date')).toBe('data.type.label.date');
    expect(getImportBaseTypeKey('boolean')).toBe('data.type.label.boolean');
    expect(getImportBaseTypeKey('geometry')).toBeNull();
    expect(getImportBaseTypeKey(undefined)).toBeNull();
  });
});

describe('ImportDataPreviewComponent', () => {
  let fixture: ComponentFixture<ImportDataPreviewComponent>;
  let component: ImportDataPreviewComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportDataPreviewComponent],
      providers: [{ provide: LocalizationService, useValue: LOCALIZATION_STUB }],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportDataPreviewComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  });

  function render(value: ImportSheet, mappings?: (column: string) => string[]): void {
    fixture.componentRef.setInput('sheet', value);
    fixture.componentRef.setInput('fileName', 'data.xlsx');

    if (mappings != null) {
      fixture.componentRef.setInput('mappings', mappings);
    }

    fixture.detectChanges();
  }

  function headers(): string[] {
    return Array.from(element.querySelectorAll('th .column-name')).map((e) => e.textContent.trim());
  }

  function cells(row: number): string[] {
    return Array.from(element.querySelectorAll('tbody tr')[row].querySelectorAll('td')).map((e) => e.textContent.trim());
  }

  it('renders nothing without a sheet', () => {
    render(null);

    expect(element.querySelector('.data-preview')).toBeNull();
  });

  it('renders nothing for a sheet without columns', () => {
    render(sheet([]));

    expect(element.querySelector('.data-preview')).toBeNull();
  });

  it('shows a message when the file has columns but no rows', () => {
    render(sheet([], { text: ['code', 'name'] }));

    expect(element.querySelector('.data-preview-empty')).not.toBeNull();
    expect(element.querySelector('table')).toBeNull();
  });

  it('keeps the column order of the file and appends columns blank in every row', () => {
    render(sheet([{ zeta: '1', alpha: '2' }, { zeta: '3', mu: '4' }], { text: ['alpha', 'blank', 'mu', 'zeta'], numeric: ['zeta'] }));

    expect(headers()).toEqual(['zeta', 'alpha', 'mu', 'blank']);
    expect(component.columns.map((c) => c.typeKey)).toEqual([
      'io.attributes.type.numeric',
      'data.type.label.text',
      'data.type.label.text',
      'data.type.label.text',
    ]);
  });

  it('detects date and boolean columns', () => {
    render(sheet([{ d: '2020-01-01', b: 'true' }], { date: ['d'], boolean: ['b'] }));

    expect(component.columns.map((c) => c.typeKey)).toEqual(['data.type.label.date', 'data.type.label.boolean']);
  });

  it('works without attribute lists (old import records)', () => {
    render({ name: 'Sheet1', sample: [{ a: '1' }] } as unknown as ImportSheet);

    expect(headers()).toEqual(['a']);
    expect(component.columns[0].typeKey).toBeNull();
  });

  it('works without a sample (old import records)', () => {
    render({ name: 'Sheet1', attributes: { text: ['a'] } } as unknown as ImportSheet);

    expect(component.sample).toEqual([]);
    expect(element.querySelector('.data-preview-empty')).not.toBeNull();
  });

  it('shows blank cells for columns named like built-in properties', () => {
    const columns = ['code', 'constructor', 'toString', 'valueOf', 'hasOwnProperty', '__proto__'];

    // The second row has only a code; JSON.parse gives __proto__ as an own key
    render(sheet([JSON.parse('{"code":"A","constructor":"c","toString":"t","valueOf":"v","hasOwnProperty":"h","__proto__":"p"}'), { code: 'B' }], { text: columns }));

    expect(headers()).toEqual(columns);
    expect(cells(0)).toEqual(['A', 'c', 't', 'v', 'h', 'p']);
    expect(cells(1)).toEqual(['B', '', '', '', '', '']);
    expect(element.textContent).not.toContain('function');
    expect(element.textContent).not.toContain('[object Object]');
  });

  it('highlights and labels mapped columns, including one named __proto__', () => {
    render(sheet([JSON.parse('{"code":"A","__proto__":"p","other":"o"}')], { text: ['code', '__proto__', 'other'] }), (column) =>
      column === 'code' ? ['Code', 'Display label'] : column === '__proto__' ? ['Proto'] : []
    );

    const th = element.querySelectorAll('th');

    expect(th[0].classList).toContain('mapped');
    expect(Array.from(th[0].querySelectorAll('.column-mapping')).map((e) => e.textContent.trim())).toEqual(['Code', 'Display label']);
    expect(th[1].classList).toContain('mapped');
    expect(th[1].querySelector('.column-mapping').textContent.trim()).toBe('Proto');
    expect(th[2].classList).not.toContain('mapped');
    expect(element.querySelectorAll('td.mapped').length).toBe(2);
  });

  it('updates the highlighting when the mapping changes', () => {
    let target = 'a';

    render(sheet([{ a: '1', b: '2' }], { text: ['a', 'b'] }), (column) => (column === target ? ['Code'] : []));

    expect(element.querySelectorAll('th')[0].classList).toContain('mapped');

    target = 'b';
    fixture.detectChanges();

    expect(element.querySelectorAll('th')[0].classList).not.toContain('mapped');
    expect(element.querySelectorAll('th')[1].classList).toContain('mapped');

    target = null;
    fixture.detectChanges();

    expect(element.querySelectorAll('th.mapped').length).toBe(0);
  });

  it('calls the mappings function once per column, not once per cell', () => {
    const rows = Array.from({ length: 10 }, (_, i) => ({ a: 'a' + i, b: 'b' + i, c: 'c' + i }));
    const mappings = jasmine.createSpy('mappings').and.returnValue([]);

    render(sheet(rows, { text: ['a', 'b', 'c'] }), mappings);

    mappings.calls.reset();
    fixture.detectChanges();

    expect(mappings.calls.count()).toBe(3);
  });

  it('does not re-create the label arrays when nothing changed', () => {
    render(sheet([{ a: '1' }], { text: ['a'] }), () => ['Code']);

    const before = component.mapped['a'];
    fixture.detectChanges();

    expect(component.mapped['a']).toBe(before);
  });

  it('shows HTML and Angular syntax as text', () => {
    render(sheet([{ html: '<b>bold</b><script>window.__hacked = true</script>', angular: '{{ 7*7 }}' }], { text: ['html', 'angular'] }));

    expect(element.querySelector('tbody b')).toBeNull();
    expect(element.querySelector('tbody script')).toBeNull();
    expect((window as any).__hacked).toBeUndefined();
    expect(cells(0)).toEqual(['<b>bold</b><script>window.__hacked = true</script>', '{{ 7*7 }}']);
  });

  it('shows the full value on hover', () => {
    const value = 'x'.repeat(200) + '…';

    render(sheet([{ long: value }], { text: ['long'] }));

    expect(element.querySelector('td').getAttribute('title')).toBe(value);
  });

  it('shows unusual column names', () => {
    const columns = ['Población', '人口', '💧 Water level', 'dots.in.name', '', 'line\nbreak'];
    const row: { [column: string]: string } = {};
    columns.forEach((c, i) => (row[c] = 'v' + i));

    render(sheet([row], { text: columns }));

    expect(component.columns.map((c) => c.name)).toEqual(columns);
    expect(cells(0)).toEqual(['v0', 'v1', 'v2', 'v3', 'v4', 'v5']);
  });

  it('renders a very wide file', () => {
    const columns = Array.from({ length: 1000 }, (_, i) => 'col' + i);
    const rows = Array.from({ length: 10 }, (_, r) => Object.fromEntries(columns.map((c) => [c, r + c])));

    render(sheet(rows, { text: columns }));

    expect(element.querySelectorAll('th').length).toBe(1000);
    expect(element.querySelectorAll('tbody td').length).toBe(10000);
  });

  it('collapses and expands', () => {
    render(sheet([{ a: '1' }], { text: ['a'] }));

    (element.querySelector('.data-preview-toggle') as HTMLElement).click();
    fixture.detectChanges();
    expect(element.querySelector('table')).toBeNull();

    (element.querySelector('.data-preview-toggle') as HTMLElement).click();
    fixture.detectChanges();
    expect(element.querySelector('table')).not.toBeNull();
  });

  it('uses the singular row count for one row', () => {
    render(sheet([{ a: '1' }], { text: ['a'] }));
    expect(element.querySelector('.data-preview-count').textContent).toContain('io.attributes.preview.countOne');

    render(sheet([{ a: '1' }, { a: '2' }], { text: ['a'] }));
    expect(element.querySelector('.data-preview-count').textContent).toContain('io.attributes.preview.count');
  });

  it('rebuilds the columns when the sheet changes', () => {
    render(sheet([{ a: '1' }], { text: ['a'] }));
    render(sheet([{ b: '1' }], { text: ['b'] }));

    expect(headers()).toEqual(['b']);
  });
});
