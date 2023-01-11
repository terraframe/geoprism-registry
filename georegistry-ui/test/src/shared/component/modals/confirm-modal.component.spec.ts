import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ConfirmModalComponent } from "@shared/component/modals/confirm-modal.component";
import { LocalizationService } from "@shared/service/localization.service";
import { ModalTypes } from '@shared/model/modal';


describe("ConfirmModalComponent", () => {
	let component: ConfirmModalComponent;
	let fixture: ComponentFixture<ConfirmModalComponent>;
	let mockService: LocalizationService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ConfirmModalComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				ModalModule.forRoot()
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
		fixture = TestBed.createComponent(ConfirmModalComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`should have a title 'Test Message'`, async(() => {
		expect(component.message).toEqual('Test Message');
	}));

	it(`should have a cancel text 'Test Message'`, async(() => {
		expect(component.cancelText).toEqual('Test Message');
	}));

	it(`should have a submit text 'Test Message'`, async(() => {
		expect(component.submitText).toEqual('Test Message');
	}));

	it(`should have a type equal to warning`, async(() => {
		expect(component.type).toEqual(ModalTypes.warning);
	}));
});
