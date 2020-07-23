import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList, MasterListByOrg } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';

import { IOService } from '../../service/io.service';
import { AuthService } from '../../../shared/service/auth.service';
import { LocalizationService } from '../../../shared/service/localization.service';

@Component({
	selector: 'publish-modal',
	templateUrl: './publish-modal.component.html',
	styleUrls: []
})
export class PublishModalComponent implements OnInit {
	message: string = null;
	master: any;

    /*
     * Observable subject for MasterList changes.  Called when an update is successful 
     */
	onMasterListChange: Subject<MasterList>;


    /*
     * List of geo object types from the system
     */
	types: { label: string, code: string }[]

    /*
     * List of geo object types from the system
     */
	readonly: boolean = false;

    /*
     * List of geo object types from the system
     */
	edit: boolean = false;

	isNew: boolean = false;

	constructor(private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, private authService: AuthService) { }

	ngOnInit(): void {

		this.onMasterListChange = new Subject();

		if (this.master == null || !this.readonly) {
			this.iService.listGeoObjectTypes(true).then(types => {

				var myOrgTypes = [];
				for (var i = 0; i < types.length; ++i) {
					if (this.authService.isGeoObjectTypeRM(types[i].orgCode, types[i].code)) {
						myOrgTypes.push(types[i]);
					}
				}
				this.types = myOrgTypes;

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});

			this.master = {
				oid: null,
				typeCode: '',
				displayLabel: this.lService.create(),
				code: '',
				representativityDate: null,
				publishDate: null,
				listAbstract: '',
				process: '',
				progress: '',
				accessConstraints: '',
				useConstraints: '',
				acknowledgements: '',
				disclaimer: '',
				contactName: '',
				organization: '',
				telephoneNumber: '',
				email: '',
				hierarchies: [],
				leaf: false,
				frequency: 'ANNUAL',
				isMaster: null,
				visibility: null
			};
		}
	}

	init(org: MasterListByOrg): void {
		this.master.organization = org.oid;
	}

	getIsDisabled(event): boolean {
		let elClasses = event.target.classList;
		for (let i = 0; i < elClasses.length; i++) {
			let c = elClasses[i];
			if (c === 'disabled') {
				return true;
			}
		}

		return false;
	}

	onChange(): void {

		if (this.master.typeCode != null && this.master.typeCode.length > 0) {
			this.iService.getHierarchiesForType(this.master.typeCode, true).then(hierarchies => {
				this.master.hierarchies = hierarchies;
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
		else {
			this.master.hierarchies = [];
		}
	}

	onSubmit(): void {
		this.service.createMasterList(this.master).then(response => {

			this.onMasterListChange.next(response);
			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onCancel(): void {
		this.bsModalRef.hide()
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
