import { Component, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { LocalizationManagerService } from '@admin/service/localization-manager.service';
import { AllLocaleInfo } from '@admin/model/localization-manager';
import { LocaleView } from '@shared/model/core';
import { LocalizationService } from '@shared/service/localization.service';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

@Component({
	selector: 'locale-modal',
	templateUrl: './locale-modal.component.html',
	styleUrls: []
})
export class NewLocaleModalComponent {

	allLocaleInfo: AllLocaleInfo;
	
	@Input() locale: LocaleView = new LocaleView(this.lService);
	
	@Input() isNew: boolean = true;

	public onSuccess: Subject<LocaleView>;

	constructor(public bsModalRef: BsModalRef, private localizationManagerService: LocalizationManagerService, private modalService: BsModalService, private lService: LocalizationService) { }

	ngOnInit(): void {
		this.allLocaleInfo = new AllLocaleInfo();
		
		this.localizationManagerService.getNewLocaleInfo().then(allLocaleInfoIN => {
			this.allLocaleInfo = allLocaleInfoIN;
		}).catch((err: HttpErrorResponse) => {

			this.bsModalRef.hide();
			this.error(err);
		});

		this.onSuccess = new Subject();
	}

	submit(): void {

		this.localizationManagerService.installLocale(this.locale).then((locale: LocaleView) => {
			this.onSuccess.next(locale);

			this.bsModalRef.hide();
		}).catch((err: HttpErrorResponse) => {
			this.bsModalRef.hide();
			this.error(err);
		});
	}

	cancel(): void {
		this.bsModalRef.hide();
	}

	public error(err: HttpErrorResponse): void {
		this.bsModalRef = ErrorHandler.showErrorAsDialog(err, this.modalService);
	}
}
