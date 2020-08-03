import { LocalizedValue } from "@shared/model/core";
import { HttpErrorResponse } from "@angular/common/http";

export const LOCALIZED_LABEL: LocalizedValue = {
	localizedValue: "Test System",
	localeValues: [
		{
			locale: "defaultLocale",
			value: "Test System"
		}
	]
};

export const MOCK_HTTP_ERROR_RESPONSE: HttpErrorResponse = {
	error: {
		localizedMessage: 'Test Error Message'
	},
	name: 'HttpErrorResponse',
	message: 'Test Error Message',
	ok: false,
} as HttpErrorResponse;