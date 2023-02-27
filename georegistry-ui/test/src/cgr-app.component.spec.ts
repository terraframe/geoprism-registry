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
import { NO_ERRORS_SCHEMA, DebugElement } from "@angular/core";
import { By } from "@angular/platform-browser";

import { CgrAppComponent } from "../../src/app/cgr-app.component";

describe("CgrAppComponent", () => {
	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [CgrAppComponent],
			schemas: [NO_ERRORS_SCHEMA]
		});

		TestBed.compileComponents();
	}));

	it("should have 'Dashboard' on header text", () => {
		const componentFixture: ComponentFixture<CgrAppComponent> = TestBed.createComponent(CgrAppComponent);
		const debugElement: DebugElement = componentFixture.debugElement;
		componentFixture.detectChanges();

		const headerElement = debugElement.query(By.css("#innerFrameHtml div")).nativeElement;
		expect(headerElement).toBeDefined();
	});
});
