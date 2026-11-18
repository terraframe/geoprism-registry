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
import { BsModalRef } from 'ngx-bootstrap/modal';

import { LocalizeComponent } from '@shared/component/localize/localize.component';
import { environment } from 'src/environments/environment';

/**
 * Sections of the data which can be included in a backup. These must match the
 * server side BackupSection enum.
 */
export const BACKUP_SECTIONS = [
  'ORGANIZATIONS',
  'METADATA',
  'GEO_OBJECTS',
  'HIERARCHIES',
  'DAGS',
  'UNDIRECTED_GRAPHS',
  'BUSINESS_OBJECTS',
  'BUSINESS_EDGES',
  'EVENTS',
  'LIST_TYPES',
  'USERS',
];

/**
 * The sections grouped for display. Each inner array is one column of the
 * modal.
 */
export const BACKUP_SECTION_GROUPS: { key: string; sections: string[] }[][] = [
  [
    { key: 'CONFIGURATION', sections: ['ORGANIZATIONS', 'METADATA', 'LIST_TYPES'] },
    { key: 'BUSINESS', sections: ['BUSINESS_OBJECTS', 'BUSINESS_EDGES'] },
  ],
  [
    { key: 'GEO_OBJECTS', sections: ['GEO_OBJECTS', 'HIERARCHIES', 'DAGS', 'UNDIRECTED_GRAPHS', 'EVENTS'] },
    { key: 'ACCOUNTS', sections: ['USERS'] },
  ],
];

@Component({
  selector: 'backup-modal',
  templateUrl: './backup-modal.component.html',
  styles: [
    `
      .backup-columns {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        margin: 4px 0 12px;
      }

      .backup-column {
        flex: 1 1 260px;
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .backup-group {
        border: 1px solid #ddd;
        border-radius: 4px;
        padding: 8px 12px;
      }

      .backup-group-header {
        font-weight: 600;
        border-bottom: 1px solid #eee;
        padding-bottom: 6px;
        margin-bottom: 6px;
      }

      .backup-option {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0;
        padding: 3px 0;
        font-weight: normal;
        line-height: 18px;
        cursor: pointer;
      }

      /* Undo the global input styles (40px height, padding) for checkboxes */
      .backup-option input[type='checkbox'] {
        position: static;
        flex-shrink: 0;
        width: auto;
        height: auto;
        margin: 0;
        padding: 0;
      }

      .backup-toolbar {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: 12px;
        flex-wrap: wrap;
      }

      .backup-users-note {
        font-size: 12px;
        color: #8a6d3b;
        margin: 0 0 12px;
      }
    `,
  ],
  standalone: true,
  imports: [FormsModule, LocalizeComponent],
})
export class BackupModalComponent {
  sections = BACKUP_SECTIONS;

  columns = BACKUP_SECTION_GROUPS;

  selected: { [section: string]: boolean } = {};

  constructor(public bsModalRef: BsModalRef) {
    this.setAll(true);
  }

  isGroupSelected(sections: string[]): boolean {
    return sections.every((section) => this.selected[section]);
  }

  isGroupPartial(sections: string[]): boolean {
    return !this.isGroupSelected(sections) && sections.some((section) => this.selected[section]);
  }

  setGroup(sections: string[], value: boolean): void {
    sections.forEach((section) => (this.selected[section] = value));
  }

  setAll(value: boolean): void {
    this.sections.forEach((section) => (this.selected[section] = value));
  }

  getSelected(): string[] {
    return this.sections.filter((section) => this.selected[section]);
  }

  submit(): void {
    const selected = this.getSelected();

    if (selected.length > 0) {
      let url = environment.apiUrl + '/api/admin/backup';

      // Leaving out the sections parameter gives a full backup
      if (selected.length < this.sections.length) {
        url += '?' + selected.map((section) => 'sections=' + encodeURIComponent(section)).join('&');
      }

      window.location.href = url;

      this.bsModalRef.hide();
    }
  }

  cancel(): void {
    this.bsModalRef.hide();
  }
}
