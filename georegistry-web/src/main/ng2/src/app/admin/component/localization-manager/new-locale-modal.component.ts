import { Component, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from '@angular/common/http';
import { BsModalService } from 'ngx-bootstrap/modal';
import { LocalizationManagerService } from '@admin/service/localization-manager.service';
import { AllLocaleInfo } from '@admin/model/localization-manager';
import { LocaleView } from '@shared/model/core';

import { ErrorHandler, ErrorModalComponent } from '@shared/component';

@Component({
	selector: 'new-locale-modal',
	templateUrl: './new-locale-modal.component.html',
	styleUrls: []
})
export class NewLocaleModalComponent {

	allLocaleInfo: AllLocaleInfo;
	
	@Input() locale: LocaleView = new LocaleView();
	
	@Input() isNew: boolean = true;

	public onSuccess: Subject<string>;

	constructor(public bsModalRef: BsModalRef, private localizationManagerService: LocalizationManagerService, private modalService: BsModalService) { }

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

		this.localizationManagerService.installLocale(this.locale.language.code, this.locale.country.code, this.locale.variant.code).then((response: { locale: string }) => {
			this.onSuccess.next(response.locale);

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
