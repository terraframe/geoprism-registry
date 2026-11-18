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
import { provideNoopAnimations } from '@angular/platform-browser/animations';

import { ImportConfiguration } from '@registry/model/io';
import { LocalizationService } from '@shared/service/localization.service';

import { AttributesPageComponent } from './attributes-page.component';

/** Localization stub which returns the key, so tests can find the text */
const LOCALIZATION_STUB = { decode: (key: string) => key };

function attribute(code: string, target: string, label: string = code, extra: any = {}): any {
  return { code: code, label: { localizedValue: label }, baseType: 'text', required: false, target: target, ...extra };
}

function configuration(sample: { [column: string]: string }[], attributes: any[], columns: string[]): ImportConfiguration {
  return {
    fileName: 'data.xlsx',
    sheet: { name: 'Sheet1', attributes: { text: columns, numeric: [], date: [], boolean: [] }, nonUniqueAttributes: ['dup'], sample: sample },
    type: { code: 'TYPE', attributes: attributes },
  } as any;
}

describe('AttributesPageComponent', () => {
  let fixture: ComponentFixture<AttributesPageComponent>;
  let component: AttributesPageComponent;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttributesPageComponent],
      providers: [{ provide: LocalizationService, useValue: LOCALIZATION_STUB }, provideNoopAnimations()],
    }).compileComponents();

    fixture = TestBed.createComponent(AttributesPageComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  });

  function render(config: ImportConfiguration): void {
    fixture.componentRef.setInput('configuration', config);
    fixture.detectChanges();
  }

  describe('getExamples', () => {
    it('returns the first three distinct non-blank values', () => {
      component.configuration = configuration(
        [{ a: 'x' }, { a: '' }, { a: '  ' }, { a: 'x' }, {}, { a: 'y' }, { a: 'z' }, { a: 'w' }],
        [],
        ['a']
      );

      expect(component.getExamples('a')).toEqual(['x', 'y', 'z']);
    });

    it('returns nothing for no column, an unknown column or a missing sample', () => {
      component.configuration = configuration([{ a: 'x' }], [], ['a']);

      expect(component.getExamples(null)).toEqual([]);
      expect(component.getExamples('')).toEqual([]);
      expect(component.getExamples('missing')).toEqual([]);

      component.configuration = { sheet: { name: 'Sheet1', attributes: {} } } as any;
      expect(component.getExamples('a')).toEqual([]);
    });

    it('does not break on columns named like built-in properties', () => {
      component.configuration = configuration([{ code: 'A' }, JSON.parse('{"code":"B","constructor":"ctor","__proto__":"proto"}')], [], ['code']);

      for (const column of ['toString', 'valueOf', 'hasOwnProperty', 'isPrototypeOf']) {
        expect(component.getExamples(column)).withContext(column).toEqual([]);
      }

      expect(component.getExamples('constructor')).toEqual(['ctor']);
      expect(component.getExamples('__proto__')).toEqual(['proto']);
    });
  });

  describe('getMappedAttributes', () => {
    it('returns the labels of every attribute mapped to the column', () => {
      component.configuration = configuration([], [attribute('code', 'id', 'Code'), attribute('displayLabel', 'id', 'Label'), attribute('other', 'x')], ['id', 'x']);

      expect(component.getMappedAttributes('id')).toEqual(['Code', 'Label']);
      expect(component.getMappedAttributes('nothing')).toEqual([]);
    });

    it('is bound to the component, so it can be passed to the preview', () => {
      component.configuration = configuration([], [attribute('code', 'id', 'Code')], ['id']);

      const unbound = component.getMappedAttributes;

      expect(unbound('id')).toEqual(['Code']);
    });

    it('handles a configuration without a type', () => {
      component.configuration = { sheet: {} } as any;

      expect(component.getMappedAttributes('id')).toEqual([]);
    });
  });

  describe('isNonUnique', () => {
    it('flags columns with duplicate values', () => {
      component.configuration = configuration([], [], ['dup', 'unique']);

      expect(component.isNonUnique('dup')).toBeTrue();
      expect(component.isNonUnique('unique')).toBeFalse();
      expect(component.isNonUnique(null)).toBeFalse();
      expect(component.isNonUnique('')).toBeFalse();
    });
  });

  describe('rendering', () => {
    it('shows examples under a mapped select and the preview below', async () => {
      render(configuration([{ id: 'A1', name: 'Alpha' }, { id: 'A2', name: 'Beta' }], [attribute('code', 'id', 'Code')], ['id', 'name']));
      await fixture.whenStable();
      fixture.detectChanges();

      expect(element.querySelector('.column-examples').textContent).toContain('A1');
      expect(element.querySelector('import-data-preview th.mapped .column-name').textContent.trim()).toBe('id');
    });

    it('renders a file with columns named like built-in properties', async () => {
      const columns = ['code', 'constructor', 'toString', '__proto__'];

      render(configuration([JSON.parse('{"code":"A","__proto__":"p"}'), { code: 'B' }], [attribute('code', 'constructor', 'Code'), attribute('name', '__proto__', 'Name')], columns));
      await fixture.whenStable();
      fixture.detectChanges();

      expect(element.textContent).not.toContain('function');
      expect(element.textContent).not.toContain('native code');
    });

    it('shows the attribute type and description tooltip', () => {
      render(configuration([], [attribute('code', null, 'Code', { description: { localizedValue: 'Human-readable unique identifier' } }), attribute('plain', null, 'Plain')], []));

      const labels = element.querySelectorAll('.label-holder label');

      expect((labels[0] as HTMLElement).style.cursor).toBe('help');
      expect((labels[1] as HTMLElement).style.cursor).toBe('');
      expect(element.querySelector('.attribute-type').textContent).toContain('data.type.label.text');
    });
  });

  describe('getDescription', () => {
    const code = attribute('code', null, 'Code', { description: { localizedValue: 'Human-readable unique identifier' } });

    function withObjectType(objectType: string): any {
      const config: any = configuration([], [code], []);
      config.objectType = objectType;

      return config;
    }

    it('adds the uniqueness scope to the code of each kind of object', () => {
      for (const objectType of ['GEO_OBJECT', 'BUSINESS_OBJECT', 'CONCEPT_OBJECT']) {
        component.configuration = withObjectType(objectType);

        expect(component.getDescription(code)).toBe('Human-readable unique identifier. io.attributes.codeUnique.' + objectType);
      }
    });

    it('only describes the scope of the code attribute', () => {
      component.configuration = withObjectType('CONCEPT_OBJECT');

      const other = attribute('other', null, 'Other', { description: { localizedValue: 'Some attribute' } });

      expect(component.getDescription(other)).toBe('Some attribute');
      expect(component.getDescription(attribute('plain', null, 'Plain'))).toBeNull();
    });

    it('leaves the code unchanged for imports without a known scope', () => {
      component.configuration = withObjectType('EDGE_OBJECT');

      expect(component.getDescription(code)).toBe('Human-readable unique identifier');
    });

    it('does not add a second full stop', () => {
      component.configuration = withObjectType('GEO_OBJECT');

      const described = attribute('code', null, 'Code', { description: { localizedValue: 'Unique identifier.' } });

      expect(component.getDescription(described)).toBe('Unique identifier. io.attributes.codeUnique.GEO_OBJECT');
    });

    it('shows the scope even without a description', () => {
      component.configuration = withObjectType('BUSINESS_OBJECT');

      expect(component.getDescription(attribute('code', null, 'Code'))).toBe('io.attributes.codeUnique.BUSINESS_OBJECT');
    });
  });
});
