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
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, DebugElement } from "@angular/core";

import { LocalizeComponent } from "@shared/component/localize/localize.component";
import { LocalizationService } from "@shared/service/localization.service";

describe("LocalizeComponent", () => {
	let component: LocalizeComponent;
	let fixture: ComponentFixture<LocalizeComponent>;
	let mockService: LocalizationService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [LocalizeComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
			],
			providers: [
				LocalizationService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		// mock response
		mockService = TestBed.inject(LocalizationService);
		mockService.decode = jasmine.createSpy().and.returnValue('Test Message');

		// initialize component
		fixture = TestBed.createComponent(LocalizeComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`should have a title 'Test Message'`, async(() => {
		expect(component.text).toEqual('Test Message');
	}));
});
