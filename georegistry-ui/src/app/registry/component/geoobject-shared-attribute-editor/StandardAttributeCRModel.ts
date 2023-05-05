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


import { ChangeRequest, AbstractAction, UpdateAttributeAction, SummaryKey } from "@registry/model/crtable";
import { AttributeType, GeoObjectOverTime } from "@registry/model/registry";
import { ActionTypes } from "@registry/model/constants";
import { AlternateId, ExternalId } from "@core/model/core";
import { LocalizationService } from "@shared/service";

export class StandardDiffView {
    summaryKey: SummaryKey;
    summaryKeyLocalized: string;
    oldValue?: any;
    value: any;
    attributeCode: string;
    editor: StandardAttributeCRModel;
    lService: LocalizationService;
    
    constructor(editor: StandardAttributeCRModel, lService: LocalizationService, summaryKey: SummaryKey = null) {
        this.editor = editor;
        this.lService = lService;
      
        let diff = this.editor.diff;
      
        if (diff != null) {
            let newVal = diff.newValue == null ? null : JSON.parse(JSON.stringify(diff.newValue));
            this.value = newVal;
            
            if (!summaryKey) {
                summaryKey = SummaryKey.VALUE_CHANGE;
            }

            if (summaryKey !== SummaryKey.DELETE && diff.oldValue !== null && diff.oldValue !== undefined) {
                this.oldValue = JSON.parse(JSON.stringify(diff.oldValue));
            } else if (summaryKey === SummaryKey.DELETE && !this.value && diff.oldValue) {
                this.value = JSON.parse(JSON.stringify(diff.oldValue));
            }
        } else {
            this.value = this.editor.value;
            
            if (!summaryKey) {
                summaryKey = SummaryKey.UNMODIFIED;
            }
        }
        
        this.setSummaryKey(summaryKey);
    }
    
    setSummaryKey(summaryKey: SummaryKey): void {
        this.summaryKey = summaryKey;
        this.summaryKeyLocalized = this.lService.decode("changeovertime.manageVersions.summaryKey." + summaryKey);
    }
}

export class ListElementView {
    lService: LocalizationService;
    editor: StandardAttributeCRModel;
    summaryKey: SummaryKey;
    summaryKeyLocalized: string;
    value: string;
    oldValue?: string;
    oldExternalSystemId: string;
    externalSystemId: string;
    externalId: ExternalId;
    view: ListDiffView;
    
    constructor(lService: LocalizationService, editor: StandardAttributeCRModel, summaryKey: SummaryKey, view: ListDiffView, id: ExternalId, oldId: string, oldExternalSystemId: string) {
        this.lService = lService;
        this.editor = editor;
        this.setSummaryKey(summaryKey);
        this.view = view;
        this.summaryKey = summaryKey;
        
        if (this.summaryKey !== SummaryKey.NEW && this.summaryKey !== SummaryKey.DELETE) {
            if (oldId && id.id !== oldId) {
                this.oldValue = oldId;
                this.setSummaryKey(SummaryKey.UPDATE);
            }
            if (oldExternalSystemId && id.externalSystemId !== oldExternalSystemId) {
                this.oldExternalSystemId = oldExternalSystemId;
                this.setSummaryKey(SummaryKey.UPDATE);
            }
            if (this.summaryKey == null) {
                this.setSummaryKey(SummaryKey.UNMODIFIED);
            }
        }
        
        if (id) {
            this.externalId = id;
            this.value = id.id;
            this.externalSystemId = id.externalSystemId;
        } else if (summaryKey === SummaryKey.DELETE) {
            this.value = oldId
            this.externalSystemId = oldExternalSystemId;
        }
    }
    
    setSummaryKey(summaryKey: SummaryKey): void {
        this.summaryKey = summaryKey;
        this.summaryKeyLocalized = this.lService.decode("changeovertime.manageVersions.summaryKey." + summaryKey);
    }
    
    updateModel() {
        if (this.summaryKey !== SummaryKey.DELETE) {
            let modelIdx = this.view.value.findIndex((id: ExternalId) => id.externalSystemId === this.externalId.externalSystemId && id.id === this.externalId.id);
            
            if (modelIdx !== -1) {
                this.externalId = this.view.value[modelIdx];
              
                if (this.summaryKey !== SummaryKey.NEW) {
                    if (!this.oldValue && this.value !== this.externalId.id) {
                        this.oldValue = this.externalId.id;
                    } else if (!this.oldExternalSystemId && this.externalSystemId !== this.externalId.externalSystemId) {
                        this.oldExternalSystemId = this.externalId.externalSystemId;
                    }
                    
                    if (this.oldValue && this.value === this.oldValue) {
                        this.oldValue = null;
                    }
                    if (this.oldExternalSystemId && this.oldExternalSystemId === this.externalSystemId) {
                        this.oldExternalSystemId = null;
                    }
                    if (this.oldValue || this.oldExternalSystemId) {
                        this.setSummaryKey(SummaryKey.UPDATE);
                    } else {
                        this.setSummaryKey(SummaryKey.UNMODIFIED);
                    }
                }
                
                this.externalId.id = this.value;
                this.externalId.externalSystemId = this.externalSystemId;
            }
        }
        
        this.editor.value = JSON.parse(JSON.stringify(this.view.value));
    }
    
    revert() {
      if (this.summaryKey === SummaryKey.NEW) {
          let valIdx = this.view.value.findIndex((id: ExternalId) => id.id === this.value && id.externalSystemId === this.externalSystemId);
          let leIdx = this.view.listElements.findIndex((le: ListElementView) => le.value === this.value && le.externalSystemId === this.externalSystemId && le.externalId === this.externalId);
          
          if (valIdx !== -1 && leIdx !== -1) {
            this.view.value.splice(valIdx,1);
            this.view.listElements.splice(leIdx,1);
            
            this.editor.value = JSON.parse(JSON.stringify(this.view.value));
          }
      } else if (this.summaryKey === SummaryKey.UNMODIFIED) {
          this.setSummaryKey(SummaryKey.DELETE);
          
          let valIdx = this.view.value.findIndex((id: ExternalId) => id.id === this.value && id.externalSystemId === this.externalSystemId);
          
          if (valIdx !== -1) {
            this.view.value.splice(valIdx,1);
            
            this.editor.value = JSON.parse(JSON.stringify(this.view.value));
          }
      } else if (this.summaryKey === SummaryKey.DELETE) {
          this.setSummaryKey(SummaryKey.UNMODIFIED);
          
          this.view.value.push({
              type: "EXTERNAL_ID",
              id: this.value,
              externalSystemId: this.externalSystemId
          });
          
          this.editor.value = JSON.parse(JSON.stringify(this.view.value));
      } else if (this.summaryKey === SummaryKey.UPDATE || this.summaryKey === SummaryKey.VALUE_CHANGE) {
          if (this.oldValue) {
              this.value = this.oldValue;
          }
          if (this.oldExternalSystemId) {
              this.externalSystemId = this.oldExternalSystemId;
          }
          this.updateModel();
      }
    }
    
    hasError(): boolean {
        return false;
    }
    
    hasWarning(): boolean {
        return false;
    }
}

export class ListDiffView extends StandardDiffView
{
  lService: LocalizationService;
  editor: StandardAttributeCRModel;
  listElements?: ListElementView[];
  oldValue?: any;
  attributeCode: string;
  
  constructor(lService: LocalizationService, editor: StandardAttributeCRModel) {
      super(editor, lService);
      this.lService = lService;
      this.editor = editor;

      this.populate();
  }
  
  add(id: ExternalId) {
      this.value.push(id);
      this.listElements.push(new ListElementView(this.lService, this.editor, SummaryKey.NEW, this, id, null, null));
      
      this.editor.value = JSON.parse(JSON.stringify(this.value));
  }
  
  populate() {
      let editorVal = this.editor.value;
      if (editorVal == null) {
        editorVal = [];
      }

      this.listElements = [];
      
      if (this.editor.diff) {
          let old = this.editor.diff.oldValue ? this.editor.diff.oldValue : [];
          let neu = this.editor.diff.newValue ? this.editor.diff.newValue : [];
        
          this.value = JSON.parse(JSON.stringify(neu));
          
          /* Diff the oldValue with the newValue */
          
          let newMatched = new Set();
          
          let oldLen = old.length;
          let newLen = neu.length;
          for (let i = 0; i < oldLen; ++i) {
              let oldId: ExternalId = old[i];
              
              let matched = false;
              for (let j = 0; j < newLen; ++j) {
                  let newId: ExternalId = this.value[j];
                  if (!newMatched.has(j) && (newId.id === oldId.id || newId.externalSystemId === oldId.externalSystemId)) {
                      matched = true;
                      newMatched.add(j);
                      this.listElements.push(new ListElementView(this.lService, this.editor, null, this, newId as ExternalId, oldId.id, oldId.externalSystemId)); // TODO : constructor needs to figure out the SummaryKey as well as oldValues
                      break;
                  }
              }
              
              if (!matched) {
                  this.listElements.push(new ListElementView(this.lService, this.editor, SummaryKey.DELETE, this, null, oldId.id, oldId.externalSystemId));
              }
          };
          
          for (let i = 0; i < newLen; ++i) {
              let newId: ExternalId = this.value[i];
              if (!newMatched.has(i)) {
                  this.listElements.push(new ListElementView(this.lService, this.editor, SummaryKey.NEW, this, newId as ExternalId, null, null));
              }
          }
      } else {
          this.value = JSON.parse(JSON.stringify(editorVal));
          let summaryKey = (this.editor.editAction.actionType === "CreateGeoObjectAction") ? SummaryKey.NEW : SummaryKey.UNMODIFIED;
          this.value.forEach(id => this.listElements.push(new ListElementView(this.lService, this.editor, summaryKey, this, id as ExternalId, null, null)));
      }
  }
}

export class StandardAttributeCRModel {

    changeRequest: ChangeRequest;

		diff: { oldValue?: any, newValue?: any };

    attribute: AttributeType;

    geoObject: GeoObjectOverTime;

    editAction: AbstractAction;

    code: string;

    private _isValid: boolean = true;

    constructor(attr: AttributeType, geoObject: GeoObjectOverTime, cr: ChangeRequest) {
        this.attribute = attr;
        this.geoObject = geoObject;
        this.changeRequest = cr;
        this.initialize();
    }

    initialize(): void {
        let actions = this.changeRequest.actions;

        if (this.changeRequest.type === "CreateGeoObject") {
            if (actions.length > 0 && actions[0].actionType === ActionTypes.CREATEGEOOBJECTACTION) {
                this.editAction = actions[0];
            }
        } else {
            actions.forEach((action: AbstractAction) => {
                if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                    let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                    if (this.attribute.code === updateAttrAction.attributeName) {
                        this.editAction = action;
                    }
                }
            });

            if (this.editAction == null) {
                this.editAction = new UpdateAttributeAction(this.attribute.code);
            }
        }

        let len = this.changeRequest.actions.length;
        for (let i = 0; i < len; ++i) {
            let action: AbstractAction = actions[i];

            if (action.actionType === ActionTypes.UPDATEATTRIBUTETACTION) {
                let updateAttrAction: UpdateAttributeAction = action as UpdateAttributeAction;

                if (this.attribute.code === updateAttrAction.attributeName) {
                    this.diff = updateAttrAction.attributeDiff;
                }
            } else if (action.actionType === ActionTypes.CREATEGEOOBJECTACTION) {
              // Nothing to do here. Create actions don't have diffs.
            } else {
                console.log("Unexpected action : " + action.actionType, action);
            }
        }
    }

    public hasChanges(): boolean {
        return this.diff != null;
    }

    isValid(): boolean {
        return this._isValid;
    }

    validate(): boolean {
        return this._isValid;
    }

    set value(val: any) {
        if (this.changeRequest.type === "CreateGeoObject") {
            this.geoObject.attributes[this.attribute.code] = val;
        } else {
            if (this.diff != null) {
                if (this.areValuesEqual(this.diff.oldValue, val)) {
                    delete (this.editAction as UpdateAttributeAction).attributeDiff;
                    this.diff = null;

                    let index = this.changeRequest.actions.findIndex(findAction => findAction === this.editAction);
                    if (index !== -1) {
                        this.changeRequest.actions.splice(index, 1);
                    }
                } else {
                    this.diff.newValue = val;
                }
            } else {
                this.diff = { oldValue: this.geoObject.attributes[this.attribute.code], newValue: val };

                (this.editAction as UpdateAttributeAction).attributeDiff = this.diff;
                this.changeRequest.actions.push(this.editAction);
            }
        }
    }

    get value(): any {
        if (this.changeRequest.type === "CreateGeoObject") {
            return this.geoObject.attributes[this.attribute.code];
        } else {
            if (this.diff != null) {
                return this.diff.newValue;
            } else {
                return this.geoObject.attributes[this.attribute.code];
            }
        }
    }

    areValuesEqual(val1: any, val2: any): boolean {
        if (this.attribute.type === "boolean") {
            return val1 === val2;
        }

        if ((val1 === "" && val2 == null) || (val2 === "" && val1 == null) ||
            (val1 == null && val2 != null && val2.length === 0) || (val2 == null && val1 != null && val1.length === 0)
          ) {
            return true;
        }

        if (!val1 && !val2) {
            return true;
        } else if ((!val1 && val2) || (!val2 && val1)) {
            return false;
        }
        
        if (this.attribute.type === "list" && this.attribute.code === "altIds") {
            let len1 = val1.length;
            let len2 = val2.length;
            if ((!len1 || !len2) || len1 !== len2) {
              return false;
            }
            
            for (let i = 0; i < len1; ++i) {
                if (val2.findIndex(id => AlternateId.getKey(id) === AlternateId.getKey(val1[i])) === -1) {
                    return false;
                }
            }
            return true;
        }

        return val1 === val2;
    }

}
