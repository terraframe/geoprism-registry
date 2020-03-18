import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterListByOrg } from '../../model/registry';

import { PublishModalComponent } from './publish-modal.component';
import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

@Component({
	selector: 'master-list-manager',
	templateUrl: './master-list-manager.component.html',
	styleUrls: ['./master-list-manager.css']
})
export class MasterListManagerComponent implements OnInit {
	message: string = null;
	orgs: MasterListByOrg[];

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;

	constructor(public service: RegistryService, private modalService: BsModalService, private router: Router,
		private localizeService: LocalizationService, authService: AuthService) {
		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
	}

	ngOnInit(): void {

		this.service.getMasterListsByOrg().then(response => {

			this.localizeService.setLocales(response.locales);

			this.orgs = response.orgs;

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}

	onCreate(org: MasterListByOrg): void {
		this.bsModalRef = this.modalService.show(PublishModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.init(org);
		this.bsModalRef.content.onMasterListChange.subscribe(list => {
			const obj = {
				label: list.displayLabel.localizedValue,
				oid: list.oid,
				createDate: list.createDate,
				lastUpdateDate: list.lastUpdateDate,
				admin: list.admin,
				isMaster: list.isMaster
			};

			org.lists.push(obj);
		});
	}

	onView(code: string): void {
		this.router.navigate(['/registry/master-list-view/', code])
	}

	onEdit(pair: { label: string, oid: string }): void {
		this.service.getMasterList(pair.oid).then(list => {

			this.bsModalRef = this.modalService.show(PublishModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});
			this.bsModalRef.content.edit = true;
			this.bsModalRef.content.master = list;
			this.bsModalRef.content.onMasterListChange.subscribe(ret => {
				pair.label = ret.displayLabel.localizedValue;
			});
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onDelete(org: MasterListByOrg, list: { label: string, oid: string }): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + list.label + ']';
		this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");
		this.bsModalRef.content.type = "danger";

		this.bsModalRef.content.onConfirm.subscribe(data => {
			this.service.deleteMasterList(list.oid).then(response => {
				org.lists = org.lists.filter((value, index, arr) => {
					return value.oid !== list.oid;
				});

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
