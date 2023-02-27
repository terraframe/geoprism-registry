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
