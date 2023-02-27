///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ModalStepIndicatorComponent } from "@shared/component/modals/modal-step-indicator.component";
import { ModalStepIndicatorService } from "@shared/service";


describe("ModalStepIndicatorComponent", () => {
	let component: ModalStepIndicatorComponent;
	let fixture: ComponentFixture<ModalStepIndicatorComponent>;
	let service: ModalStepIndicatorService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ModalStepIndicatorComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				ModalModule.forRoot()
			],
			providers: [
				BsModalService,
				BsModalRef,
				ModalStepIndicatorService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		// mock response
		service = TestBed.inject(ModalStepIndicatorService);

		// initialize component
		fixture = TestBed.createComponent(ModalStepIndicatorComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`should have the 'Test' Step`, async(() => {
		service.setStepConfig({ steps: [{ active: true, enabled: true, label: 'Test' }] });

		expect(component.stepConfig.steps).toBeTruthy();
		expect(component.stepConfig.steps.length).toEqual(1);
	}));
});
