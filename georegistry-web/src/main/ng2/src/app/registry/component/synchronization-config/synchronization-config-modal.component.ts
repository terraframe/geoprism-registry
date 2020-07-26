import { Component, OnInit, ViewChild } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LocalizationService } from '../../../shared/service/localization.service';

import { SynchronizationConfig, OrgSyncInfo, GeoObjectType } from '../../model/registry';
import { SynchronizationConfigService } from '../../service/synchronization-config.service';
import { RegistryService } from '../../service/registry.service';
import { ErrorHandler } from '../../../shared/component/error-handler/error-handler';
import {CustomAttributeConfig} from '../../model/sync';

export interface LevelRow {
  isAttributeEditor: boolean;
  
  level?: any;
  levelNum?: number;
  hasAttributes?: boolean;
  
  attrCfg?: GOTAttributeConfig;
}
export interface GOTAttributeConfig {
  geoObjectTypeCode?: string;
  attrs: CustomAttributeConfig[];
}

@Component({
  selector: 'synchronization-config-modal',
  templateUrl: './synchronization-config-modal.component.html',
  styleUrls: []
})
export class SynchronizationConfigModalComponent implements OnInit {
	message: string = null;
	
	@ViewChild('form') form;

	config: SynchronizationConfig = {
		organization: null,
		system: null,
		hierarchy: null,
		label: this.lService.create(),
		configuration: {}
	};

	organizations: OrgSyncInfo[] = [];

	cOrg: OrgSyncInfo = null;
	cSystem: { label: string, oid: string, type: string } = null;

	types: GeoObjectType[] = [];
	
	levelRows: LevelRow[] = [];


    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	onSuccess: Subject<SynchronizationConfig>;


	constructor(private service: SynchronizationConfigService, private registryService: RegistryService, private lService: LocalizationService, private bsModalRef: BsModalRef) { }

	ngOnInit(): void {
		this.onSuccess = new Subject();
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

			if (this.cSystem != null && this.cSystem.type === 'DHIS2ExternalSystem') {
				// Get the types	
				this.registryService.getGeoObjectTypes(null, [this.config.hierarchy]).then(types => {
					this.types = types;
				});

			}
			
			this.levelRows = [];
			for (var i = 0; i < this.config.configuration.levels.length; ++i)
			{
			  var level = this.config.configuration.levels[i];
			  
			  var levelRow: LevelRow = { level: level, levelNum: i, isAttributeEditor:false };
			  
			  if (level.attributes != null && Object.keys(level.attributes).length > 0)
			  {
			    levelRow.hasAttributes = true;
			  }
			  
			  this.levelRows.push(levelRow);
			}
		}
	}

	onOrganizationSelected(): void {
		let index = this.organizations.findIndex(org => org.code === this.config.organization);

		if (index !== -1) {
			this.cOrg = this.organizations[index];
		}
		else {
			this.cOrg = null;
			this.cSystem = null;
		}
	}

	onChange(): void {
		let index = this.cOrg.systems.findIndex(system => system.oid === this.config.system);

		if (index !== -1) {
			this.cSystem = this.cOrg.systems[index];
		}
		else {
			this.cSystem = null;
		}


		if (this.cSystem != null && this.cSystem.type === 'DHIS2ExternalSystem') {
			// Get the types	
			this.registryService.getGeoObjectTypes(null, [this.config.hierarchy]).then(types => {
				this.types = types;
			});

			if (this.config.configuration['levels'] == null) {
			  var lvl = {
          type: null,
          geoObjectType: null,
          level: 0
        };
				this.config.configuration['levels'] = [lvl];
				this.levelRows.push({level:lvl, levelNum: 0, isAttributeEditor:false});
			}

		}
		else {
			this.types = [];
		}
	}

	addLevel(): void {
	  var lvl = {
      type: null,
      geoObjectType: null,
      level: this.config.configuration.levels.length
    };
		var len = this.config.configuration['levels'].push(lvl);
		this.levelRows.push({ level: lvl, levelNum: len-1, isAttributeEditor:false });
	}

	removeLevel(levelNum: number, levelRowIndex: number): void {
		if (levelNum < this.config.configuration['levels'].length) {
			var editorIndex = this.getEditorIndex();
			if (editorIndex === levelRowIndex+1)
			{
			  this.levelRows.splice(editorIndex, 1);
			}
			
			this.levelRows.splice(levelRowIndex, 1);
			
			var newLevelNum = 0;
			for (var i = 0; i < this.levelRows.length; ++i)
			{
			  var levelRow: LevelRow = this.levelRows[i];
			  
			  levelRow.levelNum = newLevelNum;
			  
			  if (!levelRow.isAttributeEditor)
			  {
			    newLevelNum = newLevelNum + 1;
			  }
			}
			
			this.config.configuration['levels'].splice(levelNum, 1);
		}
	}
	
	getEditorIndex(): number {
	  for (var i = 0; i < this.levelRows.length; ++i)
    {
      var levelRow = this.levelRows[i];
      
      if (levelRow.isAttributeEditor)
      {
        return i;
      }
    }
    
    return -1;
	}
	
	configureAttributes(levelRow: any): void {
	  var editorIndex = this.getEditorIndex();
    
    if (editorIndex != -1)
    {
      this.levelRows.splice(editorIndex, 1);
      
      if (editorIndex == levelRow.levelNum + 1)
      {
        return;
      }
    }
	
    this.onSelectGeoObjectType(levelRow.level.geoObjectType, levelRow.levelNum, false);
  }
  
  getTermOptions(attr: CustomAttributeConfig, dhis2Id: string)
  {
    for (var i = 0; i < attr.dhis2Attrs.length; ++i)
    {
      var dhis2Attr = attr.dhis2Attrs[i];
      
      if (dhis2Attr.dhis2Id === dhis2Id)
      {
        return dhis2Attr.options;
      }
    }
  }
	
	onChangeAttrMapping(attr: any, newValue: string, levelRow: LevelRow)
	{
	  if (newValue == null || newValue === "")
	  {
	    if (attr.terms != null)
	    {
	      attr.terms = {};
	    }
	  
	    return;
	  }
	
	  for (var j = 0; j < levelRow.attrCfg.attrs.length; j++)
	  {
	    if (levelRow.attrCfg.attrs[j].name === attr.name)
	    {
    	  var attrCfgAttr: any = levelRow.attrCfg.attrs[j];
    	
    	  if (attrCfgAttr.terms != null)
    	  {
    	    attr.terms = {};
    	  
    	    for (var i = 0; i < attrCfgAttr.terms.length; ++i)
    	    {
    	      var term = attrCfgAttr.terms[i];
    	    
    	      attr.terms[term.code] = null;
    	    }
    	  }
    	  
    	  return;
  	  }
	  }
	}
	
	onSelectGeoObjectType(geoObjectTypeCode: string, levelRowIndex: number, isDifferentGot: boolean = true) {
    if (geoObjectTypeCode === "" || geoObjectTypeCode == null)
    {
      var levelRow: LevelRow = this.levelRows[levelRowIndex];
      
      levelRow.hasAttributes = false;
      levelRow.attrCfg = null;
      levelRow.level.attributes = {};
      
      var editorIndex = this.getEditorIndex();
      
      if (editorIndex != -1 && editorIndex === levelRowIndex+1)
      {
        this.levelRows.splice(editorIndex, 1);
      }
      
      return;
    }
    
    var attrCfg = this.levelRows[levelRowIndex].attrCfg;
    if (attrCfg != null && attrCfg.geoObjectTypeCode === geoObjectTypeCode)
    {
      // Resume an editing session on attributes that we fetched previously
      
      var editorIndex = this.getEditorIndex();
      
      if (editorIndex != -1 && editorIndex !== levelRowIndex + 1)
      {
        this.levelRows.splice(editorIndex, 1);
        
        if (editorIndex < levelRowIndex)
        {
          levelRowIndex = levelRowIndex - 1;
        }
      }
      
      this.levelRows.splice(levelRowIndex+1, 0, {isAttributeEditor:true, attrCfg:attrCfg});
    }
    else
    {
  	  this.service.getCustomAttrCfg(geoObjectTypeCode, this.config.system).then( (attrs: CustomAttributeConfig[]) => {
  	    var editorIndex = this.getEditorIndex();
  	    var levelRow: LevelRow = this.levelRows[levelRowIndex];
  	    var level = levelRow.level;
  	    
  	    if (isDifferentGot)
  	    {
  	      level.attributes = {};
  	    }
  	    
  	    levelRow.attrCfg = {geoObjectTypeCode: geoObjectTypeCode, attrs:attrs};
  	    
  	    if (editorIndex != -1 && (editorIndex === levelRowIndex+1 || attrs.length > 0))
        {
          this.levelRows.splice(editorIndex, 1);
          
          if (editorIndex < levelRowIndex)
          {
            levelRowIndex = levelRowIndex - 1;
          }
        }
  	  
  	    if (attrs.length > 0)
  	    {
  	      levelRow.hasAttributes = true;
  	      
  	      for (var i = 0; i < attrs.length; ++i)
  	      {
  	        var attr = attrs[i];
  	        
  	        if (isDifferentGot || level.attributes[attr.name] == null)
  	        { 
    	        level.attributes[attr.name] = {
    	          name: attr.name,
    	          externalId: null
    	        };
    	        
    	        if ( attr.terms != null && attr.terms.length > 0 )
    	        {
    	          level.attributes[attr.name].terms = {};
    	        
    	          for (var j = 0; j < attr.terms.length; ++j)
                {
                  var term = attr.terms[j];
                  
                  level.attributes[attr.name].terms[term.code] = null;
                }
    	        }
  	        }
  	      }
  	      
  	      this.levelRows.splice(levelRowIndex+1, 0, {isAttributeEditor:true, attrCfg:{geoObjectTypeCode: geoObjectTypeCode, attrs:attrs}});
  	    }
  	    else
  	    {
  	      levelRow.hasAttributes = false;
  	    }
  	  }).catch((err: HttpErrorResponse) => {
        this.error(err);
      });
    }
	}

	onSubmit(): void {
		this.service.apply(this.config).then(cfg => {

			this.onSuccess.next(cfg);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	cancel(): void {

		if (this.config.oid != null) {
			this.service.unlock(this.config.oid).then(() => {
				this.bsModalRef.hide();
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});

		}
		else {
			this.bsModalRef.hide();
		}
	}

	error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}
