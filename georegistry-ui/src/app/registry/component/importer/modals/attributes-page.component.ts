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

import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';

import { ImportConfiguration } from '@registry/model/io';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '@shared/component/localize/localize.component';

@Component({
  selector: 'attributes-page',
  templateUrl: './attributes-page.component.html',
  styleUrls: ['./attributes-page.component.css'],
  standalone: true,
  imports: [LocalizeComponent, FormsModule, NgClass],
})
export class AttributesPageComponent implements OnChanges {
  /**
   * Maximum number of example values shown beneath a column selection
   */
  private static readonly MAX_EXAMPLES = 3;

  @Input() configuration: ImportConfiguration;
  @Input() hasNext: boolean = false;
  @Input() hasBack: boolean = false;

  @Output() configurationChange = new EventEmitter<ImportConfiguration>();
  @Output() stateChange = new EventEmitter<string>();

  /**
   * Columns of the file in the order they appear, with their detected type
   */
  columns: { name: string; typeKey: string }[] = [];

  showPreview: boolean = true;

  constructor() {}

  ngOnChanges(): void {
    this.columns = this.buildColumns();
  }

  get sample(): { [column: string]: string }[] {
    return this.configuration?.sheet?.sample || [];
  }

  private buildColumns(): { name: string; typeKey: string }[] {
    const sheet = this.configuration?.sheet;

    if (sheet == null) {
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

    this.sample.forEach(row => Object.keys(row).forEach(add));

    if (sheet.attributes != null) {
      ['text', 'numeric', 'date', 'boolean'].forEach(type => (sheet.attributes[type] || []).forEach(add));
    }

    return names.map(name => ({ name: name, typeKey: this.getColumnTypeKey(name) }));
  }

  private getColumnTypeKey(column: string): string {
    const attributes = this.configuration.sheet.attributes;

    if (attributes == null) {
      return null;
    }
    else if ((attributes.numeric || []).indexOf(column) !== -1) {
      return this.getBaseTypeKey('numeric');
    }
    else if ((attributes.date || []).indexOf(column) !== -1) {
      return this.getBaseTypeKey('date');
    }
    else if ((attributes.boolean || []).indexOf(column) !== -1) {
      return this.getBaseTypeKey('boolean');
    }

    return this.getBaseTypeKey('text');
  }

  /**
   * Localization key for an import base type (text, numeric, date or boolean),
   * which is the kind of column an attribute can be matched to
   */
  getBaseTypeKey(baseType: string): string {
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
   * The first few distinct, non-blank values of the column in the sample
   */
  getExamples(column: string): string[] {
    const examples: string[] = [];

    if (column != null && column.length > 0) {
      for (const row of this.sample) {
        const value = row[column];

        if (value != null && value.trim().length > 0 && examples.indexOf(value) === -1) {
          examples.push(value);

          if (examples.length >= AttributesPageComponent.MAX_EXAMPLES) {
            break;
          }
        }
      }
    }

    return examples;
  }

  /**
   * Labels of the attributes which are currently mapped to the given column
   */
  getMappedAttributes(column: string): string[] {
    const type = this.configuration?.type;

    if (type == null) {
      return [];
    }

    return type.attributes.filter(attribute => attribute.target === column).map(attribute => attribute.label.localizedValue);
  }

  /**
   * True if the given column contains duplicate values within the imported file
   */
  isNonUnique(column: string): boolean {
    const nonUnique = this.configuration.sheet?.nonUniqueAttributes;

    return column != null && column.length > 0 && nonUnique != null && nonUnique.indexOf(column) !== -1;
  }

    onNext(): void {
    this.configurationChange.emit(this.configuration);
    this.stateChange.emit('NEXT');
  }

  onBack(): void {
    this.stateChange.emit('BACK');
  }

  onCancel(): void {
    this.stateChange.emit('CANCEL');
  }
}
