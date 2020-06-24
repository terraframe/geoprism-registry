import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LocalizationService } from '../../../shared/service/localization.service';

import { SynchronizationConfig, OrgSyncInfo, GeoObjectType } from '../../model/registry';
import { SynchronizationConfigService } from '../../service/synchronization-config.service';
import { RegistryService } from '../../service/registry.service';

@Component({
	selector: 'synchronization-config-modal',
	templateUrl: './synchronization-config-modal.component.html',
	styleUrls: []
})
export class SynchronizationConfigModalComponent implements OnInit {
	message: string = null;

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
				this.config.configuration['levels'] = [{
					type: null,
					geoObjectType: null
				}];
			}

		}
		else {
			this.types = [];
		}
	}

	addLevel(): void {
		this.config.configuration['levels'].push({
			type: null,
			geoObjectType: null
		});
	}

	removeLevel(index: number): void {
		if (index < this.config.configuration['levels'].length) {
			this.config.configuration['levels'].splice(index, 1);
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
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
