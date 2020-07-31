import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, DebugElement } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ErrorModalComponent } from "@shared/component/modals/error-modal.component";
import { LocalizationService } from "@shared/service";

describe("ErrorModalComponent", () => {
	let component: ErrorModalComponent;
	let fixture: ComponentFixture<ErrorModalComponent>;
	let mockService: LocalizationService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ErrorModalComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				ModalModule
			],
			providers: [
				BsModalService,
				BsModalRef,
				LocalizationService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		// mock response
		mockService = TestBed.inject(LocalizationService);
		mockService.decode = jasmine.createSpy().and.returnValue('Test Message');

		// initialize component
		fixture = TestBed.createComponent(ErrorModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`should have a title 'Test Message'`, async(() => {
		expect(component.message).toEqual('Test Message');
	}));
});
