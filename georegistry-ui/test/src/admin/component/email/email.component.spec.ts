import { TestBed, ComponentFixture, async, tick, fakeAsync } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';

import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { EmailComponent } from "@admin/component/email/email.component";
import { LocalizationService, EventService } from "@shared/service";
import { SharedModule } from "@shared/shared.module";
import { EMAIL } from "@test/admin/mocks";
import { MOCK_HTTP_ERROR_RESPONSE } from "@test/shared/mocks";
import { EmailService } from "@admin/service/email.service";

describe("EmailComponent", () => {
	let component: EmailComponent;
	let fixture: ComponentFixture<EmailComponent>;
	let emailService: EmailService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [EmailComponent],
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
				LocalizationService,
				EventService,
				EmailService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		TestBed.inject(EventService);
		emailService = TestBed.inject(EmailService);

		let lService = TestBed.inject(LocalizationService);

		// Mock methods		
		lService.decode = jasmine.createSpy().and.returnValue("Test");

		// initialize component
		fixture = TestBed.createComponent(EmailComponent);
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

	it(`Test Error`, async(() => {
		component.error(MOCK_HTTP_ERROR_RESPONSE);

		expect(component.message).toBeTruthy();
		expect(component.message).toEqual(MOCK_HTTP_ERROR_RESPONSE.error.localizedMessage);
	}));
	
	it('Test ngOnInit', fakeAsync(() => {
		emailService.getInstance = jasmine.createSpy().and.returnValue(
			Promise.resolve(EMAIL)
		);

		component.ngOnInit();

		tick(500);

		expect(component.email.oid).toEqual(EMAIL.oid);
	}));
	
	it('Test submit', fakeAsync(() => {
		let response = null;

		component.onSuccess.subscribe(data => {
			response = data;
		})

		emailService.apply = jasmine.createSpy().and.returnValue(
			Promise.resolve(EMAIL)
		);

		component.onSubmit();

		tick(500);

		expect(response).toBeTruthy();
		expect(response).toEqual(true);
	}));
});


