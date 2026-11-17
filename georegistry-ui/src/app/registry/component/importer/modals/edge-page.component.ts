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

import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

import { EdgeImportConfiguration, ImportConfiguration } from '@registry/model/io';
import { FormsModule } from '@angular/forms';

import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { SourceAuthority } from '@registry/model/source';
import { LocalizationService } from '@shared/service';
import { ImportDataPreviewComponent } from './import-data-preview.component';

@Component({
  selector: 'edge-page',
  templateUrl: './edge-page.component.html',
  styleUrls: [],
  standalone: true,
  imports: [LocalizeComponent, FormsModule, ImportDataPreviewComponent],
})
export class EdgePageComponent implements OnInit {
  @Input() configuration: EdgeImportConfiguration;
  @Input() hasNext: boolean = false;
  @Input() hasBack: boolean = false;
  @Input() authorities: SourceAuthority[] = [];

  @Output() configurationChange = new EventEmitter<ImportConfiguration>();
  @Output() stateChange = new EventEmitter<string>();

  textAttrs: { code: string; label: string }[];

  sourceTypes: { code: string; label: string }[];
  targetTypes: { code: string; label: string }[];

  public initialized: boolean = false;

  /**
   * Localized labels of the fields which can be matched to a column, shown in
   * the data preview
   */
  private labels: { source: string; sourceType: string; target: string; targetType: string };

  constructor(private localizationService: LocalizationService) {}

  ngOnInit(): void {
    this.configuration.edgeSourceStrategy = 'CODE';
    this.configuration.edgeSourceTypeStrategy = 'FIXED_TYPE';
    this.configuration.edgeTargetStrategy = 'CODE';
    this.configuration.edgeTargetTypeStrategy = 'FIXED_TYPE';

    this.sourceTypes = [...this.configuration.sourceTypes];

    this.targetTypes = [...this.configuration.targetTypes];

    this.textAttrs = [...this.configuration.sheet.attributes['text'].map((a) => ({ code: a, label: a }))];

    this.labels = {
      source: this.localizationService.decode('io.edge.source'),
      sourceType: this.localizationService.decode('io.edge.sourceType'),
      target: this.localizationService.decode('io.edge.target'),
      targetType: this.localizationService.decode('io.edge.targetType'),
    };

    this.initialized = true;
  }

  /**
   * Labels of the fields which are currently matched to the given column. The
   * type fields only name a column when their strategy is CODE; with FIXED_TYPE
   * they hold a type code instead.
   */
  getMappedFields = (column: string): string[] => {
    const config = this.configuration;
    const fields: string[] = [];

    if (config.edgeSource === column) {
      fields.push(this.labels.source);
    }

    if (config.edgeSourceTypeStrategy === 'CODE' && config.edgeSourceType === column) {
      fields.push(this.labels.sourceType);
    }

    if (config.edgeTarget === column) {
      fields.push(this.labels.target);
    }

    if (config.edgeTargetTypeStrategy === 'CODE' && config.edgeTargetType === column) {
      fields.push(this.labels.targetType);
    }

    return fields;
  };

  getTypeOptions(field: string, strategy: string) {
    if (strategy === 'FIXED_TYPE') {
      return field === 'SOURCE' ? this.sourceTypes : this.targetTypes;
    } else {
      return this.textAttrs;
    }
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
