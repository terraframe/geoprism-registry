import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { SynchronizationConfig } from '../../model/registry';
import { SynchronizationConfigService } from '../../service/synchronization-config.service';


declare var acp: any;

@Component({
	selector: 'synchronization-config',
	templateUrl: './synchronization-config.component.html',
	styleUrls: []
})
export class SynchronizationConfigComponent implements OnInit {
	message: string = null;

	config: SynchronizationConfig = null;
	page: any = {
		pageSize: 10,
		pageNumber: 1,
		count: 1,
		resultSet: []
	};

	pollingData: Subscription;

	constructor(private service: SynchronizationConfigService, private route: ActivatedRoute) { }

	ngOnInit(): void {
		const oid = this.route.snapshot.paramMap.get('oid');

		this.service.get(oid).then(config => {
			this.config = config;
		});

		//		this.pollingData = interval(5000).subscribe(() => {
		//			this.onPageChange(this.page.pageNumber);
		//		});
	}

	ngOnDestroy() {
//		this.pollingData.unsubscribe();
	}

	onRun(): void {
		this.message = null;

		//		this.service.publishMasterListVersions(this.list.oid).then((data: { job: string }) => {
		//			// Refresh the page
		//			this.onPageChange(this.page.pageNumber);
		//
		//		}).catch((err: HttpErrorResponse) => {
		//			this.error(err);
		//		});
	}

	onPageChange(pageNumber: any): void {
		if (this.config != null) {

			this.message = null;

//			this.service.getJobs(this.config.oid, this.page.pageSize, pageNumber).then(response => {
//
//				this.page = response;
//
//			}).catch((err: HttpErrorResponse) => {
//				this.error(err);
//			});
		}
	}

	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
