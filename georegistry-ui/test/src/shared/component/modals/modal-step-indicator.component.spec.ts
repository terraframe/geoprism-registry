import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ModalModule, BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

import { ModalStepIndicatorComponent } from "@shared/component/modals/modal-step-indicator.component";
import { ModalStepIndicatorService } from "@shared/service";


describe("ModalStepIndicatorComponent", () => {
	let component: ModalStepIndicatorComponent;
	let fixture: ComponentFixture<ModalStepIndicatorComponent>;
	let service: ModalStepIndicatorService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ModalStepIndicatorComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
				ModalModule.forRoot()
			],
			providers: [
				BsModalService,
				BsModalRef,
				ModalStepIndicatorService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		// mock response
		service = TestBed.inject(ModalStepIndicatorService);

		// initialize component
		fixture = TestBed.createComponent(ModalStepIndicatorComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`should have the 'Test' Step`, async(() => {
		service.setStepConfig({ steps: [{ active: true, enabled: true, label: 'Test' }] });

		expect(component.stepConfig.steps).toBeTruthy();
		expect(component.stepConfig.steps.length).toEqual(1);
	}));
});
