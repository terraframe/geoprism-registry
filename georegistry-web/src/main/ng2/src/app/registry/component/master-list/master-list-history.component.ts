import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
// import 'rxjs/add/operator/toPromise';
import { finalize } from 'rxjs/operators';
import { HttpErrorResponse } from '@angular/common/http';

import { MasterList, MasterListVersion } from '../../model/registry';
import { ErrorHandler } from '../../../shared/component/error-handler/error-handler';
import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { PublishModalComponent } from './publish-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';

import { AuthService } from '../../../shared/service/auth.service';

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
	private bsModalRef: BsModalRef;

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

	onViewMetadata(event: any): void {
		event.preventDefault();

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
		event.preventDefault();

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
