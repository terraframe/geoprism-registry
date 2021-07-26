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

export interface LevelRow {
  level?: FhirSyncLevel;
  list?: MasterList;
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

  levelRows: LevelRow[] = [];

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

    this.levelRows = [];

    if (this.config.configuration != null && this.config.configuration.levels != null) {
      for (var i = 0; i < this.config.configuration.levels.length; ++i) {
        var level = this.config.configuration.levels[i];

        var levelRow: LevelRow = { level: level };

        this.levelRows.push(levelRow);
        
        // Get version options
        this.onSelectMasterList(levelRow);
      }
    }
    else {
      this.config.configuration = {
        levels: [],
      };
    }

    // Get 
    this.rService.getMasterListsByOrg().then(response => {
      const filtered = response.orgs.filter(org => org.code = this.config.organization);

      if (filtered.length > 0) {
        this.org = filtered[0];
      }
    });
  }

  onSelectMasterList(row: LevelRow): void {

    if (row.level.masterListId != null) {

      this.rService.getMasterListHistory(row.level.masterListId, 'PUBLISHED').then(list => {
        row.list = list;
      });
    }
  }

  addLevel(): void {
    var lvl = {
      masterListId: null,
      versionId: null,
      level: this.config.configuration.levels.length,
    };

    this.levelRows.push({ level: lvl, list: null });
  }

  removeLevel(i: number): void {
    this.levelRows.splice(i, 1);
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
