import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RegistryRoleType } from "@shared/model/core";
import { AuthService, LocalizationService, EventService, ProfileService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { DateService } from "@shared/service/date.service";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { BsDropdownModule } from 'ngx-bootstrap/dropdown';
import { AccountComponent } from "@admin/component/account/account.component";
import { ACCOUNT } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { AccountService } from "@admin/service/account.service";
import { AdminModule } from "@admin/admin.module";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
//import { PasswordStrengthBarComponent } from '@shared/component/password-strength-bar/password-strength-bar.component';

describe("DateService", () => {
	let component: AccountComponent;
	let fixture: ComponentFixture<AccountComponent>;
	let dateService: DateService;
	let authService: AuthService;
	let service: AccountService;
	
	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [AccountComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				BrowserAnimationsModule,
				ModalModule.forRoot(),
				BsDropdownModule.forRoot(),
				SharedModule,
				AdminModule,
			],
			providers: [
				BsModalService,
				BsModalRef,
				LocalizationService,
				EventService,
				AccountService,
				ProfileService,
				AuthService,
				DateService
			]
		}).compileComponents()
	}));
	beforeEach(() => {
		TestBed.inject(EventService);
		TestBed.inject(AuthService).isSRA = jasmine.createSpy().and.returnValue(true);
		TestBed.inject(ProfileService);
		service = TestBed.inject(AccountService);
		
//		dateService = TestBed.inject(DateService);
		
		let lService = TestBed.inject(LocalizationService);
		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
	});
	
	
	it('should get infinity Date adjusted to local offset', () => {
		dateService = TestBed.inject(DateService);
		
		expect(dateService.getPresentDate()).toEqual(new Date(5000, 11, 31, 0, 0, 0));
	});
	
});