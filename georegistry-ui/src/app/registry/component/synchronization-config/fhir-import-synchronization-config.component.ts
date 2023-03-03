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

interface FhirSyncLevel {
  masterListId: string;
  versionId: string;
  level: number;
}

@Component({
    selector: "fhir-import-synchronization-config",
    templateUrl: "./fhir-import-synchronization-config.component.html",
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
          if (field === "organization" || field === "system") {
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
          };
      }
  }

  error(err: HttpErrorResponse): void {
      this.onError.emit(err);
  }

}
