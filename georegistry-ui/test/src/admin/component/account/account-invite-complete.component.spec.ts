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
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { of } from 'rxjs';

import { AccountInviteCompleteComponent } from "@admin/component/account/account-invite-complete.component";
import { LocalizationService, EventService, ProfileService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { USER } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { AccountService } from "@admin/service/account.service";
import { AdminModule } from "@admin/admin.module";
import { ActivatedRoute } from "@angular/router";
import { Subject } from "rxjs";

describe("AccountInviteCompleteComponent", () => {
	let component: AccountInviteCompleteComponent;
	let fixture: ComponentFixture<AccountInviteCompleteComponent>;
	let service: AccountService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AccountInviteCompleteComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				SharedModule,
				AdminModule,
			],
			providers: [
				LocalizationService,
				EventService,
				AccountService,
				ProfileService,
				AuthService,
				{
					provide: ActivatedRoute, useValue: { params: of({ token: "test-token" }) }
				},
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isSRA = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(AccountService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(AccountInviteCompleteComponent);
		component = fixture.componentInstance;

		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
	}));

	it(`Init`, async(() => {
		expect(component.message).toBeNull();
	}));

	it('Test ngOnInit', fakeAsync(() => {
		service.newUserInstance = jasmine.createSpy().and.returnValue(
			Promise.resolve(USER)
		);

		component.ngOnInit();

		tick(500);

		expect(component.token).toEqual("test-token");
		expect(component.user.oid).toEqual(USER.oid);
	}));
});


