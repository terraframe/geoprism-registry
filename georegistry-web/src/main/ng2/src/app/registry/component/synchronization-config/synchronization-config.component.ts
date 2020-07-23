import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { PageResult } from '../../../shared/model/core'
import { LocalizationService } from '../../../shared/service/localization.service';

import { SynchronizationConfig, ExportScheduledJob } from '../../model/registry';
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

	page: PageResult<ExportScheduledJob> = {
		pageSize: 10,
		pageNumber: 1,
		count: 1,
		resultSet: []
	};

	pollingData: Subscription;

	constructor(private service: SynchronizationConfigService, private lService: LocalizationService, private route: ActivatedRoute) { }

	ngOnInit(): void {
		const oid = this.route.snapshot.paramMap.get('oid');

		this.service.get(oid).then(config => {
			this.config = config;
			this.onPageChange(1);
		});

		this.pollingData = interval(10000).subscribe(() => {
			this.onPageChange(this.page.pageNumber);
		});
	}

	ngOnDestroy() {
		this.pollingData.unsubscribe();
	}

	onRun(): void {
		this.message = null;

		this.service.run(this.config.oid).then(() => {
			// Refresh the page
			this.onPageChange(this.page.pageNumber);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onPageChange(pageNumber: number): void {
		if (this.config != null) {

			this.message = null;

			this.service.getJobs(this.config.oid, pageNumber, this.page.pageSize).then(response => {

				this.formatStepConfig(response);
				this.page = response;

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
	}

	formatJobStatus(job: ExportScheduledJob) {
		if (job.status === "FEEDBACK") {
			return this.lService.decode("etl.JobStatus.FEEDBACK");
		}
		else if (job.status === "RUNNING" || job.status === "NEW") {
			return this.lService.decode("etl.JobStatus.RUNNING");
		}
		else if (job.status === "QUEUED") {
			return this.lService.decode("etl.JobStatus.QUEUED");
		}
		else if (job.status === "SUCCESS") {
			return this.lService.decode("etl.JobStatus.SUCCESS");
		}
		else if (job.status === "CANCELED") {
			return this.lService.decode("etl.JobStatus.CANCELED");
		}
		else if (job.status === "FAILURE") {
			return this.lService.decode("etl.JobStatus.FAILURE");
		}
		else {
			return this.lService.decode("etl.JobStatus.RUNNING");
		}
	}

	formatStepConfig(page: PageResult<ExportScheduledJob>): void {

		page.resultSet.forEach((job: ExportScheduledJob) => {

			job.stepConfig = {
				"steps": [
					{
						"label": "Queued",
						"status": job.stage === "NEW" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "NEW")
					},

					{
						"label": "Connecting",
						"status": job.stage === "CONNECTING" || job.stage === "CONNECTION_FAILED" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "CONNECTION_FAILED")
					},
					{
						"label": "Database Export",
						"status": job.stage === "EXPORT" || job.stage === "EXPORT_RESOLVE" || job.stage === "RESUME_EXPORT" ? this.getJobStatus(job) : ""
					}
				]
			}
		});
	}

	getCompletedStatus(jobStage: string, targetStage: string): string {
		let order = ["CONNECTING", "CONNECTION_FAILED", "EXPORT", "EXPORT_RESOLVE", "RESUME_EXPORT"];

		let jobPos = order.indexOf(jobStage);
		let targetPos = order.indexOf(targetStage);

		if (targetPos < jobPos) {
			return "COMPLETE";
		}
		else {
			return "";
		}
	}

	getJobStatus(job: ExportScheduledJob): string {
		if (job.status === "QUEUED" || job.status === "RUNNING") {
			return "WORKING"
		}
		else if (job.status === "FEEDBACK" || job.status === "FAILURE") {
			return "STUCK";
		}

		return "";
	}


	error(err: HttpErrorResponse): void {
		// Handle error
		if (err !== null) {
			this.message = ((err.error && (err.error.localizedMessage || err.error.message)) || err.message || "An unspecified error has occurred");
		}
	}

}
