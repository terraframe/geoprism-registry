import { Component, OnInit, Input } from '@angular/core';
import {
	trigger,
	style,
	animate,
	transition
} from '@angular/animations'
import { BsModalRef } from 'ngx-bootstrap/modal';
import { HttpErrorResponse } from "@angular/common/http";

import { StepConfig } from '@shared/model/modal';
import { LocalizationService, ModalStepIndicatorService } from '@shared/service';
import { ErrorHandler } from '@shared/component';

import { GeoObjectType, AttributeTerm, Term, ManageGeoObjectTypeModalState, GeoObjectTypeModalStates } from '@registry/model/registry';
import { RegistryService, GeoObjectTypeManagementService } from '@registry/service';

@Component({
	selector: 'edit-term-option-input',
	templateUrl: './edit-term-option-input.component.html',
	styleUrls: [],
	animations: [
		trigger('openClose',
			[
				transition(
					':enter', [
					style({ 'opacity': 0 }),
					animate('500ms', style({ 'opacity': 1 }))
				]
				),
				transition(
					':leave', [
					style({ 'opacity': 1 }),
					animate('0ms', style({ 'opacity': 0 })),

				]
				)]
		)
	]
})
export class EditTermOptionInputComponent implements OnInit {

	@Input() geoObjectType: GeoObjectType;
	@Input() attribute: AttributeTerm = null;
	@Input() termOption: Term;

	message: string = null;
	modalState: ManageGeoObjectTypeModalState = { "state": GeoObjectTypeModalStates.editAttribute, "attribute": this.attribute, "termOption": "" };
	modalStepConfig: StepConfig = {
		"steps": [
			{ "label": this.localizeService.decode("modal.step.indicator.manage.geoobjecttype"), "active": true, "enabled": false },
			{ "label": this.localizeService.decode("modal.step.indicator.manage.attributes"), "active": true, "enabled": false },
			{ "label": this.localizeService.decode("modal.step.indicator.edit.attribute"), "active": true, "enabled": false },
			{ "label": this.localizeService.decode("modal.step.indicator.manage.term.options"), "active": true, "enabled": false },
			{ "label": this.localizeService.decode("modal.step.indicator.edit.term.option"), "active": true, "enabled": true }

		]
	};

	constructor(public bsModalRef: BsModalRef, private modalStepIndicatorService: ModalStepIndicatorService, private geoObjectTypeManagementService: GeoObjectTypeManagementService,
		private localizeService: LocalizationService, private registryService: RegistryService) {
	}

	ngOnInit(): void {
		this.modalStepIndicatorService.setStepConfig(this.modalStepConfig);
	}

	ngAfterViewInit() {

	}

	ngOnDestroy() {
	}

	handleOnSubmit(): void {

		this.registryService.updateAttributeTermTypeOption(this.attribute.rootTerm.code, this.termOption).then(data => {
			// Update the term definition on the attribute
			const index = this.attribute.rootTerm.children.findIndex(t => t.code === data.code);

			if (index !== -1) {
				this.attribute.rootTerm.children[index] = data;
			}

			this.geoObjectTypeManagementService.setModalState({ "state": GeoObjectTypeModalStates.manageTermOption, "attribute": this.attribute, "termOption": null })
		}).catch((err: HttpErrorResponse) => {
			this.error(err);
		});
	}

	isFormValid(): boolean {
		// let isAttrValid: boolean = this.attributeInputComponent.isValid();

		// if(isAttrValid){
		//     return true;
		// }

		// return false;
		return true
	}

	cancel(): void {
		this.geoObjectTypeManagementService.setModalState({ "state": GeoObjectTypeModalStates.manageTermOption, "attribute": this.attribute, "termOption": null })
	}

	error(err: HttpErrorResponse): void {
		this.message = ErrorHandler.getMessageFromError(err);
	}

}
