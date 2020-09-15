import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { webSocket, WebSocketSubject } from "rxjs/webSocket";

import { HttpErrorResponse } from '@angular/common/http';

import { PublishModalComponent } from './publish-modal.component';
import { MasterList, MasterListVersion } from '@registry/model/registry';

import { ErrorHandler, ConfirmModalComponent } from '@shared/component';
import { RegistryService } from '@registry/service';
import { AuthService, LocalizationService } from '@shared/service';

declare var acp: any;

@Component({
	selector: 'published-master-list-history',
	templateUrl: './published-master-list-history.component.html',
	styleUrls: []
})
export class PublishedMasterListHistoryComponent implements OnInit, OnDestroy {
	message: string = null;
	list: MasterList = null;
	page: any = {
		pageSize: 10,
		pageNumber: 1,
		count: 1,
		results: []
	};

	@Input() oid: string;

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	notifier: WebSocketSubject<{ type: string, content: any }>;

	isAdmin: boolean;
	isSRA: boolean;


	constructor(public service: RegistryService, private router: Router, private modalService: BsModalService, public authService: AuthService, private localizeService: LocalizationService) {

		this.isAdmin = authService.isAdmin();
		this.isSRA = authService.isSRA();
	}

	ngOnInit(): void {
		this.service.getMasterListHistory(this.oid, "PUBLISHED").then(list => {
			this.list = list;

			this.onPageChange(1);
		});

		let baseUrl = "wss://" + window.location.hostname + (window.location.port ? ':' + window.location.port : '') + acp;

		this.notifier = webSocket(baseUrl + '/websocket/notify');
		this.notifier.subscribe(message => {
			if (message.type === 'PUBLISH_JOB_CHANGE') {
				this.onPageChange(this.page.pageNumber);
			}
		});
	}

	ngOnDestroy() {
		this.notifier.complete();
	}

	//isGeoObjectTypeRM(type: string): boolean {
	//	return this.authService.isGeoObjectTypeRM(type);
	//}

	onDeleteMasterListVersion(version: MasterListVersion): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.message = this.localizeService.decode("confirm.modal.verify.delete") + ' [' + version.forDate + ']';
		this.bsModalRef.content.submitText = this.localizeService.decode("modal.button.delete");

		this.bsModalRef.content.onConfirm.subscribe(data => {
			this.service.deleteMasterListVersion(version.oid).then(response => {
				this.updateList();

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		});
	}

	updateList(): void {
		// update the list of versions. 
		this.service.getMasterListHistory(this.oid, "PUBLISHED").then(list => {
			this.list = list;
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onPublish(): void {
		this.message = null;

		this.service.publishMasterListVersions(this.list.oid).then((data: { job: string }) => {
			// Refresh the page
			this.onPageChange(this.page.pageNumber);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onPageChange(pageNumber: number): void {
		if (this.list != null) {

			this.message = null;

			this.service.getPublishMasterListJobs(this.list.oid, this.page.pageSize, pageNumber, "createDate", true).then(response => {

				this.page = response;

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});

			this.updateList();
		}
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
		this.router.navigate(['/registry/master-list/', version.oid, true])
	}

	onPublishShapefile(version: MasterListVersion): void {

		this.service.publishShapefile(version.oid).then(() => {

			this.onPageChange(this.page.pageNumber);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onDownloadShapefile(version: MasterListVersion): void {
		window.location.href = acp + '/master-list/download-shapefile?oid=' + version.oid;
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
