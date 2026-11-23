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
import { provideRouter } from '@angular/router';

import { LocalizationService } from '@shared/service/localization.service';
import { GenericTableColumn, GenericTableConfig, TableQuery } from '@shared/model/generic-table';
import { GenericTableComponent } from './generic-table.component';

/** Waits for AG Grid to render its (asynchronously created) header components. */
async function settle(fixture: ComponentFixture<unknown>): Promise<void> {
  for (let i = 0; i < 5; i++) {
    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 50));
    await fixture.whenStable();
  }
}

describe('GenericTableComponent', () => {
  let fixture: ComponentFixture<GenericTableComponent>;
  let component: GenericTableComponent;
  let element: HTMLElement;
  let page: jasmine.Spy;

  function columns(): GenericTableColumn[] {
    return [
      { headerType: 'ATTRIBUTE', header: 'Name', field: 'name', type: 'TEXT', filter: true, rowspan: 1, colspan: 1 },
      {
        headerType: 'ATTRIBUTE',
        header: 'Status',
        field: 'status',
        type: 'AUTOCOMPLETE',
        filter: true,
        text: '',
        rowspan: 1,
        colspan: 1,
        onComplete: () => Promise.resolve(['Active', 'Retired']),
      },
    ];
  }

  async function create(initialState: TableQuery = null, cols: GenericTableColumn[] = columns()): Promise<void> {
    page = jasmine
      .createSpy('page')
      .and.callFake(() => Promise.resolve({ resultSet: [{ name: 'A', status: 'Active' }], count: 1, pageNumber: 1, pageSize: 30 }));

    await TestBed.configureTestingModule({
      imports: [GenericTableComponent],
      providers: [provideRouter([]), { provide: LocalizationService, useValue: { decode: (key: string) => key } }],
    }).compileComponents();

    fixture = TestBed.createComponent(GenericTableComponent);
    component = fixture.componentInstance;
    element = fixture.nativeElement;

    const config: GenericTableConfig = { service: { page: page }, remove: false, label: 'Row' };

    component.setup = { headers: [cols], columns: cols };
    component.config = config;
    component.initialState = initialState;

    await settle(fixture);
  }

  function autocompleteInput(): HTMLInputElement {
    return element.querySelector('generic-table-filter input[role="combobox"]') as HTMLInputElement;
  }

  afterEach(() => {
    document.querySelectorAll('typeahead-container').forEach((e) => e.remove());
  });

  it('shows a restored autocomplete filter value', async () => {
    await create({ first: 0, rows: 30, filters: { status: { value: 'Retired', matchMode: 'equals' } } });

    expect(autocompleteInput()).not.toBeNull();
    expect(autocompleteInput().value).toBe('Retired');
  });

  it('keeps the chosen suggestion in the autocomplete input after the reload', async () => {
    await create();

    const input = autocompleteInput();
    input.focus();
    input.value = 'Ret';
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new KeyboardEvent('keyup', { key: 'R' }));
    await settle(fixture);

    const match = Array.from(document.querySelectorAll('typeahead-container button, typeahead-container a')).find(
      (e) => e.textContent.trim() === 'Retired'
    ) as HTMLElement;

    expect(match).withContext('suggestion list').toBeDefined();

    match.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    match.click();
    for (let i = 0; i < 6; i++) {
      await new Promise((r) => setTimeout(r, 20));
        fixture.detectChanges();
      }
    await settle(fixture);

    expect(page.calls.mostRecent().args[0].filters).toEqual({ status: { value: 'Retired', matchMode: 'equals' } });
    expect(autocompleteInput().value).toBe('Retired');
  });

  it('keeps a typed text filter value after the reload', async () => {
    await create();

    const input = element.querySelector('generic-table-filter input[type="text"]:not([role])') as HTMLInputElement;
    input.value = 'Ab';
    input.dispatchEvent(new Event('input'));
    await new Promise((resolve) => setTimeout(resolve, 400));
    await settle(fixture);

    expect(page.calls.mostRecent().args[0].filters).toEqual({ name: { value: 'Ab', matchMode: 'contains' } });
    expect(input.value).toBe('Ab');
  });

  it('rebuilds the columns when the setup changes', async () => {
    await create();

    const cols = [
      ...columns(),
      { headerType: 'ATTRIBUTE', header: 'Invalid', field: 'invalid', type: 'BOOLEAN', filter: true, rowspan: 1, colspan: 1 },
    ];

    fixture.componentRef.setInput('setup', { headers: [cols], columns: cols });
    await settle(fixture);

    // Read the grid's columns: headers scrolled out of view are not rendered.
    const headers = (component as any).api.getColumns().map((c) => c.getColDef().headerName);

    expect(headers).toEqual(['Name', 'Status', 'Invalid']);
  });

  it('draws a border between filter cells', async () => {
    await create();

    const cells = Array.from(element.querySelectorAll('.ag-floating-filter')) as HTMLElement[];
    const width = (cell: HTMLElement) => getComputedStyle(cell).borderLeftWidth;

    expect(cells.length).toBe(2);
    expect(width(cells.find((c) => c.getAttribute('aria-colindex') === '1'))).toBe('0px');
    expect(width(cells.find((c) => c.getAttribute('aria-colindex') === '2'))).not.toBe('0px');
  });

  it('keeps 2px of padding around the filter inputs', async () => {
    await create();

    element.querySelectorAll('.ag-floating-filter').forEach((cell) => {
      const box = cell.getBoundingClientRect();
      const input = cell.querySelector('generic-table-filter input').getBoundingClientRect();

      expect(input.top - box.top).toBeGreaterThanOrEqual(2);
      expect(box.bottom - input.bottom).toBeGreaterThanOrEqual(2);
    });
  });

  it('keeps 2px of padding around the date filter inputs', async () => {
    await create(null, [
      { headerType: 'ATTRIBUTE', header: 'Date', field: 'date', type: 'DATE', filter: true, rowspan: 1, colspan: 1 },
    ]);

    const cell = element.querySelector('.ag-floating-filter') as HTMLElement;
    const box = cell.getBoundingClientRect();
    const inputs = Array.from(cell.querySelectorAll('input')).map((i) => i.getBoundingClientRect());

    expect(inputs.length).toBe(2);
    expect(inputs[0].top - box.top).toBeGreaterThanOrEqual(2);
    expect(box.bottom - inputs[1].bottom).toBeGreaterThanOrEqual(2);
  });

  it('shows a sort icon at the right of sortable, unsorted headers', async () => {
    await create({ first: 0, rows: 30, multiSortMeta: [{ field: 'code', order: 1 }] }, [
      { headerType: 'ATTRIBUTE', header: 'Name', field: 'name', type: 'TEXT', sortable: true, rowspan: 1, colspan: 1 },
      { headerType: 'ATTRIBUTE', header: 'Code', field: 'code', type: 'TEXT', sortable: true, rowspan: 1, colspan: 1 },
      { headerType: 'ATTRIBUTE', header: 'Notes', field: 'notes', type: 'TEXT', rowspan: 1, colspan: 1 },
    ]);

    const visibleSortIcon = (field: string) =>
      Array.from(element.querySelectorAll(`.ag-header-cell[col-id="${field}"] .fa-sort`)).find(
        (icon) => (icon as HTMLElement).offsetParent != null
      ) as HTMLElement;

    const icon = visibleSortIcon('name');

    expect(icon).withContext('unsorted sortable column').toBeDefined();
    expect(visibleSortIcon('code')).withContext('sorted column').toBeUndefined();
    expect(element.querySelector('.ag-header-cell[col-id="code"] .fa-sort-up')).withContext('ascending icon').not.toBeNull();
    expect(visibleSortIcon('notes')).withContext('unsortable column').toBeUndefined();

    const header = element.querySelector('.ag-header-cell[col-id="name"]').getBoundingClientRect();
    const text = element.querySelector('.ag-header-cell[col-id="name"] .ag-header-cell-text').getBoundingClientRect();

    // Pushed to the right edge, not next to the label.
    expect(icon.getBoundingClientRect().left - text.right).toBeGreaterThan(20);
    expect(header.right - icon.getBoundingClientRect().right).toBeLessThan(30);
  });

  it('uses fa-sort-down when sorted descending', async () => {
    await create({ first: 0, rows: 30, multiSortMeta: [{ field: 'name', order: -1 }] }, [
      { headerType: 'ATTRIBUTE', header: 'Name', field: 'name', type: 'TEXT', sortable: true, rowspan: 1, colspan: 1 },
    ]);

    const icon = Array.from(element.querySelectorAll('.ag-header-cell[col-id="name"] i.fa-solid')).find(
      (i) => (i as HTMLElement).offsetParent != null
    );

    expect(icon?.classList).toContain('fa-sort-down');
  });

  it('colours the header of a sorted column', async () => {
    await create({ first: 0, rows: 30, multiSortMeta: [{ field: 'code', order: -1 }] }, [
      { headerType: 'ATTRIBUTE', header: 'Name', field: 'name', type: 'TEXT', sortable: true, filter: true, rowspan: 1, colspan: 1 },
      { headerType: 'ATTRIBUTE', header: 'Code', field: 'code', type: 'TEXT', sortable: true, rowspan: 1, colspan: 1 },
    ]);

    const style = (field: string) => getComputedStyle(element.querySelector(`.ag-header-cell[col-id="${field}"]`));
    const background = (field: string) => style(field).backgroundColor;

    // .btn-primary colours: theme light green background, theme green text.
    expect(background('code')).toBe('rgb(237, 244, 232)');
    expect(style('code').color).toBe('rgb(107, 165, 66)');
    expect(background('name')).not.toBe('rgb(237, 244, 232)');

    // Filtering no longer colours the header.
    const input = element.querySelector('generic-table-filter input[type="text"]') as HTMLInputElement;
    input.value = 'Ab';
    input.dispatchEvent(new Event('input'));
    await new Promise((resolve) => setTimeout(resolve, 400));
    await settle(fixture);

    expect(background('name')).not.toBe('rgb(237, 244, 232)');
  });
});
