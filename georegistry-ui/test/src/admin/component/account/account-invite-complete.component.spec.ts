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


