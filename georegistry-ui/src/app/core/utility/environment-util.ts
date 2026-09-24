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

import { environment } from "src/environments/environment";

export default class EnvironmentUtil {

    static getApiUrl(): string {
        let context = environment.apiUrl;

        if (context == '.') {
            context = "";
        }

        return context;

    }

    /**
     * Returns the fully qualified URL of the application root, without a trailing
     * slash (e.g. "https://example.com/gpr").
     *
     * The URL is resolved against document.baseURI, so it honors the <base href>
     * that the server injects into index.html. Use this anywhere an absolute URL is
     * required (MapLibre tile / glyph sources, websockets) so that the app keeps
     * working when it is deployed under a context path.
     */
    static getAbsoluteApiUrl(): string {
        let context = environment.apiUrl;

        if (context == null || context === '' || context === '.') {
            context = './';
        } else if (!context.endsWith('/')) {
            context = context + '/';
        }

        return new URL(context, document.baseURI).href.replace(/\/+$/, '');
    }

}
