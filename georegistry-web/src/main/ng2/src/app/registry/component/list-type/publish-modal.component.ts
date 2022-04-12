import { Component, OnInit } from "@angular/core";
import { BsModalRef } from "ngx-bootstrap/modal";
import { Subject } from "rxjs";
import { HttpErrorResponse } from "@angular/common/http";
import { v4 as uuid } from "uuid";

import { IOService } from "@registry/service";
import { DateService } from "@shared/service/date.service";

import { ErrorHandler } from "@shared/component";
import { LocalizationService } from "@shared/service";
import { ListType, ListTypeByType } from "@registry/model/list-type";
import { ListTypeService } from "@registry/service/list-type.service";
import { AttributeType, GeoObjectType, PRESENT } from "@registry/model/registry";
import Utils from "@registry/utility/Utils";
import { RegistryCacheService } from "@registry/service/registry-cache.service";

@Component({
    selector: "list-type-publish-modal",
    templateUrl: "./publish-modal.component.html",
    styleUrls: ["./list-type-manager.css"]
})
export class ListTypePublishModalComponent implements OnInit {

    currentDate: Date = new Date();
    message: string = null;
    onListTypeChange: Subject<ListType> = null;

    list: ListType = null;

    geoObjectType: GeoObjectType = null;

    tab: string = "LIST";

    readonly: boolean = false;

    isNew: boolean = false;

    valid: boolean = true;

    gap: boolean = false;

    // eslint-disable-next-line no-useless-constructor
    constructor(
        private service: ListTypeService,
        private iService: IOService,
        private lService: LocalizationService,
        private cacheService: RegistryCacheService,
        private bsModalRef: BsModalRef,
        private dateService: DateService) { }

    ngOnInit(): void {
    }

    init(listByType: ListTypeByType, onListTypeChange: Subject<ListType>, list?: ListType): void {
        this.onListTypeChange = onListTypeChange;
        this.readonly = !listByType.write;

        const cache = this.cacheService.getTypeCache();

        cache.waitOnTypes().then(types => {
            this.geoObjectType = cache.getTypeByCode(listByType.typeCode);
        });

        if (list == null) {
            this.isNew = true;
            this.list = {
                oid: null,
                listType: "single",
                organization: "",
                typeCode: "",
                typeLabel: "",
                displayLabel: this.lService.create(),
                description: this.lService.create(),
                code: listByType.typeCode + "_" + Math.floor(Math.random() * 999999),
                hierarchies: [],
                subtypeHierarchies: [],
                listMetadata: {
                    label: this.lService.create(),
                    description: this.lService.create(),
                    originator: "",
                    collectionDate: "",
                    process: this.lService.create(),
                    progress: this.lService.create(),
                    accessConstraints: this.lService.create(),
                    useConstraints: this.lService.create(),
                    acknowledgements: this.lService.create(),
                    disclaimer: this.lService.create(),
                    contactName: "",
                    organization: "",
                    telephoneNumber: "",
                    email: ""
                },
                geospatialMetadata: {
                    label: this.lService.create(),
                    description: this.lService.create(),
                    originator: "",
                    collectionDate: "",
                    process: this.lService.create(),
                    progress: this.lService.create(),
                    accessConstraints: this.lService.create(),
                    useConstraints: this.lService.create(),
                    acknowledgements: this.lService.create(),
                    disclaimer: this.lService.create(),
                    contactName: "",
                    organization: "",
                    telephoneNumber: "",
                    email: "",
                    topicCategories: "",
                    placeKeywords: "",
                    updateFrequency: "",
                    lineage: "",
                    languages: "",
                    scaleResolution: "",
                    spatialRepresentation: "vector",
                    referenceSystem: "EPSG4326",
                    reportSpecification: "",
                    distributionFormat: "SHAPEFILE"
                }

            };

            this.list.typeCode = listByType.typeCode;
            this.list.typeLabel = listByType.typeLabel;
            this.list.organization = listByType.orgCode;

            if (listByType.geometryType === "MULTIPOINT" || listByType.geometryType === "POINT") {
                this.list.includeLatLong = true;
            }

            this.iService.getHierarchiesForType(this.list.typeCode, true).then(hierarchies => {
                this.list.hierarchies = hierarchies;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });

            this.iService.getHierarchiesForSubtypes(this.list.typeCode, false).then(hierarchies => {
                this.list.subtypeHierarchies = hierarchies;
            }).catch((err: HttpErrorResponse) => {
                this.error(err);
            });
        } else {
            this.list = list;
            this.isNew = false;

            if (this.list.listType === "interval") {
                this.list.intervalJson.forEach(interval => {
                    interval.readonly = interval.endDate !== PRESENT ? "BOTH" : "START";
                    interval.oid = uuid();
                });
            }
        }
    }

    getIsDisabled(event): boolean {
        let elClasses = event.target.classList;
        for (let i = 0; i < elClasses.length; i++) {
            let c = elClasses[i];
            if (c === "disabled") {
                return true;
            }
        }

        return false;
    }

    onSubmit(): void {
        this.service.apply(this.list).then(response => {
            this.onListTypeChange.next(response);
            this.bsModalRef.hide();
        }).catch((err: HttpErrorResponse) => {
            this.error(err);
        });
    }

    getAttributeForFilter(filter: { attribute: string, comparator: string, value: any }): AttributeType {
        if (filter.attribute != null && filter.attribute !== "") {
            const attributeType = this.geoObjectType.attributes.find(attributeType => attributeType.code === filter.attribute);

            return attributeType;
        }

        return null;
    }

    onNewFilter(): void {
        if (this.list.filter == null) {
            this.list.filter = [];
        }

        this.list.filter.push({
            attribute: "",
            operation: "EQ",
            value: null,
            id: uuid()
        });
    }

    removeFilter(index: number): void {
        this.list.filter.splice(index, 1);
    }

    onNewInterval(): void {
        if (this.list.intervalJson == null) {
            this.list.intervalJson = [];
        }

        this.list.intervalJson.push({
            startDate: "",
            endDate: "",
            oid: uuid()
        });
    }

    removeInterval(index: number): void {
        this.list.intervalJson.splice(index, 1);

        this.handleDateChange();
    }

    handleDateChange(): void {
        if (this.list.listType === "single") {
            this.valid = (this.list.validOn != null && this.list.validOn.length > 0);
        } else if (this.list.listType === "incremental") {
            this.valid = (this.list.publishingStartDate != null && this.list.publishingStartDate.length > 0);
        } else if (this.list.listType === "interval") {
            this.valid = this.list.intervalJson.map(interval => {
                return ((interval.startDate != null && interval.startDate.length > 0) &&
                    (interval.endDate != null && interval.endDate.length > 0) &&
                    !this.dateService.after(interval.startDate, interval.endDate));
            }).reduce((a, b) => a && b);

            // Sort the entries
            this.list.intervalJson = this.list.intervalJson.sort((a, b) => {
                const d1: Date = new Date(a.startDate);
                const d2: Date = new Date(b.startDate);

                return d1 < d2 ? 1 : -1;
            });

            // Check for overlaps
            this.list.intervalJson.forEach((element, index) => {
                if (index > 0) {
                    const future = this.list.intervalJson[index - 1];

                    if (future.startDate && future.endDate && element.startDate && element.endDate) {
                        let s1: any = new Date(future.startDate);
                        let e1: any = new Date(future.endDate);
                        let s2: any = new Date(element.startDate);
                        let e2: any = new Date(element.endDate);

                        if (Utils.dateRangeOverlaps(s1.getTime(), e1.getTime(), s2.getTime(), e2.getTime())) {
                            this.valid = false;
                        }
                    }
                }
            });

            if (this.valid) {
                // Check for gap
                this.gap = false;

                this.list.intervalJson.forEach((element, index) => {
                    if (index > 0) {
                        const future = this.list.intervalJson[index - 1];

                        if (future.startDate && element.endDate) {
                            let e1: any = new Date(element.endDate);
                            let s2: any = new Date(future.startDate);

                            if (Utils.hasGap(e1.getTime(), s2.getTime())) {
                                this.gap = true;
                            }
                        }
                    }
                });
            }
        } else {
            this.valid = true;
        }
    }

    getGeoObjectTypeTermAttributeOptions(termAttributeCode: string) {
        return GeoObjectType.getGeoObjectTypeTermAttributeOptions(this.geoObjectType, termAttributeCode);
    }

    onCancel(): void {
        this.bsModalRef.hide();
    }

    handleTab(tab: string): void {
        this.tab = tab;
    }

    error(err: HttpErrorResponse): void {
        this.message = ErrorHandler.getMessageFromError(err);
    }

}
