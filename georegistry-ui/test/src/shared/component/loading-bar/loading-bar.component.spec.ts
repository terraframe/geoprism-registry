import { TestBed, ComponentFixture, async } from "@angular/core/testing";
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, DebugElement } from "@angular/core";

import { LoadingBarComponent } from "@shared/component/loading-bar/loading-bar.component";
import { EventService } from "@shared/service";

describe("LoadingBarComponent", () => {
	let component: LoadingBarComponent;
	let fixture: ComponentFixture<LoadingBarComponent>;
	let service: EventService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [LoadingBarComponent],
			schemas: [NO_ERRORS_SCHEMA],
			imports: [
				HttpClientTestingModule,
			],
			providers: [
				EventService
			]
		}).compileComponents();
	}));

	beforeEach(() => {
		service = TestBed.inject(EventService);
		
		// initialize component
		fixture = TestBed.createComponent(LoadingBarComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});

	it(`Init: showIndicator should be false`, async(() => {
		expect(component.showIndicator).toBeFalse();
	}));
	
	it(`Init: showIndicator should be false`, async(() => {
		service.start();
		
		expect(component.showIndicator).toBeTrue();
	}));
});
