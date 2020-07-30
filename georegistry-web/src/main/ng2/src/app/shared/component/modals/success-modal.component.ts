import { Component, Input, OnInit } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { LocalizationService } from '@shared/service';

@Component({
	selector: 'success-modal',
	templateUrl: './success-modal.component.html',
	styleUrls: ['./success-modal.css']
})
export class SuccessModalComponent implements OnInit {
    /*
     * Message
     */
	@Input() message: string;

	constructor(public bsModalRef: BsModalRef, private localizeService: LocalizationService) {
	}

	ngOnInit(): void {
		this.message = this.message ? this.message : this.localizeService.decode("success.modal.default.message");
	}

}
