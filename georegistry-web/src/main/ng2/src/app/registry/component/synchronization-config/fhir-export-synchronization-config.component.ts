import { Component, OnInit, Input, OnDestroy, EventEmitter, Output } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { SynchronizationConfig } from '@registry/model/registry';
import { SynchronizationConfigService } from '@registry/service';
import { ListTypeService } from '@registry/service/list-type.service';
import { ListTypeVersion } from '@registry/model/list-type';

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  level: number;
}

@Component({
  selector: 'fhir-export-synchronization-config',
  templateUrl: './fhir-export-synchronization-config.component.html',
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
  lists: {label:string, oid:string}[] = [];

  constructor(private service: SynchronizationConfigService, private rService: ListTypeService) { }

  ngOnInit(): void {

    this.reset();

    this.subscription = this.fieldChange.subscribe((field: string) => {
      if (field === 'organization' || field === 'system') {
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
    this.rService.getAllLists().then(response => {
      this.lists = response;
    });
  }

  onSelectMasterList(level: FhirSyncLevel): void {

    if (level.masterListId != null && level.masterListId.length > 0) {

      this.rService.getPublicVersions(level.masterListId).then(list => {
        this.versions[level.masterListId] = list;
      });
    }
    else {
      this.versions[level.masterListId] = null;
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
