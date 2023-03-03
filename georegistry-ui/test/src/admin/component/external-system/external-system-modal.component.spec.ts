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

import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ExternalSystemModalComponent } from "@admin/component/external-system/external-system-modal.component";
import { LocalizationService, ExternalSystemService, EventService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { ORGANIZATION, EXTERNAL_SYSTEM } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";

describe("ExternalSystemModalComponent", () => {
	let component: ExternalSystemModalComponent;
	let fixture: ComponentFixture<ExternalSystemModalComponent>;
	let systemService: ExternalSystemService;
	let authService: AuthService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ExternalSystemModalComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				FormsModule,
				SharedModule,
				ModalModule.forRoot()
			],
			providers: [
				BsModalService,
				BsModalRef,
				ExternalSystemService,
				LocalizationService,
				EventService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		systemService = TestBed.inject(ExternalSystemService);
		authService = TestBed.inject(AuthService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue({
			localizedValue: '',
			localeValues: [{ locale: "defaultLocale", value: 'Test' }]
		});

		// initialize component
		fixture = TestBed.createComponent(ExternalSystemModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.message).toBeNull();
		expect(component.onSuccess).toBeTruthy();
	}));

	it(`Init method`, async(() => {
		authService.isOrganizationRA = jasmine.createSpy().and.returnValue(true);

		component.init([ORGANIZATION], EXTERNAL_SYSTEM);

		expect(component.organizations).toBeTruthy();
		expect(component.organizations.length).toEqual(1);
		expect(component.system).toBeTruthy();
		expect(component.system.id).toEqual(EXTERNAL_SYSTEM.id);
	}));

	it(`Init method: Filter RA`, async(() => {
		authService.isOrganizationRA = jasmine.createSpy().and.returnValue(false);

		component.init([ORGANIZATION], null);

		expect(component.organizations).toBeTruthy();
		expect(component.organizations.length).toEqual(0);
	}));

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
		expect(component.message).toEqual(MOCK_HTTP_ERROR_RESPONSE.error.localizedMessage);
	}));
	
	it('Test submit', fakeAsync(() => {
		let response = null;

		component.onSuccess.subscribe(data => {
			response = data;
		})

		systemService.applyExternalSystem = jasmine.createSpy().and.returnValue(
			Promise.resolve(EXTERNAL_SYSTEM)
		);

		component.onSubmit();

		tick(500);

		expect(response).toBeTruthy();
		expect(response.id).toEqual(EXTERNAL_SYSTEM.id);
	}));
});


