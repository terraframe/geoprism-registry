import { GeoObject } from '../hierarchy/hierarchy';

export class ChangeRequest {
  oid: string;
  createdBy: string;
  createDate: Date;
  approvalStatus: string;
  total: number;
  pending: number;
}

export class AbstractAction {
  approvalStatus: GovernanceStatus;
  createActionDate: Date;
  label: string;
}

export class UpdateGeoObjectAction extends AbstractAction {
  geoObjectJson: GeoObject;
}

export class CreateGeoObjectAction extends AbstractAction {
  geoObjectJson: GeoObject;
}

export class AddChildAction extends AbstractAction {
  childId: string;
  childTypeCode: string;
  parentId: string;
  parentTypeCode: string;
  hierarchyCode: string;
}

export class RemoveChildAction extends AbstractAction {
  childId: string;
  childTypeCode: string;
  parentId: string;
  parentTypeCode: string;
  hierarchyCode: string;
}

export class GovernanceStatus {
  key: string;
  label: string;
}

export class PageEvent {
  type:string;
  data:any;
}
