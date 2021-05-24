import { Component, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { Subject } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList, MasterListByOrg } from '@registry/model/registry';
import { RegistryService, IOService } from '@registry/service';
import { DateService } from '@shared/service/date.service';

import { ErrorHandler } from '@shared/component';
import { LocalizationService, AuthService } from '@shared/service';

@Component({
	selector: 'publish-modal',
	templateUrl: './publish-modal.component.html',
	styleUrls: []
})
export class PublishModalComponent implements OnInit {
	currentDate : Date = new Date();
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

	constructor(private service: RegistryService, private iService: IOService, private lService: LocalizationService, public bsModalRef: BsModalRef, private authService: AuthService,
		private dateService: DateService) { }

	ngOnInit(): void {

		this.onMasterListChange = new Subject();

		if (!this.master || !this.readonly) {
			this.iService.listGeoObjectTypes(true).then(types => {

				var myOrgTypes = [];
				for (var i = 0; i < types.length; ++i) {
					const orgCode = types[i].orgCode;
					const typeCode = types[i].superTypeCode != null ? types[i].superTypeCode : types[i].code;

					if (this.authService.isGeoObjectTypeRM(orgCode, typeCode)) {
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
				publishingStartDate: null,
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
				subtypeHierarchies: [],
				leaf: false,
				frequency: 'ANNUAL',
				isMaster: null,
				visibility: null
			};
		}
	}
	
	ngAfterContentInit(){
		
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

			this.iService.getHierarchiesForSubtypes(this.master.typeCode, false).then(hierarchies => {
				this.master.subtypeHierarchies = hierarchies;
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
	
	ngOnDestroy() {
		this.onMasterListChange.unsubscribe()
	}
	
	formatDate(date: string): string {
		return this.dateService.formatDateForDisplay(date);
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
