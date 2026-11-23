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
import { FormsModule } from '@angular/forms';
import { IFloatingFilterAngularComp } from 'ag-grid-angular';
import { IFloatingFilterParams } from 'ag-grid-community';
import { TypeaheadModule } from 'ngx-bootstrap/typeahead';
import { Observable } from 'rxjs';
import { GenericTableColumn } from '@shared/model/generic-table';
import { DateFieldComponent } from '../form-fields/date-field/date-field.component';
import { LocalizePipe } from '../../pipe/localize.pipe';

/*
 * Values are passed as functions because AG Grid deep-copies plain objects in
 * column-def params; the filter must update the real column object.
 */
export interface GenericTableFilterParams {
  getColumn: () => GenericTableColumn;
  booleanOptions: { label: string; value: boolean | null }[];
  filter: (value: any, field: string, matchMode: string, debounce?: boolean) => void;
  getSuggestions: (col: GenericTableColumn) => Observable<string[]>;
}

/*
 * The filter control shown under a column header. It does not use AG Grid's own
 * filter model; it reports changes back to the generic table, which re-queries
 * the server.
 */
@Component({
  selector: 'generic-table-filter',
  standalone: true,
  imports: [FormsModule, TypeaheadModule, DateFieldComponent, LocalizePipe],
  styles: [
    `
      /*
       * Fill the filter cell (which clips overflow) so the padding stays
       * inside it; the inputs shrink to fit instead of pushing it out.
       */
      :host {
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 2px;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        padding: 2px;
      }

      .form-control,
      .form-select {
        flex: 1 1 auto;
        min-height: 0;
      }

      /*
       * Two stacked date inputs must fit the taller date filter row; the
       * global input rules (form.scss) set a 40px height and min-height.
       */
      :host ::ng-deep date-field input {
        height: 32px;
        min-height: 0;
      }
    `,
  ],
  template: `
    @switch (col.type) {
      @case ('NUMBER') {
        <input
          type="number"
          step="any"
          class="form-control form-control-sm"
          [ngModel]="col.value"
          (ngModelChange)="onValueChange($event, 'equals', true)"
        />
      }
      @case ('DATE') {
        <date-field
          [(value)]="col.startDate"
          [classNames]="'search-input'"
          (change)="filterDates()"
          [allowInfinity]="false"
          customStyles="font-size: 10px;"
          [placeholder]="'generic.table.to.date' | localize"
        >
        </date-field>
        <date-field
          [(value)]="col.endDate"
          [classNames]="'search-input'"
          (change)="filterDates()"
          [allowInfinity]="false"
          customStyles="font-size: 10px;"
          [placeholder]="'generic.table.from.date' | localize"
        >
        </date-field>
      }
      @case ('AUTOCOMPLETE') {
        <input
          type="text"
          class="form-control form-control-sm"
          autocomplete="off"
          [ngModel]="col.text"
          [typeahead]="suggestions"
          [typeaheadAsync]="true"
          [typeaheadWaitMs]="200"
          container="body"
          (typeaheadOnSelect)="params.filter($event.value, col.field, 'equals')"
          (ngModelChange)="onTextChange($event)"
        />
      }
      @case ('BOOLEAN') {
        <select
          class="form-select form-select-sm"
          [ngModel]="col.value"
          (ngModelChange)="onValueChange($event, 'equals')"
        >
          @for (option of params.booleanOptions; track option.value) {
            <option [ngValue]="option.value">{{ option.label }}</option>
          }
        </select>
      }
      @default {
        <input
          type="text"
          class="form-control form-control-sm"
          [ngModel]="col.value"
          (ngModelChange)="onValueChange($event, 'contains', true)"
        />
      }
    }
  `,
})
export class GenericTableFilterComponent implements IFloatingFilterAngularComp {
  params: IFloatingFilterParams & GenericTableFilterParams;
  col: GenericTableColumn;
  suggestions: Observable<string[]>;

  agInit(params: IFloatingFilterParams & GenericTableFilterParams): void {
    this.params = params;
    this.col = params.getColumn();

    if (this.col.type === 'AUTOCOMPLETE') {
      this.suggestions = params.getSuggestions(this.col);
    }
  }

  onParentModelChanged(): void {
    // The parent AG Grid filter is never used, so there is nothing to sync.
  }

  /*
   * The value is stored on the column here, not through [(ngModel)]: a
   * two-way binding plus a separate (ngModelChange) handler on the same
   * element left the column value unset.
   */
  onValueChange(value: any, matchMode: string, debounce: boolean = false): void {
    this.col.value = value;
    this.params.filter(value, this.col.field, matchMode, debounce);
  }

  /*
   * Typing only updates the text; the filter is applied when a suggestion is
   * chosen, or cleared when the input is emptied.
   */
  onTextChange(text: string): void {
    this.col.text = text;

    if (text === '') {
      this.params.filter('', this.col.field, 'equals');
    }
  }

  filterDates(): void {
    this.params.filter({ startDate: this.col.startDate, endDate: this.col.endDate }, this.col.field, 'between');
  }
}
