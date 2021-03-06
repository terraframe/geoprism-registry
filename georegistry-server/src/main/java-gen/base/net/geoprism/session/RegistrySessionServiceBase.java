/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.session;

@com.runwaysdk.business.ClassSignature(hash = 2115594662)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to RegistrySessionService.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class RegistrySessionServiceBase extends com.runwaysdk.business.Util
{
  public final static String CLASS = "net.geoprism.session.RegistrySessionService";
  public static java.lang.String OID = "oid";
  private static final long serialVersionUID = 2115594662;
  
  public RegistrySessionServiceBase()
  {
    super();
  }
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.session.RegistrySessionService.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static RegistrySessionService get(String oid)
  {
    return (RegistrySessionService) com.runwaysdk.business.Util.get(oid);
  }
  
  public static java.lang.String ologin(java.lang.String serverId, java.lang.String code, java.lang.String locales, java.lang.String redirectBase)
  {
    String msg = "This method should never be invoked.  It should be overwritten in net.geoprism.session.RegistrySessionService.java";
    throw new com.runwaysdk.dataaccess.metadata.ForbiddenMethodException(msg);
  }
  
  public String toString()
  {
    if (this.isNew())
    {
      return "New: "+ this.getClassDisplayLabel();
    }
    else
    {
      return super.toString();
    }
  }
}
