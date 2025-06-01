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

import { Component, OnInit } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { Router } from '@angular/router';

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service/localization.service";
import { LabeledPropertyGraphType } from "@registry/model/labeled-property-graph-type";
import { RegistryService } from "@registry/service";
import { RDFExport } from "@registry/model/rdf-export";

@Component({
    selector: "rdf-export",
    templateUrl: "./rdf-export.component.html",
    styleUrls: []
})
export class RDFExportComponent implements OnInit {

    currentDate: Date = new Date();
    message: string = null;

    type: LabeledPropertyGraphType = null;

    exportGeometryType: string | null = "NO_GEOMETRIES";

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: RegistryService,
        private router: Router,
        private lService: LocalizationService) {
    }

    ngOnInit(): void {
        this.type = {
            oid: null,
            graphType: "single",
            displayLabel: this.lService.create(),
            description: this.lService.create(),
            code: "graph_" + Math.floor(Math.random() * 999999),
            hierarchy: '',
            strategyType: "",
            strategyConfiguration: {
                code: null,
                typeCode: null
            }
        }
    }

    onSubmit(type: LabeledPropertyGraphType): void {
        const agtr: string[] = (type.graphTypes == null || type.graphTypes.length == 0) ? [] : JSON.parse(type.graphTypes);

        const graphTypes = agtr.map(value => {
            const split = value.split('$@~');

            return { code: split[1], typeCode: split[0] }
        })

        const typeCodes: string[] = (type.geoObjectTypeCodes == null || type.geoObjectTypeCodes.length == 0) ? [] : JSON.parse(type.geoObjectTypeCodes);
        const businessTypeCodes: string[] = (type.businessTypeCodes == null || type.businessTypeCodes.length == 0) ? [] : JSON.parse(type.businessTypeCodes);
        const businessEdgeCodes: string[] = (type.businessEdgeCodes == null || type.businessEdgeCodes.length == 0) ? [] : JSON.parse(type.businessEdgeCodes);

        const config: RDFExport = {
            geomExportType: this.exportGeometryType,
            typeCodes,
            graphTypes,
            businessTypeCodes,
            businessEdgeCodes
        };

        this.service.rdfExportStart(config).then(() => {
            this.router.navigate(["/registry/scheduled-jobs"]);
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    onCancel(): void {
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
