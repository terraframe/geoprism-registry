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
