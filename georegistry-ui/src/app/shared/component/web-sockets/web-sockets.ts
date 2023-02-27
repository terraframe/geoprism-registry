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


import EnvironmentUtil from "@core/utility/environment-util";

export class WebSockets {

    static buildBaseUrl(): string {
        let protocol = "wss";

        if (window.location.protocol.indexOf("https") !== -1) {
            protocol = "wss"; // Web Socket Secure
        } else {
            protocol = "ws";
        }

        let baseUrl = protocol + "://" + window.location.hostname + (window.location.port ? ":" + window.location.port : "") + EnvironmentUtil.getApiUrl();

        return baseUrl;
    }

}
