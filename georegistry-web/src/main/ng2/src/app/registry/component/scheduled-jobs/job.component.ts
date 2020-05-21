import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';

import { ConfirmModalComponent } from '../../../shared/component/modals/confirm-modal.component';
import { JobConflictModalComponent } from './conflict-widgets/job-conflict-modal.component';
import { ReuploadModalComponent } from './conflict-widgets/reupload-modal.component';

import { RegistryService } from '../../service/registry.service';
import { LocalizationService } from '../../../shared/service/localization.service';
import { AuthService } from '../../../shared/service/auth.service';

import { ScheduledJob } from '../../model/registry';
import { ModalTypes } from '../../../shared/model/modal';
import { IOService } from '../../service/io.service';

import { interval } from 'rxjs';

@Component({
	selector: 'job',
	templateUrl: './job.component.html',
	styleUrls: ['./scheduled-jobs.css']
})
export class JobComponent implements OnInit {
	message: string = null;
	job: ScheduledJob;
	allSelected: boolean = false;
	historyId: string = "";

	page: any = {
		count: 0,
		pageNumber: 1,
		pageSize: 10,
		results: []
	};

	timeCounter: number = 0;

    /*
     * Reference to the modal current showing
    */
	bsModalRef: BsModalRef;

	isAdmin: boolean;
	isMaintainer: boolean;
	isContributor: boolean;

	pollingData: any;
	isPolling: boolean = false;
	hasRowValidationProblem: boolean = false;

	constructor(public service: RegistryService, private modalService: BsModalService,
		private router: Router, private route: ActivatedRoute,
		private localizeService: LocalizationService, authService: AuthService, public ioService: IOService) {
		this.isAdmin = authService.isAdmin();
		this.isMaintainer = this.isAdmin || authService.isMaintainer();
		this.isContributor = this.isAdmin || this.isMaintainer || authService.isContributer();
	}

	ngOnInit(): void {

		this.historyId = this.route.snapshot.params["oid"];

		this.onPageChange(1);

	}

	ngOnDestroy() {
		this.stopPolling();
	}

	formatAffectedRows(rows: string) {
		return rows.replace(/,/g, ", ");
	}

	formatValidationResolve(obj: any) {
		return JSON.stringify(obj);
	}

	onProblemResolved(problem: any): void {
		for (let i = 0; i < this.page.results.length; ++i) {
			let pageConflict = this.page.results[i];

			if (pageConflict.id === problem.id) {
				this.page.results.splice(i, 1);
			}
		}
	}

	getFriendlyProblemType(probType: string): string {

		if (probType === "net.geoprism.registry.io.ParentCodeException") {
			return this.localizeService.decode("scheduledjobs.job.problem.type.parent.lookup");
		}

		if (probType === "net.geoprism.registry.io.PostalCodeLocationException") {
			return this.localizeService.decode("scheduledjobs.job.problem.type.postal.code.lookup");
		}

		if (probType === "net.geoprism.registry.io.AmbiguousParentException") {
			return this.localizeService.decode("scheduledjobs.job.problem.type.multi.parent.lookup");
		}

		if (probType === "net.geoprism.registry.io.InvalidGeometryException") {
			return this.localizeService.decode("scheduledjobs.job.problem.type.invalid.geom.lookup");
		}

		if (probType === "net.geoprism.registry.DataNotFoundException") {
			return this.localizeService.decode("scheduledjobs.job.problem.type.required.value.lookup");
		}

		if (
			probType === "net.geoprism.registry.roles.CreateGeoObjectPermissionException"
			|| probType === "net.geoprism.registry.roles.WriteGeoObjectPermissionException"
			|| probType === "net.geoprism.registry.roles.DeleteGeoObjectPermissionException"
			|| probType === "net.geoprism.registry.roles.ReadGeoObjectPermissionException"
		) {
			return this.localizeService.decode("scheduledjobs.job.problem.type.permission");
		}

		// if(probType === "net.geoprism.registry.io.TermValueException"){
		//   return this.localizeService.decode( "scheduledjobs.job.problem.type.postal.code.lookup" );
		// }

		if (
			probType === "com.runwaysdk.dataaccess.DuplicateDataException"
			|| probType === "net.geoprism.registry.DuplicateGeoObjectException"
		) {
			return this.localizeService.decode("scheduledjobs.job.problem.type.duplicate.data.lookup");
		}

		return probType;
	}


	onEdit(problem: any): void {
		// this.router.navigate( ['/registry/master-list-history/', code] )

		this.bsModalRef = this.modalService.show(JobConflictModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});
		this.bsModalRef.content.problem = problem;
		this.bsModalRef.content.job = this.job;
		this.bsModalRef.content.onConflictAction.subscribe(data => {
			if (data.action === 'RESOLVED') {
				this.onProblemResolved(data.data);
			}
		});
	}

	onPageChange(pageNumber: any): void {

		this.message = null;

		this.service.getScheduledJob(this.historyId, this.page.pageSize, pageNumber, true).then(response => {

			this.job = response;

			if (this.job.stage === 'IMPORT_RESOLVE') {
				this.page = this.job.importErrors;
			}
			else if (this.job.stage === 'VALIDATION_RESOLVE') {
				this.page = this.job.problems;

				for (let i = 0; i < this.page.results.length; ++i) {
					let problem = this.page.results[i];

					if (problem.type === 'RowValidationProblem') {
						this.hasRowValidationProblem = true;
					}
				}
			}

			if (!this.isPolling && this.job.status === 'RUNNING') {
				this.startPolling();
			}
			else if (this.isPolling && this.job.status != 'RUNNING') {
				this.stopPolling();
			}

		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});

	}

	stopPolling(): void {
		if (this.isPolling && this.pollingData != null) {
			this.pollingData.unsubscribe();
		}
	}

	startPolling(): void {
		this.timeCounter = 0;

		this.pollingData = interval(1000).subscribe(() => {
			this.timeCounter++

			if (this.timeCounter >= 2) {
				this.onPageChange(this.page.pageNumber);

				this.timeCounter = 0;
			}
		});

		this.isPolling = true;
	}

	onViewAllActiveJobs(): void {

	}

	onViewAllCompleteJobs(): void {

	}

	toggleAll(): void {
		this.allSelected = !this.allSelected;

		this.job.importErrors.results.forEach(row => {
			row.selected = this.allSelected;
		})
	}

	onReuploadAndResume(historyId: string): void {
		this.bsModalRef = this.modalService.show(ReuploadModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});

		this.bsModalRef.content.job = this.job;

		this.bsModalRef.content.onConfirm.subscribe(data => {
			this.router.navigate(['/registry/scheduled-jobs'])
		});
	}

	onResolveScheduledJob(historyId: string): void {
		if (this.page.results.length == 0) {
			this.service.resolveScheduledJob(historyId).then(response => {
				this.router.navigate(['/registry/scheduled-jobs']);
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});
		}
		else {
			this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
				animated: true,
				backdrop: true,
				ignoreBackdropClick: true,
			});

			if (this.job.stage === 'VALIDATION_RESOLVE') {
				this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.validationDescription");
				this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.validationButton");
			}
			else {
				this.bsModalRef.content.message = this.localizeService.decode("etl.import.resume.modal.importDescription");
				this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.resume.modal.importButton");
			}

			this.bsModalRef.content.type = ModalTypes.danger;

			this.bsModalRef.content.onConfirm.subscribe(data => {

				this.service.resolveScheduledJob(historyId).then(response => {

					this.router.navigate(['/registry/scheduled-jobs'])

				}).catch((err: HttpErrorResponse) => {
					this.error(err);
				});

			});
		}
	}

	onCancelScheduledJob(historyId: string): void {
		this.bsModalRef = this.modalService.show(ConfirmModalComponent, {
			animated: true,
			backdrop: true,
			ignoreBackdropClick: true,
		});

		this.bsModalRef.content.message = this.localizeService.decode("etl.import.cancel.modal.description");
		this.bsModalRef.content.submitText = this.localizeService.decode("etl.import.cancel.modal.button");

		this.bsModalRef.content.type = ModalTypes.danger;

		this.bsModalRef.content.onConfirm.subscribe(data => {

			this.ioService.cancelImport(this.job.configuration).then(response => {
				//this.bsModalRef.hide()
				this.router.navigate(['/registry/scheduled-jobs'])
			}).catch((err: HttpErrorResponse) => {
				this.error(err);
			});

		});
	}

	error(err: HttpErrorResponse): void {
		console.log("Encountered error", err);

		// Handle error
		if (err !== null) {
			this.message = (err.error.localizedMessage || err.error.message || err.message);
		}
	}

}
