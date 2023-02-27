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

import { OrganizationModalComponent } from "@admin/component/organization/organization-modal.component";
import { LocalizationService, OrganizationService, EventService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";

import { ORGANIZATION } from "@test/admin/mocks";

describe("OrganizationModalComponent", () => {
	let component: OrganizationModalComponent;
	let fixture: ComponentFixture<OrganizationModalComponent>;
	let orgService: OrganizationService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [OrganizationModalComponent],
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
				OrganizationService,
				LocalizationService,
				EventService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		let lService = TestBed.inject(LocalizationService);
		orgService = TestBed.inject(OrganizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue({
			localizedValue: '',
			localeValues: [{ locale: "defaultLocale", value: 'Test' }]
		});

		// initialize component
		fixture = TestBed.createComponent(OrganizationModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.isNewOrganization).toBeTrue();
		expect(component.onSuccess).toBeTruthy();
	}));

	it('Test cancel', fakeAsync(() => {
		component.cancel();

		tick(500);

		expect(component.bsModalRef).toBeTruthy();
	}));

	it('Test submit', fakeAsync(() => {
		let response = null;

		component.onSuccess.subscribe(data => {
			response = data;
		})

		orgService.newOrganization = jasmine.createSpy().and.returnValue(
			Promise.resolve(ORGANIZATION)
		);

		component.onSubmit();

		tick(500);

		expect(response).toBeTruthy();
		expect(response.code).toEqual(ORGANIZATION.code);
	}));
});


