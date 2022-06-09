import { Component, OnDestroy, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { LocalizationService } from "@shared/service";
import { ErrorHandler } from "@shared/component";

import { SynchronizationConfig, OrgSyncInfo } from "@registry/model/registry";
import { SynchronizationConfigService } from "@registry/service";

@Component({
    selector: "synchronization-config-modal",
    templateUrl: "./synchronization-config-modal.component.html",
    styleUrls: []
})
export class SynchronizationConfigModalComponent implements OnInit, OnDestroy {

  message: string = null;

  config: SynchronizationConfig = {
      organization: null,
      system: null,
      hierarchy: null,
      isImport: false,
      label: this.lService.create(),
      configuration: {}
  };

  organizations: OrgSyncInfo[] = [];

  cOrg: OrgSyncInfo = null;
  cSystem: { label: string, oid: string, type: string } = null;

  fieldChange: Subject<string>;

  /*
   * Observable subject for MasterList changes.  Called when an update is successful
   */
  onSuccess: Subject<SynchronizationConfig>;

  // eslint-disable-next-line no-useless-constructor
  constructor(private service: SynchronizationConfigService, private lService: LocalizationService, private bsModalRef: BsModalRef) { }

  ngOnInit(): void {
      this.onSuccess = new Subject();
      this.fieldChange = new Subject();
  }

  ngOnDestroy(): void {
      this.onSuccess.unsubscribe();
      this.fieldChange.unsubscribe();
  }

  init(config: SynchronizationConfig, organizations: OrgSyncInfo[]): void {
      this.organizations = organizations;

      if (config != null) {
          this.config = config;

          let oIndex = this.organizations.findIndex(org => org.code === this.config.organization);

          if (oIndex !== -1) {
              this.cOrg = this.organizations[oIndex];
          }

          let sIndex = this.cOrg.systems.findIndex(system => system.oid === this.config.system);

          if (sIndex !== -1) {
              this.cSystem = this.cOrg.systems[sIndex];
          }
      }
  }

  handleFieldChange(field: string): void {
      this.fieldChange.next(field);
  }

  onOrganizationSelected(): void {
      let index = this.organizations.findIndex(org => org.code === this.config.organization);

      if (index !== -1) {
          this.cOrg = this.organizations[index];
      } else {
          this.cOrg = null;
      }

      this.cSystem = null;

      this.handleFieldChange("organization");
  }

  onChangeExternalSystem(): void {
      let index = this.cOrg.systems.findIndex(system => system.oid === this.config.system);

      if (index !== -1) {
          this.cSystem = this.cOrg.systems[index];
      } else {
          this.cSystem = null;
      }

      this.handleFieldChange("system");
  }

  onSubmit(): void {
    /*
    let levelIndex = 0;
    let len = this.levelRows.length;
    for (let i = 0; i < len; ++i)
    {
      let levelRow: LevelRow = this.levelRows[i];

      if (levelRow.isAttributeEditor)
      {
        continue;
      }
      else if (levelRow.attrCfg == null)
      {
        levelIndex++;
        continue;
      }

      let mappings = this.config.configuration.levels[levelIndex].mappings;
      let mappingsLen = levelRow.attrCfg.mappings.length;
      for (let j = 0; j < mappingsLen; ++j)
      {
        let mapping = JSON.parse(JSON.stringify(levelRow.attrCfg.mappings[j]));
        delete mapping.info;
        mappings.push(mapping);
      }

      levelIndex++;
    }
    */

      this.service.apply(this.config).then(cfg => {
          this.onSuccess.next(cfg);
          this.bsModalRef.hide();
      }).catch((err: HttpErrorResponse) => {
          this.error(err);
      });
  }

  cancel(): void {
      /*
      if (this.config.oid != null) {
          this.service.unlock(this.config.oid).then(() => {
              this.bsModalRef.hide();
          }).catch((err: HttpErrorResponse) => {
              this.error(err);
          });
      } else {
          this.bsModalRef.hide();
      }
      */

      this.bsModalRef.hide();
  }

  error(err: HttpErrorResponse): void {
      this.message = ErrorHandler.getMessageFromError(err);
  }

}
