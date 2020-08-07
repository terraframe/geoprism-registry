import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList, MasterListVersion } from '@registry/model/registry';
import { ErrorHandler, ConfirmModalComponent } from '@shared/component';
import { PublishModalComponent } from './publish-modal.component';

import { RegistryService } from '@registry/service';
import { LocalizationService, AuthService } from '@shared/service';

@Component({
	selector: 'master-list-history',
	templateUrl: './master-list-history.component.html',
	styleUrls: []
})
export class MasterListHistoryComponent implements OnInit {
	message: string = null;
	list: MasterList = null;
	forDate: string = '';

	@Input() oid: string;

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;


	constructor(public service: RegistryService, private router: Router,
		private modalService: BsModalService, private localizeService: LocalizationService, authService: AuthService) {

		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
	}

	ngOnInit(): void {
		this.service.getMasterListHistory(this.oid, "EXPLORATORY").then(list => {
			this.list = list;
		});
	}


	onPublish(): void {
		this.message = null;

		this.service.createMasterListVersion(this.list.oid, this.forDate).then(version => {
			this.list.versions.push(version);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onViewMetadata(): void {
		this.bsModalRef = this.modalService.show(PublishModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.readonly = true;
		this.bsModalRef.content.master = this.list;
		this.bsModalRef.content.isNew = false;
	}


	onView(version: MasterListVersion): void {
		this.router.navigate(['/registry/master-list/', version.oid, false])
	}

	onDelete(version: MasterListVersion): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + version.forDate + ']';
		this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");

		this.bsModalRef.content.onConfirm.subscribe(data => {
			this.service.deleteMasterListVersion(version.oid).then(response => {
				this.list.versions = this.list.versions.filter((value, index, arr) => {
					return value.oid !== version.oid;
				});

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}


	error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}
