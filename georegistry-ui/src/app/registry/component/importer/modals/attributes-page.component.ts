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

import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ImportConfiguration } from '@registry/model/io';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { LocalizationService } from '@shared/service/localization.service';
import { TooltipModule } from 'primeng/tooltip';
import { ImportDataPreviewComponent, getImportBaseTypeKey, getSampleValue } from './import-data-preview.component';

@Component({
  selector: 'attributes-page',
  templateUrl: './attributes-page.component.html',
  styleUrls: ['./attributes-page.component.css'],
  standalone: true,
  imports: [LocalizeComponent, FormsModule, NgClass, TooltipModule, ImportDataPreviewComponent],
})
export class AttributesPageComponent {
  /**
   * Maximum number of example values shown beneath a column selection
   */
  private static readonly MAX_EXAMPLES = 3;

  @Input() configuration: ImportConfiguration;
  @Input() hasNext: boolean = false;
  @Input() hasBack: boolean = false;

  @Output() configurationChange = new EventEmitter<ImportConfiguration>();
  @Output() stateChange = new EventEmitter<string>();

  constructor(private localizationService: LocalizationService) {}

  get sample(): { [column: string]: string }[] {
    return this.configuration?.sheet?.sample || [];
  }

  /**
   * The tooltip text of an attribute: its description, and for the code the
   * scope in which it must be unique. Codes of geo objects and business objects
   * are unique within their type, but codes of concepts are unique across all
   * concept classes.
   */
  getDescription(attribute: { code: string; description?: { localizedValue: string } }): string | null {
    const description = attribute.description?.localizedValue;
    const scope = attribute.code === 'code' ? this.getCodeUniquenessScope() : null;

    if (description && scope) {
      return description + (/[.!?]$/.test(description.trim()) ? ' ' : '. ') + scope;
    }

    return description || scope || null;
  }

  private getCodeUniquenessScope(): string | null {
    const objectType = this.configuration?.objectType;

    if (objectType === 'GEO_OBJECT' || objectType === 'BUSINESS_OBJECT' || objectType === 'CONCEPT_OBJECT') {
      return this.localizationService.decode('io.attributes.codeUnique.' + objectType);
    }

    return null;
  }

  /**
   * Localization key for an import base type (text, numeric, date or boolean),
   * which is the kind of column an attribute can be matched to
   */
  getBaseTypeKey(baseType: string): string {
    return getImportBaseTypeKey(baseType);
  }

  /**
   * The first few distinct, non-blank values of the column in the sample
   */
  getExamples(column: string): string[] {
    const examples: string[] = [];

    if (column != null && column.length > 0) {
      for (const row of this.sample) {
        const value = getSampleValue(row, column);

        if (typeof value === 'string' && value.trim().length > 0 && examples.indexOf(value) === -1) {
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
  getMappedAttributes = (column: string): string[] => {
    const type = this.configuration?.type;

    if (type == null) {
      return [];
    }

    return type.attributes.filter((attribute) => attribute.target === column).map((attribute) => attribute.label.localizedValue);
  };

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
