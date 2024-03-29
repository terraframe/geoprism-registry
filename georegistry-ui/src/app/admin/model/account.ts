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

export class UserInvite {
  email: string;
  roles: any;
}

export class User {
  oid: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  inactive: boolean;
  newInstance: boolean;
  roles: string[];
  altFirstName?: string;
  altLastName?: string;
  altPhoneNumber?: string;
  position?: string;
  department?: string;
  externalSystemOid?: string;
  type?: string;
  
  constructor() {
    this.newInstance = true;
    this.type = "net.geoprism.GeoprismUser";
    this.inactive = false;
  }
}

export class Group {
  name:string;
  assigned:string;
  roles:Role[];
}

export class Account {
  user:User;
  roles: any;
  changePassword:boolean;
}

export class Role {
  type:string;
  name:string;
  label:any;
  orgCode:string;
  orgLabel:any;
  geoObjectTypeCode:string;
  geoObjectTypeLabel: any;
  assigned: boolean;
}

export class FormattedRoles {
  SRA: Role;
  ORGANIZATIONS: FormattedOrganization[];
}

export class FormattedOrganization {
  ORGANIZATIONLABEL: any;
  RA: Role;
  GEOOBJECTTYPEROLES: FormattedGeoObjectTypeRoleGroup[];
  CODE: string;
}

export class FormattedGeoObjectTypeRoleGroup {
  ENABLEDROLE: string;
  GEOOBJECTTYPELABEL: any;
  GEOOBJECTTYPEROLESGROUP: Role[];
}
