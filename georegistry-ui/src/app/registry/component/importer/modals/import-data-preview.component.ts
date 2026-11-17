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

import { Component, DoCheck, Input, OnChanges } from '@angular/core';
import { NgClass } from '@angular/common';

import { ImportSheet } from '@registry/model/io';
import { LocalizeComponent } from '@shared/component/localize/localize.component';

/**
 * Localization key for an import base type (text, numeric, date or boolean),
 * which is the kind of column an attribute can be matched to
 */
export function getImportBaseTypeKey(baseType: string): string {
  switch (baseType) {
    case 'text':
      return 'data.type.label.text';
    case 'numeric':
      return 'io.attributes.type.numeric';
    case 'date':
      return 'data.type.label.date';
    case 'boolean':
      return 'data.type.label.boolean';
    default:
      return null;
  }
}

/**
 * Value of the column in a sample row. Uses an own-property check so that a
 * column named like a built-in object property (constructor, toString, ...)
 * which is blank in the row gives undefined, not the inherited function.
 */
export function getSampleValue(row: { [column: string]: string }, column: string): string {
  return row != null && column != null && Object.prototype.hasOwnProperty.call(row, column) ? row[column] : undefined;
}

/**
 * Collapsible table showing the first few rows of an import file, used on the
 * import pages where the user matches the file's columns to fields.
 */
@Component({
  selector: 'import-data-preview',
  templateUrl: './import-data-preview.component.html',
  styleUrls: ['./import-data-preview.component.css'],
  standalone: true,
  imports: [LocalizeComponent, NgClass],
})
export class ImportDataPreviewComponent implements OnChanges, DoCheck {
  @Input() sheet: ImportSheet;

  @Input() fileName: string;

  /**
   * Returns the labels of the fields currently matched to the given column.
   * Matched columns are highlighted and list these labels in their header.
   */
  @Input() mappings: (column: string) => string[] = () => [];

  /**
   * Columns of the file in the order they appear, with their detected type
   */
  columns: { name: string; typeKey: string }[] = [];

  /**
   * Result of the mappings function for each column, refreshed on every change
   * detection run so that it is called once per column instead of once per cell
   */
  mapped: { [column: string]: string[] } = Object.create(null);

  showPreview: boolean = true;

  ngOnChanges(): void {
    this.columns = this.buildColumns();
  }

  ngDoCheck(): void {
    // No prototype, so that columns like "__proto__" or "constructor" are
    // ordinary keys
    const mapped: { [column: string]: string[] } = Object.create(null);

    this.columns.forEach((column) => {
      const labels = this.mappings(column.name) || [];

      // Keep the previous array when nothing changed, to avoid re-rendering
      const previous = this.mapped[column.name];
      mapped[column.name] = previous != null && previous.join('\n') === labels.join('\n') ? previous : labels;
    });

    this.mapped = mapped;
  }

  getValue(row: { [column: string]: string }, column: string): string {
    return getSampleValue(row, column);
  }

  get sample(): { [column: string]: string }[] {
    return this.sheet?.sample || [];
  }

  private buildColumns(): { name: string; typeKey: string }[] {
    if (this.sheet == null) {
      return [];
    }

    // The sample rows preserve the column order of the file. Columns which are
    // blank in every sample row are appended from the sheet's column lists.
    const names: string[] = [];
    const add = (name: string) => {
      if (names.indexOf(name) === -1) {
        names.push(name);
      }
    };

    this.sample.forEach((row) => Object.keys(row).forEach(add));

    if (this.sheet.attributes != null) {
      ['text', 'numeric', 'date', 'boolean'].forEach((type) => (this.sheet.attributes[type] || []).forEach(add));
    }

    return names.map((name) => ({ name: name, typeKey: this.getColumnTypeKey(name) }));
  }

  private getColumnTypeKey(column: string): string {
    const attributes = this.sheet.attributes;

    if (attributes == null) {
      return null;
    } else if ((attributes.numeric || []).indexOf(column) !== -1) {
      return getImportBaseTypeKey('numeric');
    } else if ((attributes.date || []).indexOf(column) !== -1) {
      return getImportBaseTypeKey('date');
    } else if ((attributes.boolean || []).indexOf(column) !== -1) {
      return getImportBaseTypeKey('boolean');
    }

    return getImportBaseTypeKey('text');
  }
}
