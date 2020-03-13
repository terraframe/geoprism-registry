import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import { Subscription, Observable } from 'rxjs';
import 'rxjs/add/operator/toPromise';
import 'rxjs/add/operator/finally';
import { HttpErrorResponse } from '@angular/common/http';

import { PublishModalComponent } from './publish-modal.component';
import { MasterList, MasterListVersion } from '../../model/registry';

import { RegistryService } from '../../service/registry.service';
import { AuthService } from '../../../shared/service/auth.service';

declare var acp: any;

@Component({
	selector: 'published-master-list-history',
	templateUrl: './published-master-list-history.component.html',
	styleUrls: []
})
export class PublishedMasterListHistoryComponent implements OnInit {
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
	private bsModalRef: BsModalRef;

	pollingData: Subscription;

	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;


	constructor(public service: RegistryService, private router: Router, private modalService: BsModalService, authService: AuthService) {

		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
	}

	ngOnInit(): void {
		this.service.getMasterListHistory(this.oid, "PUBLISHED").then(list => {
			this.list = list;

			this.onPageChange(1);
		});

		this.pollingData = Observable.interval(5000).subscribe(() => {
			this.onPageChange(this.page.pageNumber);
		});
	}

	ngOnDestroy() {
		this.pollingData.unsubscribe();
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

	onPageChange(pageNumber: any): void {
		if (this.list != null) {

			this.message = null;

			this.service.getPublishMasterListJobs(this.list.oid, this.page.pageSize, pageNumber, "createDate", true).then(response => {

				this.page = response;

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
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
	}


	onView(version: MasterListVersion): void {
		event.preventDefault();

		this.router.navigate(['/registry/master-list/', version.oid])
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
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
