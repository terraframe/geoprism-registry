import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject, config } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LocalizationService } from '@shared/service';
import { ErrorHandler } from '@shared/component';

import { SynchronizationConfig, OrgSyncInfo, GeoObjectType, MasterListByOrg, MasterListVersion, MasterList } from '@registry/model/registry';
import { SynchronizationConfigService, RegistryService } from '@registry/service';
import { AttributeConfigInfo } from '@registry/model/sync';

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  type: string;
  level: number;
}


export interface LevelRow {
  level?: FhirSyncLevel;
  levelNum?: number;
  list?: MasterList;
}

@Component({
  selector: 'fhir-synchronization-config',
  templateUrl: './fhir-synchronization-config.component.html',
  styleUrls: []
})
export class FhirSynchronizationConfigComponent implements OnInit {
  message: string = null;

  @Input() config: SynchronizationConfig;

  levelRows: LevelRow[] = [];

  org: MasterListByOrg = null;

  constructor(private rService: RegistryService) { }

  ngOnInit(): void {

    this.levelRows = [];

    if (this.config.configuration != null) {
      for (var i = 0; i < this.config.configuration.levels.length; ++i) {
        var level = this.config.configuration.levels[i];

        var levelRow: LevelRow = { level: level, levelNum: i };

        this.levelRows.push(levelRow);
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
    this.rService.getMasterListHistory(row.level.masterListId, 'PUBLISHED').then(list => {
      row.list = list;
    })
  }

  addLevel(): void {
    var lvl = {
      type: null,
      masterListId: null,
      versionId: null,
      level: this.config.configuration.levels.length,
    };

    var len = this.config.configuration['levels'].push(lvl);
    this.levelRows.push({ level: lvl, levelNum: len - 1, list : null });
  }

  removeLevel(levelNum: number, levelRowIndex: number): void {
    if (levelNum < this.config.configuration['levels'].length) {
    }
  }

  error(err: HttpErrorResponse): void {
    this.message = ErrorHandler.getMessageFromError(err);
  }

}
