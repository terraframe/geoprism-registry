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

import { Component, OnInit, Input, OnDestroy, Output, EventEmitter } from "@angular/core";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";

import { SynchronizationConfig, OrgSyncInfo, GeoObjectType } from "@registry/model/registry";
import { SynchronizationConfigService } from "@registry/service";
import { AttributeConfigInfo, DHIS2AttributeMapping, SyncLevel } from "@registry/model/sync";
import { LocalizationService } from "@shared/service/localization.service";
import { ControlContainer, NgForm } from "@angular/forms";

let DEFAULT_MAPPING_STRATEGY = "net.geoprism.registry.etl.DHIS2AttributeMapping";
let END_DATE_MAPPING = "net.geoprism.registry.etl.DHIS2EndDateAttributeMapping";
let START_DATE_MAPPING = "net.geoprism.registry.etl.DHIS2StartDateAttributeMapping";

export interface GOTAttributeConfig {
  geoObjectTypeCode?: string;
  mappings: DHIS2AttributeMapping[];
  attrConfigInfos: AttributeConfigInfo[];
}
export interface LevelRow {
  isAttributeEditor: boolean;

  level?: SyncLevel;
  levelNum?: number;

  attrCfg?: GOTAttributeConfig;
}

@Component({

    selector: "dhis2-synchronization-config",
    templateUrl: "./dhis2-synchronization-config.component.html",
    styleUrls: ["dhis2-synchronization-config.css"],
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class Dhis2SynchronizationConfigComponent implements OnInit, OnDestroy {

  message: string = null;

  @Input() config: SynchronizationConfig;
  @Input() cOrg: OrgSyncInfo = null;

  @Input() fieldChange: Subject<string>;
  @Output() onError = new EventEmitter<HttpErrorResponse>();

  organizations: OrgSyncInfo[] = [];

  types: GeoObjectType[] = [];

  levelRows: LevelRow[] = [];

  orgUnitGroups: any[] = [];

  // eslint-disable-next-line no-useless-constructor
  constructor(private service: SynchronizationConfigService, public localizationService: LocalizationService) { }

  ngOnInit(): void {
      if (this.config.configuration == null) {
          this.config.configuration = {
              levels: [],
              hierarchyCode: null,
              syncNonExistent: false,
              preferredLocale: "defaultLocale"
          };
      }

      this.levelRows = [];

      if (this.config.configuration.levels != null) {
          for (let i = 0; i < this.config.configuration.levels.length; ++i) {
              let level = this.config.configuration.levels[i];

              let levelRow: LevelRow = { level: level, levelNum: i, isAttributeEditor: false };

              this.levelRows.push(levelRow);
          }
      } else {
          this.config.configuration.levels = [];
      }

      if (this.config.configuration.preferredLocale == null) {
          this.config.configuration.preferredLocale = "defaultLocale";
      }

      if (this.config.configuration.hierarchyCode != null) {
          this.service.getConfigForES(this.config.system, this.config.configuration.hierarchyCode).then(esConfig => {
              this.types = esConfig.types;
              this.orgUnitGroups = esConfig.orgUnitGroups;
          }).catch((err: HttpErrorResponse) => {
              this.error(err);
          });
      }

      this.fieldChange.subscribe(() => {
          this.clearMappingData();
      });
  }

  ngOnDestroy(): void {
      this.fieldChange.unsubscribe();
  }

  onChangeHierarchy(): void {
      this.clearMappingData();
  }

  buildDefaultMappings(): DHIS2AttributeMapping[] {
      return [
          {
              attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
              cgrAttrName: "displayLabel",
              externalId: null,
              dhis2Id: "name",
              dhis2AttrName: "name"
          },
          {
              attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
              cgrAttrName: "displayLabel",
              externalId: null,
              dhis2Id: "shortName",
              dhis2AttrName: "shortName"
          },
          {
              attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
              cgrAttrName: "code",
              dhis2Id: "code",
              externalId: null,
              dhis2AttrName: "code"
          },
          {
              attributeMappingStrategy: START_DATE_MAPPING,
              cgrAttrName: "exists",
              externalId: null,
              dhis2Id: "openingDate",
              dhis2AttrName: "openingDate"
          },
          {
              attributeMappingStrategy: END_DATE_MAPPING,
              cgrAttrName: "exists",
              externalId: null,
              dhis2Id: "closedDate",
              dhis2AttrName: "closedDate"
          }
          /*
          {
              attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
              isOrgUnitGroup: false,
              cgrAttrName: "createDate",
              externalId: null,
              dhis2Id: "created",
              dhis2AttrName: "created"
          },
          {
              attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
              isOrgUnitGroup: false,
              cgrAttrName: "lastUpdateDate",
              externalId: null,
              dhis2Id: "lastUpdated",
              dhis2AttrName: "lastUpdated"
          },
          */
      ];
  }

  clearMappingData(): void {
      this.types = [];
      this.levelRows = [];
      this.config.configuration["levels"] = [];

      if (this.config.configuration.hierarchyCode != null) {
          this.service.getConfigForES(this.config.system, this.config.configuration.hierarchyCode).then(esConfig => {
              this.types = esConfig.types;
              this.orgUnitGroups = esConfig.orgUnitGroups;
          }).catch((err: HttpErrorResponse) => {
              this.error(err);
          });
      }

      let lvl = {
          type: null,
          geoObjectType: null,
          level: 0,
          mappings: [],
          orgUnitGroupIds: []
      };
      this.config.configuration["levels"] = [lvl];
      this.levelRows.push({ level: lvl, levelNum: 0, isAttributeEditor: false });
  }
  
  buildOrgUnitButtonLabel(orgUnitGroupIds: string[]): string {
    if (orgUnitGroupIds == null || orgUnitGroupIds.length == 0) {
      return this.localizationService.decode("sync.dhis2.orgUnit.noneSelected");
    } else if (orgUnitGroupIds.length > 2) {
      return this.localizationService.decode("sync.dhis2.orgUnit.multipleSelected");
    } else {
      return orgUnitGroupIds.flatMap(id => this.orgUnitGroups.find(group => group.id === id).name).join(", ");
    }
  }
  
  clickOrgUnitOption(event: any, level: SyncLevel, group: any): void {
    if (level.orgUnitGroupIds == null) {
      level.orgUnitGroupIds = [];
    }
    
    if (level.orgUnitGroupIds.indexOf(group.id) !== -1) {
      level.orgUnitGroupIds.splice(level.orgUnitGroupIds.indexOf(group.id), 1);
    } else {
      level.orgUnitGroupIds.push(group.id);
    }
    
    event.stopPropagation();
  }

  onSelectLevelType(levelRow: LevelRow): void {
      if (levelRow.level.type === "RELATIONSHIPS") {
          if (levelRow.attrCfg) {
              levelRow.attrCfg.mappings = [];
          }
          levelRow.level.mappings = [];

          let editorIndex = this.getEditorIndex();
          if (editorIndex !== -1) {
              this.levelRows.splice(editorIndex, 1);
          }
      } else {
          if (levelRow.attrCfg && levelRow.attrCfg.mappings.length === 0) {
              levelRow.attrCfg.mappings = this.buildDefaultMappings();
              levelRow.level.mappings = levelRow.attrCfg.mappings;

              let len = levelRow.level.mappings.length;
              for (let i = 0; i < len; ++i) {
                  let mapping = levelRow.level.mappings[i];

                  levelRow.attrCfg.attrConfigInfos.forEach((info) => {
                      if (info.cgrAttr.name === mapping.cgrAttrName) {
                          mapping.info = info;
                      }
                  });
              }
          }
      }
  }

  deleteMapping(levelRow: LevelRow, index: number): void {
      levelRow.attrCfg.mappings.splice(index, 1);
  }

  addNewMapping(levelRow: LevelRow): void {
      levelRow.attrCfg.mappings.push({
          attributeMappingStrategy: DEFAULT_MAPPING_STRATEGY,
          cgrAttrName: null,
          dhis2AttrName: null,
          externalId: null,
          dhis2Id: null,
          terms: []
      });
  }

  addLevel(): void {
      let lvl = {
          type: null,
          geoObjectType: null,
          level: this.config.configuration.levels.length,
          mappings: [],
          orgUnitGroupIds: []
      };
      let len = this.config.configuration["levels"].push(lvl);
      this.levelRows.push({ level: lvl, levelNum: len - 1, isAttributeEditor: false });
  }

  removeLevel(levelNum: number, levelRowIndex: number): void {
      if (levelNum < this.config.configuration["levels"].length) {
          let editorIndex = this.getEditorIndex();
          if (editorIndex === levelRowIndex + 1) {
              this.levelRows.splice(editorIndex, 1);
          }

          this.levelRows.splice(levelRowIndex, 1);

          let newLevelNum = 0;
          for (let i = 0; i < this.levelRows.length; ++i) {
              let levelRow: LevelRow = this.levelRows[i];

              levelRow.levelNum = newLevelNum;

              if (!levelRow.isAttributeEditor) {
                  newLevelNum = newLevelNum + 1;
              }
          }

          this.config.configuration["levels"].splice(levelNum, 1);
      }
  }

  getEditorIndex(): number {
      for (let i = 0; i < this.levelRows.length; ++i) {
          let levelRow = this.levelRows[i];

          if (levelRow.isAttributeEditor) {
              return i;
          }
      }

      return -1;
  }

  configureAttributes(levelRow: any): void {
      let editorIndex = this.getEditorIndex();

      if (editorIndex !== -1) {
          this.levelRows.splice(editorIndex, 1);

          if (editorIndex === levelRow.levelNum + 1) {
              return;
          }
      }

      this.onSelectGeoObjectType(levelRow.level.geoObjectType, levelRow.levelNum, false);
  }

  getTermOptions(info: AttributeConfigInfo, dhis2Id: string) {
      let strategy = info.attributeMappingStrategies[0];

      for (let i = 0; i < strategy.dhis2Attrs.length; ++i) {
          let dhis2Attr = strategy.dhis2Attrs[i];

          if (dhis2Attr.dhis2Id === dhis2Id) {
              return dhis2Attr.options;
          }
      }
  }

  onChangeDHIS2Attr(mapping: DHIS2AttributeMapping) {
      if (mapping.dhis2Id == null || mapping.dhis2Id === "") {
          if (mapping.terms != null) {
              mapping.terms = {};
          }

          mapping.dhis2AttrName = null;

          return;
      }

      mapping.terms = {};

      let strategy = this.getMappingStrategy(mapping);

      let len = strategy.dhis2Attrs.length;
      for (let i = 0; i < len; ++i) {
          if (strategy.dhis2Attrs[i].dhis2Id === mapping.dhis2Id) {
              if (strategy.dhis2Attrs[i].dhis2Id !== strategy.dhis2Attrs[i].name) {
                  mapping.externalId = strategy.dhis2Attrs[i].dhis2Id;
              } else {
                  mapping.externalId = null;
              }
              mapping.dhis2AttrName = strategy.dhis2Attrs[i].name;
          }
      }
  }

  onSelectGeoObjectType(geoObjectTypeCode: string, levelRowIndex: number, isDifferentGot: boolean = true) {
      if (geoObjectTypeCode === "" || geoObjectTypeCode == null) {
          let levelRow: LevelRow = this.levelRows[levelRowIndex];

          levelRow.attrCfg = null;
          levelRow.level.mappings = [];

          let editorIndex = this.getEditorIndex();

          if (editorIndex !== -1 && editorIndex === levelRowIndex + 1) {
              this.levelRows.splice(editorIndex, 1);
          }

          return;
      }

      let attrCfg = this.levelRows[levelRowIndex].attrCfg;
      if (attrCfg != null && attrCfg.geoObjectTypeCode === geoObjectTypeCode) {
          // Resume an editing session on attributes that we fetched previously

          let editorIndex = this.getEditorIndex();

          if (editorIndex !== -1 && editorIndex !== levelRowIndex + 1) {
              this.levelRows.splice(editorIndex, 1);

              if (editorIndex < levelRowIndex) {
                  levelRowIndex = levelRowIndex - 1;
              }
          }

          this.levelRows.splice(levelRowIndex + 1, 0, { isAttributeEditor: true, attrCfg: attrCfg });
      } else {
          this.service.getCustomAttrCfg(geoObjectTypeCode, this.config.system).then((infos: AttributeConfigInfo[]) => {
              let editorIndex = this.getEditorIndex();
              let levelRow: LevelRow = this.levelRows[levelRowIndex];
              let level = levelRow.level;

              if (level.mappings == null) {
                  level.mappings = [];
              }

              if (level.mappings.length === 0 || isDifferentGot) {
                  level.mappings = this.buildDefaultMappings();
              }

              let len = level.mappings.length;
              for (let i = 0; i < len; ++i) {
                  let mapping = level.mappings[i];

                  infos.forEach((info) => {
                      if (info.cgrAttr.name === mapping.cgrAttrName) {
                          mapping.info = info;
                      }
                  });

                  // mapping.dhis2Id is a derived field which only exists on the front-end. This is necessary because of the way DHIS2 separates built-in attributes from custom attributes.
                  // Only custom attributes actually have ids. Standard attributes are referenced via their name.
                  level.mappings.forEach((levelMapping) => {
                      if (levelMapping.dhis2AttrName && levelMapping.dhis2AttrName === mapping.dhis2AttrName) {
                          if (levelMapping.externalId) {
                              mapping.dhis2Id = levelMapping.externalId;
                          } else if (levelMapping.dhis2AttrName) {
                              mapping.dhis2Id = levelMapping.dhis2AttrName;
                          }
                      }
                  });
              }

              levelRow.attrCfg = { geoObjectTypeCode: geoObjectTypeCode, mappings: level.mappings, attrConfigInfos: infos };

              if (editorIndex !== -1 && (editorIndex === levelRowIndex + 1 || infos.length > 0)) {
                  this.levelRows.splice(editorIndex, 1);

                  if (editorIndex < levelRowIndex) {
                      levelRowIndex = levelRowIndex - 1;
                  }
              }

              if (!isDifferentGot) {
                  this.levelRows.splice(levelRowIndex + 1, 0, { isAttributeEditor: true, attrCfg: levelRow.attrCfg, level: levelRow.level, levelNum: levelRow.levelNum });
              }
          }).catch((err: HttpErrorResponse) => {
              this.error(err);
          });
      }
  }

  strategyHasTerms(mapping: DHIS2AttributeMapping) {
      let strategy = this.getMappingStrategy(mapping);

      if (strategy != null) {
          return strategy.terms != null;
      }
  }

  getMappingStrategy(mapping: DHIS2AttributeMapping) {
      if (mapping.info == null) { return null; }
      if (mapping.info.attributeMappingStrategies.length === 1) {
          return mapping.info.attributeMappingStrategies[0];
      }

      for (let i = 0; i < mapping.info.attributeMappingStrategies.length; ++i) {
          let strategy = mapping.info.attributeMappingStrategies[i];

          if (strategy.type === mapping.attributeMappingStrategy) {
              return strategy;
          }
      }

      return null;
  }

  mapCgrAttrEvent(event:Event, levelRow: LevelRow, mapping: DHIS2AttributeMapping) : void {
    const target =  event.target as HTMLSelectElement;
  
    if(target.selectedIndex == 0) {
        this.mapCgrAttr(null, mapping);
    } 
    else {
        const info: AttributeConfigInfo =  levelRow.attrCfg.attrConfigInfos[target.selectedIndex-1];

        this.mapCgrAttr(info, mapping);
    }
  }

  mapCgrAttr(info: AttributeConfigInfo, mapping: DHIS2AttributeMapping) : void {
      if (info == null) {
          mapping.cgrAttrName = null;
          mapping.info = null;
          mapping.dhis2AttrName = null;
          mapping.externalId = null;
          mapping.terms = null;
          mapping.attributeMappingStrategy = null;
          return;
      }

      mapping.cgrAttrName = info.cgrAttr.name;
      mapping.info = info;
      mapping.dhis2AttrName = null;
      mapping.externalId = null;
      mapping.dhis2Id = null;
      mapping.terms = {};

      if (mapping.info.attributeMappingStrategies.length > 0) {
          mapping.attributeMappingStrategy = mapping.info.attributeMappingStrategies[0].type;
      }
  }

  onChangeMappingStrategy(mapping: DHIS2AttributeMapping): void {
      mapping.externalId = null;

      for (const key in mapping.terms) {
          if (Object.prototype.hasOwnProperty.call(mapping.terms, key)) {
              mapping.terms[key] = null;
          }
      }
  }

  error(err: HttpErrorResponse): void {
      this.onError.emit(err);
  }

}
