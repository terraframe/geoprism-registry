import { Component, OnInit, AfterViewInit, ElementRef, Input, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import {
	trigger,
	state,
	style,
	animate,
	transition
} from '@angular/animations'
import { NgControl, Validators, FormBuilder } from '@angular/forms';

import { GeoObjectType, Attribute } from '@registry/model/registry';
import { HierarchyService } from '@registry/service';
import { ErrorHandler } from '@shared/component/error-handler/error-handler';
import { GeoObjectAttributeCodeValidator } from '../../../factory/form-validation.factory';



@Component({
	selector: 'attribute-input',
	templateUrl: './attribute-input.component.html',
	styleUrls: ['./attribute-input.css'],
	animations: [
		trigger('toggleInputs', [
			state('none, void',
				style({ 'opacity': 0 })
			),
			state('show',
				style({ 'opacity': 1 })
			),
			transition('none => show', animate('300ms'))
			//   transition('show => none', animate('100ms'))
		])
	]
})
export class AttributeInputComponent implements OnInit {

	@Input() disableCodeField: boolean = false;
	@Input() excludeDescription: boolean = false;
	@Input() isDecimal: boolean = false;
	@Input() geoObjectType: GeoObjectType;
	@Input() attribute: Attribute;
	@Output() attributeChange = new EventEmitter<Attribute>();
	message: string = null;

	state: string = 'none';

	constructor(private hierarchyService: HierarchyService, private cdr: ChangeDetectorRef) {
	}

	ngOnInit(): void {

	}

	ngAfterViewInit() {
		this.state = 'show';
		this.cdr.detectChanges();
	}

	ngOnChanges() {

	}

	ngOnDestroy() {

	}

	handleOnSubmit(): void {

	}

	toggleIsUnique(): void {
		this.attribute.unique = !this.attribute.unique;
	}

	animate(): void {
		this.state = "none";
	}

	onAnimationDone(event: AnimationEvent): void {
		this.state = "show";
	}

	isValid(): boolean {
		if (this.attribute.code) {

			// if code has a space
			if (this.attribute.code.indexOf(" ") !== -1) {
				return false;
			}

			if (this.attribute.label.localeValues[0].value.length === 0) {
				return false;
			}

			if (this.isDecimal) {
				console.log(this.attribute);
			}
			
			if (this.isDecimal && (this.attribute.precision == null || this.attribute.precision.toString() == '')) {
				return false;
			}

			if (this.isDecimal && (this.attribute.scale == null || this.attribute.scale.toString() == '')) {
				return false;
			}


			return true;
		}

		return false;
	}
}
