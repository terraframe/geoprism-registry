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

import {
  Component,
  OnInit,
  Input,
  Output,
  EventEmitter,
  OnDestroy,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AgGridAngular } from 'ag-grid-angular';
import {
  AllCommunityModule,
  ColDef,
  ColGroupDef,
  GridApi,
  GridReadyEvent,
  ModuleRegistry,
  SizeColumnsToFitGridStrategy,
  themeQuartz,
} from 'ag-grid-community';
import { PaginationModule } from 'ngx-bootstrap/pagination';
import { Observable, Subject, Subscription } from 'rxjs';

import {
  GenericTableColumn,
  GenericTableConfig,
  TableColumnSetup,
  TableEvent,
  TableFilter,
  TableQuery,
  TableSort,
} from '@shared/model/generic-table';
import { PageResult } from '@shared/model/core';
import { LocalizationService } from '@shared/service/localization.service';
import { GenericTableCellComponent, GenericTableCellParams } from './generic-table-cell.component';
import { GenericTableFilterComponent, GenericTableFilterParams } from './generic-table-filter.component';

ModuleRegistry.registerModules([AllCommunityModule]);

/** Column types that get a filter control under their header. */
const FILTER_TYPES = new Set(['URL', 'TEXT', 'NUMBER', 'DATE', 'AUTOCOMPLETE', 'BOOLEAN']);

const ACTIONS_COL_ID = '__actions';

/** Delay before a typed text or number filter is sent to the server. */
const FILTER_DEBOUNCE_MS = 300;

/*
 * Server-paged table. AG Grid only renders the current page; paging, sorting
 * and filtering are all done by config.service.page(), which receives a
 * TableQuery.
 */
@Component({
  selector: 'generic-table',
  templateUrl: './generic-table.component.html',
  styleUrls: ['./generic-table.css'],
  standalone: true,
  imports: [AgGridAngular, FormsModule, PaginationModule],
})
export class GenericTableComponent implements OnInit, OnChanges, OnDestroy {
  page: PageResult<Object> = {
    resultSet: [],
    count: 0,
    pageNumber: 1,
    pageSize: 30,
  };

  @Input() setup: TableColumnSetup;

  @Input() pageConfig: any = null;

  @Input() config: GenericTableConfig;

  @Input() refresh: Subject<void>;

  @Input() initialState: TableQuery = null;

  @Output() click = new EventEmitter<TableEvent>();
  @Output() onLoadEvent = new EventEmitter<TableQuery>();

  @Input() paginator: boolean = true;

  @Input() scrollable: boolean = false;

  /*
   * Fixed grid height (any CSS length). Rows then scroll inside the grid;
   * without it the grid grows to fit the page of rows.
   */
  @Input() height: string = null;

  readonly theme = themeQuartz.withParams({
    fontFamily: 'inherit',
    fontSize: 13,
    headerFontWeight: 600,
    spacing: 6,
    columnBorder: true,
    wrapperBorderRadius: 4,
    headerBackgroundColor: '#f8f9fa',
    accentColor: '#6BA542',
  });

  readonly autoSizeStrategy: SizeColumnsToFitGridStrategy = { type: 'fitGridWidth', defaultMinWidth: 120 };

  readonly defaultColDef: ColDef = {
    resizable: true,
    sortable: false,
    suppressHeaderMenuButton: true,
    // Show the sortUnSort icon on sortable columns that are not sorted.
    unSortIcon: true,
    // Rows arrive already sorted by the server; stop AG Grid re-sorting the page.
    comparator: () => 0,
  };

  readonly icons = {
    sortUnSort: '<i class="fa-solid fa-sort"></i>',
    sortAscending: '<i class="fa-solid fa-sort-up"></i>',
    sortDescending: '<i class="fa-solid fa-sort-down"></i>',
  };

  columnDefs: (ColDef | ColGroupDef)[] = [];

  pageSizeOptions: number[] = [10, 25, 50, 100, 500];

  floatingFiltersHeight: number = undefined;

  first: number = 0;

  loading: boolean = true;

  booleanOptions: { label: string; value: boolean | null }[] = [];

  hasFilter: boolean = false;

  event: TableQuery = null;

  private sort: TableSort[] = [];

  private filters: { [field: string]: TableFilter } = {};

  private api: GridApi = null;

  private refreshSubscription: Subscription = null;

  private filterTimers = new Map<string, ReturnType<typeof setTimeout>>();

  private requestId = 0;

  private initialized = false;

  private suggestions = new WeakMap<GenericTableColumn, Observable<string[]>>();

  constructor(private localizationService: LocalizationService) {
    this.booleanOptions = [
      { label: '', value: null },
      { value: true, label: this.localizationService.decode('change.request.boolean.option.true') },
      { value: false, label: this.localizationService.decode('change.request.boolean.option.false') },
    ];
  }

  ngOnInit(): void {
    if (this.config.baseZIndex == null) {
      this.config.baseZIndex = 0;
    }

    if (this.config.pageSize != null) {
      this.page.pageSize = this.config.pageSize;
    }

    if (!this.pageSizeOptions.includes(this.page.pageSize)) {
      this.pageSizeOptions = [...this.pageSizeOptions, this.page.pageSize].sort((a, b) => a - b);
    }

    this.sort = this.config.sort != null ? [...this.config.sort] : [];

    if (this.initialState != null) {
      this.first = this.initialState.first != null ? this.initialState.first : 0;

      if (this.initialState.rows != null) {
        this.page.pageSize = this.initialState.rows;
      }

      if (this.initialState.multiSortMeta != null) {
        this.sort = [...this.initialState.multiSortMeta];
        this.config.sort = this.initialState.multiSortMeta;
      }

      if (this.initialState.filters != null) {
        Object.keys(this.initialState.filters).forEach((field) => {
          const filter = this.initialState.filters[field];

          if (filter != null && !Array.isArray(filter)) {
            this.filters[field] = { ...filter };
          }
        });
      }
    }

    this.loadColumns();

    this.columnDefs = this.buildColumnDefs();
    this.initialized = true;

    if (this.refresh != null) {
      this.refreshSubscription = this.refresh.subscribe(() => {
        this.load();
      });
    }

    this.load();
  }

  /*
   * A new column setup (e.g. toggling "show invalid") rebuilds the columns,
   * keeping the current filters and sort.
   */
  ngOnChanges(changes: SimpleChanges): void {
    const setup = changes['setup'];

    if (setup != null && this.initialized && setup.currentValue != null) {
      this.loadColumns();
      this.columnDefs = this.buildColumnDefs();
    }
  }

  ngOnDestroy(): void {
    if (this.refreshSubscription != null) {
      this.refreshSubscription.unsubscribe();
    }

    this.filterTimers.forEach((timer) => clearTimeout(timer));
  }

  /*
   * Copies any initial filter values onto the columns so the filter controls
   * show them, and works out whether the filter row is needed.
   */
  loadColumns(): void {
    this.hasFilter = false;

    this.setup.columns.forEach((column) => {
      if (column.headerType === 'ATTRIBUTE') {
        if (column.filter) {
          this.hasFilter = true;
        }

        const filter = this.filters[column.field];

        if (filter != null) {
          if (column.type === 'DATE') {
            column.startDate = filter.value?.startDate;
            column.endDate = filter.value?.endDate;
          } else if (column.type === 'AUTOCOMPLETE') {
            column.text = filter.value;
          } else if (FILTER_TYPES.has(column.type)) {
            column.value = filter.value;
          }
        }
      }
    });

    if (this.hasFilter && this.setup.columns.some((c) => c.type === 'DATE' && c.filter !== false)) {
      // Room for the two stacked date inputs.
      this.floatingFiltersHeight = 72;
    }
  }

  onGridReady(event: GridReadyEvent): void {
    this.api = event.api;
  }

  onSortChanged(): void {
    if (this.api == null) {
      return;
    }

    const sort: TableSort[] = this.api
      .getColumnState()
      .filter((state) => state.sort != null)
      .sort((a, b) => (a.sortIndex ?? 0) - (b.sortIndex ?? 0))
      .map((state) => ({ field: state.colId, order: state.sort === 'asc' ? 1 : -1 }));

    if (JSON.stringify(sort) !== JSON.stringify(this.sort)) {
      this.sort = sort;
      this.config.sort = sort;
      this.first = 0;
      this.load();
    }
  }

  onPageChanged(event: { page: number; itemsPerPage: number }): void {
    const first = (event.page - 1) * this.page.pageSize;

    if (first !== this.first) {
      this.first = first;
      this.load();
    }
  }

  onPageSizeChange(pageSize: number): void {
    this.page.pageSize = Number(pageSize);
    this.first = 0;
    this.load();
  }

  get currentPage(): number {
    return Math.floor(this.first / this.page.pageSize) + 1;
  }

  get pageReport(): string {
    const count = this.page.count ?? 0;
    const first = count === 0 ? 0 : this.first + 1;
    const last = Math.min(this.first + this.page.pageSize, count);
    const totalPages = Math.max(1, Math.ceil(count / this.page.pageSize));

    return this.localizationService
      .decode('generic.table.pageReport')
      .replace('{first}', String(first))
      .replace('{last}', String(last))
      .replace('{totalRecords}', String(count))
      .replace('{rows}', String(this.page.pageSize))
      .replace('{currentPage}', String(this.currentPage))
      .replace('{totalPages}', String(totalPages));
  }

  /*
   * Sets or clears the filter on a field and reloads from the first page. A
   * blank value removes the filter.
   */
  filter(value: any, field: string, matchMode: string, debounce: boolean = false): void {
    clearTimeout(this.filterTimers.get(field));

    const apply = () => {
      this.filterTimers.delete(field);

      if (this.isBlank(value)) {
        delete this.filters[field];
      } else {
        this.filters[field] = { value: value, matchMode: matchMode };
      }

      this.first = 0;
      this.load();
    };

    if (debounce) {
      this.filterTimers.set(field, setTimeout(apply, FILTER_DEBOUNCE_MS));
    } else {
      apply();
    }
  }

  load(): void {
    const query: TableQuery = {
      first: this.first,
      rows: this.page.pageSize,
      multiSortMeta: [...this.sort],
      filters: { ...this.filters },
    };

    const requestId = ++this.requestId;

    this.loading = true;
    this.event = query;

    this.config.service
      .page(query, this.pageConfig)
      .then((page) => {
        // Ignore responses that a newer request has overtaken.
        if (requestId === this.requestId) {
          this.page = page;

          this.onLoadEvent.emit(query);
        }
      })
      .finally(() => {
        if (requestId === this.requestId) {
          this.loading = false;
        }
      });
  }

  onClick(type: string, row: Object, col: GenericTableColumn): void {
    this.click.emit({
      type: type,
      row: row,
      col: col,
    });
  }

  /*
   * Typeahead source for an AUTOCOMPLETE column. Cached per column so the
   * typeahead keeps a stable Observable.
   */
  getSuggestions(col: GenericTableColumn): Observable<string[]> {
    let source = this.suggestions.get(col);

    if (source == null) {
      source = new Observable<string[]>((observer) => {
        Promise.resolve(col.onComplete != null ? col.onComplete() : null)
          .then((results) => {
            observer.next(results ?? col.results ?? []);
            observer.complete();
          })
          .catch(() => {
            observer.next([]);
            observer.complete();
          });
      });

      this.suggestions.set(col, source);
    }

    return source;
  }

  private buildColumnDefs(): (ColDef | ColGroupDef)[] {
    const roots = this.setup.headers?.[0] ?? this.setup.columns;

    return roots.map((node) => this.toColumnDef(node)).filter((def) => def != null);
  }

  private toColumnDef(node: GenericTableColumn): ColDef | ColGroupDef {
    if (node.headerType === 'GROUP') {
      const children = (node.children ?? []).map((child) => this.toColumnDef(child)).filter((def) => def != null);

      return children.length > 0 ? { headerName: node.header, marryChildren: true, children: children } : null;
    }

    const isActions = node.type === 'ACTIONS';

    const cellParams: GenericTableCellParams = {
      getColumn: () => node,
      getConfig: () => this.config,
      onClick: (type, row, col) => this.onClick(type, row, col),
    };

    const def: ColDef = {
      colId: isActions ? ACTIONS_COL_ID : node.field,
      field: node.field,
      headerName: node.header,
      sortable: node.sortable === true,
      cellRenderer: GenericTableCellComponent,
      cellRendererParams: cellParams,
    };

    if (isActions) {
      def.width = 90;
      def.resizable = false;
      def.suppressSizeToFit = true;
      def.cellClass = 'actions-col';
    }

    const sortIndex = this.sort.findIndex((s) => s.field === node.field);

    if (node.field != null && sortIndex !== -1) {
      def.sort = this.sort[sortIndex].order === 1 ? 'asc' : 'desc';
      def.sortIndex = sortIndex;
    }

    if (this.hasFilter && node.filter !== false && FILTER_TYPES.has(node.type)) {
      const filterParams: GenericTableFilterParams = {
        getColumn: () => node,
        booleanOptions: this.booleanOptions,
        filter: (value, field, matchMode, debounce) => this.filter(value, field, matchMode, debounce),
        getSuggestions: (col) => this.getSuggestions(col),
      };

      // A filter type is required for AG Grid to show a floating filter, but its
      // model is never set; the custom floating filter reports to this.filter().
      def.filter = 'agTextColumnFilter';
      def.floatingFilter = true;
      def.floatingFilterComponent = GenericTableFilterComponent;
      def.floatingFilterComponentParams = filterParams;
      def.suppressFloatingFilterButton = true;
      def.suppressHeaderFilterButton = true;
      // Leave arrow and enter keys to the filter inputs (e.g. the typeahead).
      def.suppressHeaderKeyboardEvent = (params) => {
        const target = params.event.target as HTMLElement;

        return target != null && ['INPUT', 'SELECT'].includes(target.tagName);
      };
    }

    return def;
  }

  private isBlank(value: any): boolean {
    if (value == null || value === '') {
      return true;
    }

    if (Array.isArray(value)) {
      return value.length === 0;
    }

    if (typeof value === 'object' && 'startDate' in value) {
      return value.startDate == null && value.endDate == null;
    }

    return false;
  }
}
