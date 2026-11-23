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
import { RouterLink } from '@angular/router';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';
import { GenericTableColumn, GenericTableConfig } from '@shared/model/generic-table';
import { DateTextComponent } from '../date-text/date-text.component';
import { LocalizeComponent } from '../localize/localize.component';

/*
 * Values are passed as functions because AG Grid deep-copies plain objects in
 * column-def params; click events must carry the real column object.
 */
export interface GenericTableCellParams {
  getColumn: () => GenericTableColumn;
  getConfig: () => GenericTableConfig;
  onClick: (type: string, row: Object, col: GenericTableColumn) => void;
}

/*
 * Renders one cell of the generic table, based on the column type.
 */
@Component({
  selector: 'generic-table-cell',
  standalone: true,
  imports: [RouterLink, LocalizeComponent, DateTextComponent],
  template: `
    @switch (type) {
      @case ('URL') {
        <a [routerLink]="['/' + col.baseUrl + '/' + row[col.urlField]]">{{ row[col.field] }}</a>
      }
      @case ('ACTIONS') {
        @if (config.view) {
          <a class="fa fa-eye ico-edit" (click)="onClick('view')" [title]="'View ' + config.label"></a>
        }
        @if (config.edit) {
          <a class="fa fa-pencil-alt ico-edit" (click)="onClick('edit')" [title]="'Edit ' + config.label"></a>
        }
        @if (config.remove) {
          <a class="fa fa-trash-alt ico-remove" (click)="onClick('remove')" [title]="'Remove ' + config.label"></a>
        }
      }
      @case ('CONSTANT') {
        @if (row[col.field] != null) {
          <a (click)="onClick('custom')">{{ col.text }}</a>
        }
      }
      @case ('BOOLEAN') {
        @if (row[col.field] === true) {
          <localize key="change.request.boolean.option.true"></localize>
        }
        @if (row[col.field] === false) {
          <localize key="change.request.boolean.option.false"></localize>
        }
      }
      @case ('DATE') {
        <date-text [date]="row[col.field]"></date-text>
      }
      @case ('NONE') {}
      @default {
        {{ row[col.field] }}
      }
    }
  `,
})
export class GenericTableCellComponent implements ICellRendererAngularComp {
  row: any = {};
  col: GenericTableColumn;
  config: GenericTableConfig;
  type: string;

  private params: ICellRendererParams & GenericTableCellParams;

  agInit(params: ICellRendererParams & GenericTableCellParams): void {
    this.setParams(params);
  }

  refresh(params: ICellRendererParams & GenericTableCellParams): boolean {
    this.setParams(params);

    return true;
  }

  onClick(type: string): void {
    this.params.onClick(type, this.row, this.col);
  }

  private setParams(params: ICellRendererParams & GenericTableCellParams): void {
    this.params = params;
    this.row = params.data ?? {};
    this.col = params.getColumn();
    this.config = params.getConfig();
    this.type = this.col.columnType != null ? this.col.columnType(this.row) : this.col.type;
  }
}
