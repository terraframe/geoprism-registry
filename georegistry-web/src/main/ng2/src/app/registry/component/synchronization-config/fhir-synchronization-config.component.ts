import { Component, OnInit, Input, OnDestroy, EventEmitter, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { SynchronizationConfig, MasterListByOrg, MasterList } from '@registry/model/registry';
import { RegistryService } from '@registry/service';

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  level: number;
}

@Component({
  selector: 'fhir-synchronization-config',
  templateUrl: './fhir-synchronization-config.component.html',
  styleUrls: []
})
export class FhirSynchronizationConfigComponent implements OnInit, OnDestroy {
  message: string = null;

  @Input() config: SynchronizationConfig;
  @Input() fieldChange: Subject<string>;
  @Output() onError = new EventEmitter<HttpErrorResponse>();

  lists: { [key: string]: MasterList } = {};

  org: MasterListByOrg = null;

  constructor(private rService: RegistryService) { }

  ngOnInit(): void {

    this.reset();

    this.fieldChange.subscribe((field: string) => {
      if (field === 'organization' || field === 'system') {
        this.reset();
      }
    });
  }

  ngOnDestroy(): void {
    this.fieldChange.unsubscribe();
  }

  reset(): void {

    if (this.config.configuration == null) {
      this.config.configuration = {
        levels: [],
        hierarchy: null
      }
    }

    if (this.config.configuration.levels != null) {
      for (var i = 0; i < this.config.configuration.levels.length; ++i) {
        var level = this.config.configuration.levels[i];

        // Get version options
        this.onSelectMasterList(level);
      }
    }
    else {
      this.config.configuration.levels = [];
    }

    // Get 
    this.rService.getMasterListsByOrg().then(response => {
      const filtered = response.orgs.filter(org => org.code = this.config.organization);

      if (filtered.length > 0) {
        this.org = filtered[0];
      }
    });
  }

  onSelectMasterList(level: FhirSyncLevel): void {

    if (level.masterListId != null) {

      this.rService.getMasterListHistory(level.masterListId, 'PUBLISHED').then(list => {
        this.lists[level.masterListId] = list;
      });
    }
    else {
      this.lists[level.masterListId] = null;
    }
  }

  addLevel(): void {
    var level = {
      masterListId: null,
      versionId: null,
      level: this.config.configuration.levels.length,
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
