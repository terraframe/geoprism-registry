import { ChangeType } from "@registry/model/constants";
import { ChangeRequest } from "@registry/model/crtable";
import { AttributeType, GeoObjectOverTime, GeoObjectType, HierarchyOverTime, ValueOverTime } from "@registry/model/registry";
import { GeoJsonLayerDataSource, GeometryService, LayerDataSource, ParamLayer, RegistryService, Layer, GeoJsonLayer } from "@registry/service";
import { DateService, LocalizationService } from "@shared/service";
import { Subject } from "rxjs";
import { ChangeRequestChangeOverTimeAttributeEditor } from "./change-request-change-over-time-attribute-editor";
import { StandardAttributeCRModel } from "./StandardAttributeCRModel";
import { ValueOverTimeCREditor } from "./ValueOverTimeCREditor";

export class ChangeRequestEditor {

    changeRequest: ChangeRequest;

    // eslint-disable-next-line no-undef
    attributeEditors: (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[];

    geometryAttributeType: AttributeType;

    parentAttributeType: AttributeType;

    geoObject: GeoObjectOverTime;

    geoObjectType: GeoObjectType;

    hierarchies: HierarchyOverTime[];

    private _isValid: boolean;

    onChangeSubject : Subject<ChangeType> = new Subject<ChangeType>();

    localizationService: LocalizationService;

    dateService: DateService;

    registryService: RegistryService;

    geomService: GeometryService;

    constructor(changeRequest: ChangeRequest, geoObject: GeoObjectOverTime, geoObjectType: GeoObjectType, hierarchies: HierarchyOverTime[], geometryAttributeType: AttributeType, parentAttributeType: AttributeType, localizationService: LocalizationService, dateService: DateService, registryService: RegistryService, geomService: GeometryService) {
        this.changeRequest = changeRequest;
        this.geoObject = geoObject;
        this.geoObjectType = geoObjectType;
        this.geometryAttributeType = geometryAttributeType;
        this.parentAttributeType = parentAttributeType;
        this.hierarchies = hierarchies;
        this.localizationService = localizationService;
        this.dateService = dateService;
        this.registryService = registryService;

        this.attributeEditors = this.generateAttributeEditors();
        this.validate();

        let crEditor = this;
        this.geomService.registerDataSourceProvider({
            getId(): string {
                return changeRequest.oid;
            },
            getDataSource(dataSourceId: string): LayerDataSource {
                let editor = crEditor.findEditorForValueOverTime(dataSourceId);
                return editor.getEditor(dataSourceId);
            }
        });
    }

    private generateAttributeEditors() {
        let geoObjectAttributeExcludes: string[] = ["uid", "sequence", "type", "lastUpdateDate", "createDate"];

        let editors = [];

        let attrs = this.geoObjectType.attributes.slice(); // intentionally a shallow copy
        attrs = attrs.filter(attr => geoObjectAttributeExcludes.indexOf(attr.code) === -1);
        attrs.push(this.geometryAttributeType);
        attrs.push(this.parentAttributeType);

        attrs.forEach(attr => {
            if (attr.code !== "_PARENT_") {
                if (attr.isChangeOverTime) {
                    let editor = new ChangeRequestChangeOverTimeAttributeEditor(this, attr, null);

                    editors.push(editor);
                } else {
                    let editor = new StandardAttributeCRModel(attr, this.geoObject, this.changeRequest);

                    editors.push(editor);
                }
            } else {
                this.hierarchies.forEach(hierarchy => {
                    let editor = new ChangeRequestChangeOverTimeAttributeEditor(this, attr, hierarchy);

                    editors.push(editor);
                });
            }
        });

        return editors;
    }

    public hasChanges(): boolean {
        let hasChanges = false;

        this.attributeEditors.forEach(attributeEditor => {
            if (attributeEditor.hasChanges()) {
                hasChanges = true;
            }
        });

        return hasChanges;
    }

    public isValid(): boolean {
        return this._isValid;
    }

    public validate(skipExists: boolean = false): boolean {
        this._isValid = true;

        this.attributeEditors.forEach(attributeEditor => {
            if (!skipExists || attributeEditor.attribute.code !== "exists") {
                attributeEditor.validate(true);

                if (!attributeEditor.isValid()) {
                    this._isValid = false;
                }
            }
        });

        return this._isValid;
    }

    findExistingValueOverTimeByOid(oid: string, attributeCode: string) {
        if (this.geoObject.attributes[attributeCode]) {
            let index = this.geoObject.attributes[attributeCode].values.findIndex((vot: ValueOverTime) => vot.oid === oid);

            if (index !== -1) {
                return this.geoObject.attributes[attributeCode].values[index];
            }
        }

        return null;
    }

    findEditorForValueOverTime(oid: string): ChangeRequestChangeOverTimeAttributeEditor {
        for (let i = 0; i < this.attributeEditors.length; ++i) {
            let editor = this.attributeEditors[i];

            if (editor instanceof ChangeRequestChangeOverTimeAttributeEditor) {
                return editor;
            }
        }
    }

    public getEditorForAttribute(attribute: AttributeType, hierarchy: HierarchyOverTime = null): ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel {
        let indexOf = this.attributeEditors.findIndex(editor => (!editor.attribute.isChangeOverTime || (hierarchy == null && (editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy == null) || ((editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy != null && (editor as ChangeRequestChangeOverTimeAttributeEditor).hierarchy.code === hierarchy.code)) && editor.attribute.code === attribute.code);

        if (indexOf === -1) {
            return null;
        } else {
            return this.attributeEditors[indexOf];
        }
    }

    public getEditors(): (ChangeRequestChangeOverTimeAttributeEditor | StandardAttributeCRModel)[] {
        return this.attributeEditors;
    }

    public onChange(type: ChangeType) {
        this.onChangeSubject.next(type);
    }

    public existsAtDate(date: string) {
        let existsAttribute: AttributeType = GeoObjectType.getAttribute(this.geoObjectType, "exists");
        let existEditors = (this.getEditorForAttribute(existsAttribute) as ChangeRequestChangeOverTimeAttributeEditor).getEditors();

        let valLen = existEditors.length;
        for (let j = 0; j < valLen; ++j) {
            let editor: ValueOverTimeCREditor = existEditors[j];

            if (editor.startDate != null && editor.endDate != null && !editor.isDelete() && editor.value === true && this.dateService.between(date, editor.startDate, editor.endDate)) {
                return true;
            }
        }

        return false;
    }

}
