import { Component, OnInit, Input, OnDestroy, EventEmitter, Output } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { SynchronizationConfig } from '@registry/model/registry';
import { SynchronizationConfigService } from '@registry/service';

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  level: number;
}

@Component({
  selector: 'fhir-import-synchronization-config',
  templateUrl: './fhir-import-synchronization-config.component.html',
  styleUrls: []
})
export class FhirImportSynchronizationConfigComponent implements OnInit, OnDestroy {
  message: string = null;

  @Input() config: SynchronizationConfig;
  @Input() fieldChange: Subject<string>;
  @Output() onError = new EventEmitter<HttpErrorResponse>();
  subscription: Subscription = null;

  implementations: { className: string, label: string }[] = [];

  constructor(private service: SynchronizationConfigService) { }

  ngOnInit(): void {
    this.reset();

    this.subscription = this.fieldChange.subscribe((field: string) => {
      if (field === 'organization' || field === 'system') {
        this.reset();
      }
    });

    this.service.getFhirImportImplementations().then(implementations => {
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
        implementation: null
      }
    }
  }

  error(err: HttpErrorResponse): void {
    this.onError.emit(err);
  }

}
