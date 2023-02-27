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

import { Component, OnInit, Input, OnDestroy, EventEmitter, Output } from "@angular/core";
import { Subject, Subscription } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { SynchronizationConfig } from "@registry/model/registry";
import { SynchronizationConfigService } from "@registry/service";
import { ListTypeService } from "@registry/service/list-type.service";
import { ListTypeVersion } from "@registry/model/list-type";

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  level: number;
}

@Component({
    selector: "fhir-export-synchronization-config",
    templateUrl: "./fhir-export-synchronization-config.component.html",
    styleUrls: []
})
export class FhirExportSynchronizationConfigComponent implements OnInit, OnDestroy {

  message: string = null;

  @Input() config: SynchronizationConfig;
  @Input() fieldChange: Subject<string>;
  @Output() onError = new EventEmitter<HttpErrorResponse>();

  subscription: Subscription = null;
  versions: { [key: string]: ListTypeVersion[] } = {};
  implementations: { className: string, label: string }[] = [];
  lists: { label: string, oid: string }[] = [];

  constructor(private service: SynchronizationConfigService, private rService: ListTypeService) { }

  ngOnInit(): void {
      this.reset();

      this.subscription = this.fieldChange.subscribe((field: string) => {
          if (field === "organization" || field === "system") {
              this.reset();
          }
      });

      this.service.getFhirExportImplementations().then(implementations => {
          this.implementations = implementations;
      });
  }

  ngOnDestroy(): void {
      if (this.subscription != null) {
          this.subscription.unsubscribe();
      }
  }

  reset(): void {
      if (this.config.configuration == null) {
          this.config.configuration = {
              levels: [],
              hierarchy: null
          };
      }

      if (this.config.configuration.levels != null) {
          for (let i = 0; i < this.config.configuration.levels.length; ++i) {
              let level = this.config.configuration.levels[i];

        // Get version options
              this.onSelectMasterList(level);
          }
      } else {
          this.config.configuration.levels = [];
      }

    // Get
      this.rService.getAllLists().then(response => {
          this.lists = response;
      });
  }

  onSelectMasterList(level: FhirSyncLevel): void {
      if (level.masterListId != null && level.masterListId.length > 0) {
          this.rService.getPublicVersions(level.masterListId).then(list => {
              this.versions[level.masterListId] = list;
          });
      } else {
          this.versions[level.masterListId] = null;
      }
  }

  addLevel(): void {
      let level = {
          masterListId: null,
          versionId: null,
          level: this.config.configuration.levels.length
      };

      this.config.configuration.levels.push(level);
  }

  removeLevel(i: number): void {
      this.config.configuration.levels.splice(i, 1);

    // Reorder the level
      if (this.config.configuration != null && this.config.configuration.levels != null) {
          for (var i = 0; i < this.config.configuration.levels.length; ++i) {
              this.config.configuration.levels[i].level = i;
          }
      }
  }

  error(err: HttpErrorResponse): void {
      this.onError.emit(err);
  }

}
