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

import { LocalizedValue } from "@core/model/core";

export class Transition {

    oid?: string;
    sourceCode: string;
    sourceType: string;
    sourceText?: string;
    targetCode: string;
    targetType: string;
    targetText?: string;
    transitionType: string;
    impact: string;
    typeUpdown?: string;
    typePart?: string;
    order: number;
    isNew: boolean;

}

export class TransitionEvent {

    oid?: string;
    eventId?: string;
    beforeTypeCode: string;
    afterTypeCode: string;
    beforeTypeLabel?: string;
    afterTypeLabel?: string;
    eventDate: string;
    permissions: string[];
    description: LocalizedValue;
    transitions?: Transition[];
}

export class HistoricalRow {
    eventId: string;
    eventDate: string;
    eventType: string;
    description: LocalizedValue;
    beforeType: string;
    beforeCode: string;
    beforeLabel: LocalizedValue;
    afterType: string;
    afterCode: string;
    afterLabel: LocalizedValue;
}

