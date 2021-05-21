import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';

import { PageResult } from '@shared/model/core';
import { LocalizationService } from '@shared/service';
import { ModalTypes } from '@shared/model/modal';
import { ErrorHandler, ConfirmModalComponent } from '@shared/component';

import { SynchronizationConfig } from '@registry/model/registry';
import { SynchronizationConfigService } from '@registry/service';
import { SynchronizationConfigModalComponent } from './synchronization-config-modal.component';

@Component({
	selector: 'synchronization-config-manager',
	templateUrl: './synchronization-config-manager.component.html',
	styleUrls: []
})
export class SynchronizationConfigManagerComponent implements OnInit {
	message: string = null;

	page: PageResult<SynchronizationConfig> = {
		resultSet: [],
		count: 0,
		pageNumber: 1,
		pageSize: 20
	};

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	constructor(public service: SynchronizationConfigService, private lService: LocalizationService, private router: Router, private modalService: BsModalService) { }

	ngOnInit(): void {
		this.onPageChange(1);
	}

	onPageChange(pageNumber: number): void {
		this.service.getPage(pageNumber, this.page.pageSize).then(page => {
			this.page = page;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	create(): void {
		this.service.edit(null).then(response => {

			let bsModalRef = this.modalService.show(SynchronizationConfigModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});
			bsModalRef.content.init(null, response.orgs)
			bsModalRef.content.onSuccess.subscribe(() => {
				this.onPageChange(this.page.pageNumber);
			})
		});
	}

	onEdit(config: SynchronizationConfig): void {

		this.service.edit(config.oid).then(response => {

			let bsModalRef = this.modalService.show(SynchronizationConfigModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});
			bsModalRef.content.init(response.config, response.orgs)
			bsModalRef.content.onSuccess.subscribe(() => {
				this.onPageChange(this.page.pageNumber);
			})
		});
	}

	onRemove(config: SynchronizationConfig): void {

		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.lService.decode("confirm.modal.verify.delete") + ' [' + config.label.localizedValue + ']';
		this.bsModalRef.content.submitText = this.lService.decode("modal.button.delete");
		this.bsModalRef.content.type = ModalTypes.danger;

		this.bsModalRef.content.onConfirm.subscribe(() => {
			this.service.remove(config.oid).then(() => {
				this.onPageChange(this.page.pageNumber);
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	onView(config: SynchronizationConfig): void {
		this.router.navigate(['/registry/synchronization-config/', config.oid])
	}

	error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}
