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
