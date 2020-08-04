import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { NewLocaleModalComponent } from "@admin/component/localization-manager/new-locale-modal.component";
import { LocalizationService, EventService, AuthService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { MOCK_HTTP_ERROR_RESPONSE, LOCALIZED_LABEL } from "@test/shared/mocks";
import { LocalizationManagerService } from "@admin/service/localization-manager.service";

describe("NewLocaleModalComponent", () => {
	let component: NewLocaleModalComponent;
	let fixture: ComponentFixture<NewLocaleModalComponent>;
	let service: LocalizationManagerService;
	let authService: AuthService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [NewLocaleModalComponent],
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
				LocalizationManagerService,
				LocalizationService,
				EventService,
				AuthService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		service = TestBed.inject(LocalizationManagerService);
		authService = TestBed.inject(AuthService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");
		lService.create = jasmine.createSpy().and.returnValue(LOCALIZED_LABEL);

		// initialize component
		fixture = TestBed.createComponent(NewLocaleModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init`, async(() => {
		expect(component.onSuccess).toBeTruthy();
	}));

	it(`Init method`, fakeAsync(() => {
		service.getNewLocaleInfo = jasmine.createSpy().and.returnValue(Promise.resolve({
			countries: [{ key: 'en_US', label: 'US' }],
			languages: [{ key: 'en_US', label: 'US' }]
		}));

		component.ngOnInit();

		tick(500);

		expect(component.allLocaleInfo).toBeTruthy();
		expect(component.onSuccess).toBeTruthy();
	}));


	it('Test submit', fakeAsync(() => {
		let response = null;

		component.onSuccess.subscribe(data => {
			response = data;
		})

		service.installLocale = jasmine.createSpy().and.returnValue(
			Promise.resolve({ locale: "en_US" })
		);

		component.submit();

		tick(500);

		expect(response).toBeTruthy();
		expect(response).toEqual("en_US");
	}));
});


