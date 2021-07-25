import { LocalizedValue, PageResult } from "@shared/model/core";
import { HttpErrorResponse } from "@angular/common/http";


export const LOCALIZED_LABEL: LocalizedValue = new LocalizedValue("Test System", [
		{
			locale: "defaultLocale",
			value: "Test System"
		}
	])

export const MOCK_HTTP_ERROR_RESPONSE: HttpErrorResponse = {
	error: {
		localizedMessage: 'Test Error Message'
	},
	name: 'HttpErrorResponse',
	message: 'Test Error Message',
	ok: false,
} as HttpErrorResponse;



export function PAGE<T>(value: T, pageNumber?: number): PageResult<T> {
	return {
		count: 1,
		pageNumber: pageNumber ? pageNumber : 1,
		pageSize: 10,
		resultSet: [value]
	};
}
