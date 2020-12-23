import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';
import { interval } from 'rxjs';


import { RegistryService, IOService  } from '@registry/service';
import { ScheduledJob, ScheduledJobOverview, PaginationPage } from '@registry/model/registry';

import { ErrorHandler, ConfirmModalComponent } from '@shared/component';
import { LocalizationService, AuthService } from '@shared/service';
import { ModalTypes } from '@shared/model/modal';

@Component({
	selector: 'scheduled-jobs',
	templateUrl: './scheduled-jobs.component.html',
	styleUrls: ['./scheduled-jobs.css']
})
export class ScheduledJobsComponent implements OnInit {
	message: string = null;

	activeJobsPage: PaginationPage = {
		count: 0,
		pageNumber: 1,
		pageSize: 10,
		results: []
	};

	completeJobsPage: PaginationPage = {
		count: 0,
		pageNumber: 1,
		pageSize: 10,
		results: []
	};

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;

	activeTimeCounter: number = 0;
	completeTimeCounter: number = 0;

	pollingData: any;

	isViewAllOpen: boolean = false;

	constructor(public service: RegistryService, private modalService: BsModalService, private router: Router,
		private localizeService: LocalizationService, authService: AuthService, public ioService: IOService) {
		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
	}

	ngOnInit(): void {

		this.onActiveJobsPageChange(1);

		this.pollingData = interval(1000).subscribe(() => {
			this.activeTimeCounter++
			this.completeTimeCounter++

			if (this.isViewAllOpen) {
				if (this.activeTimeCounter >= 4) {
					this.onActiveJobsPageChange(this.activeJobsPage.pageNumber);

					this.activeTimeCounter = 0;
				}
				if (this.completeTimeCounter >= 7) {
					this.onCompleteJobsPageChange(this.completeJobsPage.pageNumber);

					this.completeTimeCounter = 0;
				}
			}
			else {
				if (this.activeTimeCounter >= 2) {
					this.onActiveJobsPageChange(this.activeJobsPage.pageNumber);

					this.activeTimeCounter = 0;
				}
			}
		});

	}

	ngOnDestroy() {
		this.pollingData.unsubscribe();
	}

	formatJobStatus(job: ScheduledJobOverview) {
		if (job.status === "FEEDBACK") {
			return this.localizeService.decode("etl.JobStatus.FEEDBACK");
		}
		else if (job.status === "RUNNING" || job.status === "NEW") {
			return this.localizeService.decode("etl.JobStatus.RUNNING");
		}
		else if (job.status === "QUEUED") {
			return this.localizeService.decode("etl.JobStatus.QUEUED");
		}
		else if (job.status === "SUCCESS") {
			return this.localizeService.decode("etl.JobStatus.SUCCESS");
		}
		else if (job.status === "CANCELED") {
			return this.localizeService.decode("etl.JobStatus.CANCELED");
		}
		else if (job.status === "FAILURE") {
			return this.localizeService.decode("etl.JobStatus.FAILURE");
		}
		else {
			return this.localizeService.decode("etl.JobStatus.RUNNING");
		}
	}

	formatStepConfig(page: PaginationPage): void {

		page.results.forEach(job => {

			let stepConfig = {
				"steps": [
					{ "label": this.localizeService.decode("scheduler.step.fileImport"), "status": "COMPLETE" },

					{
						"label": this.localizeService.decode("scheduler.step.staging"),
						"status": job.stage === "NEW" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "NEW")
					},

					{
						"label": this.localizeService.decode("scheduler.step.validation"),
						"status": job.stage === "VALIDATE" || job.stage === "VALIDATION_RESOLVE" ? this.getJobStatus(job) : this.getCompletedStatus(job.stage, "VALIDATE")
					},

					{
						"label": this.localizeService.decode("scheduler.step.databaseImport"),
						"status": job.stage === "IMPORT" || job.stage === "IMPORT_RESOLVE" || job.stage === "RESUME_IMPORT" ? this.getJobStatus(job) : ""
					}
				]
			}

			job = job as ScheduledJobOverview;
			job.stepConfig = stepConfig;
		});

	}


	getCompletedStatus(jobStage: string, targetStage: string): string {
		let order = ["NEW", "VALIDATE", "VALIDATION_RESOLVE", "IMPORT", "IMPORT_RESOLVE", "RESUME_IMPORT"];

		let jobPos = order.indexOf(jobStage);
		let targetPos = order.indexOf(targetStage);
		if (targetPos < jobPos) {
			return "COMPLETE";
		}
		else {
			return "";
		}
	}

	getJobStatus(job: ScheduledJob): string {
		if (job.status === "QUEUED" || job.status === "RUNNING") {
			return "WORKING"
		}
		else if (job.status === "FEEDBACK") {
			return "STUCK";
		}

		return "";
	}


	onViewAllCompleteJobs(): void {
		this.onCompleteJobsPageChange(1);

		this.isViewAllOpen = true;
	}


	onView(code: string): void {
		this.router.navigate(['/registry/master-list-history/', code])
	}

	onActiveJobsPageChange(pageNumber: any): void {

		this.message = null;

		this.service.getScheduledJobs(this.activeJobsPage.pageSize, pageNumber, "createDate", false).then(response => {

			this.activeJobsPage = response;
			this.formatStepConfig(this.activeJobsPage);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	onCompleteJobsPageChange(pageNumber: any): void {

		this.message = null;

		this.service.getCompletedScheduledJobs(this.completeJobsPage.pageSize, pageNumber, "createDate", false).then(response => {

			this.completeJobsPage = response;
			this.formatStepConfig(this.completeJobsPage);

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}


	onCancelScheduledJob(historyId: string, job: ScheduledJob): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});

		this.bsModalRef.content.message = this.localizeService.decode("etl.import.cancel.modal.description");
		this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.cancel.modal.button");
		
		this.bsModalRef.content.type = ModalTypes.danger;

		this.bsModalRef.content.onConfirm.subscribe(data => {

			this.ioService.cancelImport(job.configuration).then(response => {
				this.bsModalRef.hide()

				for (let i = 0; i < this.activeJobsPage.results.length; ++i) {
					let activeJob = this.activeJobsPage.results[i];

					if (activeJob.jobId === job.jobId) {
						this.activeJobsPage.results.splice(i, 1);
						break;
					}
				}

				this.onViewAllCompleteJobs();

			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});

		});
	}

  onResolveScheduledJob(historyId: string, job: ScheduledJob): void {
    this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
      animated: true,
      backdrop: true,
      ignoreBackdropClick: true,
    });

    this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.importDescription");
    this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.importButton");

    this.bsModalRef.content.type = ModalTypes.danger;

    this.bsModalRef.content.onConfirm.subscribe(data => {

      this.service.resolveScheduledJob(historyId).then(response => {

        this.bsModalRef.hide()

        for (let i = 0; i < this.activeJobsPage.results.length; ++i) {
          let activeJob = this.activeJobsPage.results[i];

          if (activeJob.jobId === job.jobId) {
            this.activeJobsPage.results.splice(i, 1);
            break;
          }
        }

        this.onViewAllCompleteJobs();

      }).catch((err: HttpErrorResponse) => {
        this.error(err);
      });

    });
  }


	error(err: HttpErrorResponse): void {
			this.message = ErrorHandler.getMessageFromError(err);
	}

}
