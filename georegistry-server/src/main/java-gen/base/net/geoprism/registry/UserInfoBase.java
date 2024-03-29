/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 84502665)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to UserInfo.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class UserInfoBase extends com.runwaysdk.business.Business
{
  public final static String CLASS = "net.geoprism.registry.UserInfo";
  public static final java.lang.String ALTFIRSTNAME = "altFirstName";
  public static final java.lang.String ALTLASTNAME = "altLastName";
  public static final java.lang.String ALTPHONENUMBER = "altPhoneNumber";
  public static final java.lang.String CREATEDATE = "createDate";
  public static final java.lang.String CREATEDBY = "createdBy";
  public static final java.lang.String DEPARTMENT = "department";
  public static final java.lang.String ENTITYDOMAIN = "entityDomain";
  public static final java.lang.String EXTERNALSYSTEMOID = "externalSystemOid";
  public static final java.lang.String GEOPRISMUSER = "geoprismUser";
  public static final java.lang.String KEYNAME = "keyName";
  public static final java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static final java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static final java.lang.String LOCKEDBY = "lockedBy";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String OWNER = "owner";
  public static final java.lang.String POSITION = "position";
  public static final java.lang.String SEQ = "seq";
  public static final java.lang.String SITEMASTER = "siteMaster";
  public static final java.lang.String TYPE = "type";
  private static final long serialVersionUID = 84502665;
  
  public UserInfoBase()
  {
    super();
  }
  
  public String getAltFirstName()
  {
    return getValue(ALTFIRSTNAME);
  }
  
  public void validateAltFirstName()
  {
    this.validateAttribute(ALTFIRSTNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getAltFirstNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(ALTFIRSTNAME);
  }
  
  public void setAltFirstName(String value)
  {
    if(value == null)
    {
      setValue(ALTFIRSTNAME, "");
    }
    else
    {
      setValue(ALTFIRSTNAME, value);
    }
  }
  
  public String getAltLastName()
  {
    return getValue(ALTLASTNAME);
  }
  
  public void validateAltLastName()
  {
    this.validateAttribute(ALTLASTNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getAltLastNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(ALTLASTNAME);
  }
  
  public void setAltLastName(String value)
  {
    if(value == null)
    {
      setValue(ALTLASTNAME, "");
    }
    else
    {
      setValue(ALTLASTNAME, value);
    }
  }
  
  public String getAltPhoneNumber()
  {
    return getValue(ALTPHONENUMBER);
  }
  
  public void validateAltPhoneNumber()
  {
    this.validateAttribute(ALTPHONENUMBER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getAltPhoneNumberMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(ALTPHONENUMBER);
  }
  
  public void setAltPhoneNumber(String value)
  {
    if(value == null)
    {
      setValue(ALTPHONENUMBER, "");
    }
    else
    {
      setValue(ALTPHONENUMBER, value);
    }
  }
  
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public void validateCreateDate()
  {
    this.validateAttribute(CREATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getCreateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(CREATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getCreatedBy()
  {
    if (getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(CREATEDBY));
    }
  }
  
  public String getCreatedByOid()
  {
    return getValue(CREATEDBY);
  }
  
  public void validateCreatedBy()
  {
    this.validateAttribute(CREATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getCreatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(CREATEDBY);
  }
  
  public String getDepartment()
  {
    return getValue(DEPARTMENT);
  }
  
  public void validateDepartment()
  {
    this.validateAttribute(DEPARTMENT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getDepartmentMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(DEPARTMENT);
  }
  
  public void setDepartment(String value)
  {
    if(value == null)
    {
      setValue(DEPARTMENT, "");
    }
    else
    {
      setValue(DEPARTMENT, value);
    }
  }
  
  public com.runwaysdk.system.metadata.MdDomain getEntityDomain()
  {
    if (getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomain.get(getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainOid()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void validateEntityDomain()
  {
    this.validateAttribute(ENTITYDOMAIN);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getEntityDomainMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomain value)
  {
    if(value == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, value.getOid());
    }
  }
  
  public void setEntityDomainId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(ENTITYDOMAIN, "");
    }
    else
    {
      setValue(ENTITYDOMAIN, oid);
    }
  }
  
  public String getExternalSystemOid()
  {
    return getValue(EXTERNALSYSTEMOID);
  }
  
  public void validateExternalSystemOid()
  {
    this.validateAttribute(EXTERNALSYSTEMOID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getExternalSystemOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(EXTERNALSYSTEMOID);
  }
  
  public void setExternalSystemOid(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALSYSTEMOID, "");
    }
    else
    {
      setValue(EXTERNALSYSTEMOID, value);
    }
  }
  
  public net.geoprism.GeoprismUser getGeoprismUser()
  {
    if (getValue(GEOPRISMUSER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.GeoprismUser.get(getValue(GEOPRISMUSER));
    }
  }
  
  public String getGeoprismUserOid()
  {
    return getValue(GEOPRISMUSER);
  }
  
  public void validateGeoprismUser()
  {
    this.validateAttribute(GEOPRISMUSER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getGeoprismUserMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(GEOPRISMUSER);
  }
  
  public void setGeoprismUser(net.geoprism.GeoprismUser value)
  {
    if(value == null)
    {
      setValue(GEOPRISMUSER, "");
    }
    else
    {
      setValue(GEOPRISMUSER, value.getOid());
    }
  }
  
  public void setGeoprismUserId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(GEOPRISMUSER, "");
    }
    else
    {
      setValue(GEOPRISMUSER, oid);
    }
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
  }
  
  public void validateKeyName()
  {
    this.validateAttribute(KEYNAME);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getKeyNameMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(KEYNAME);
  }
  
  public void setKeyName(String value)
  {
    if(value == null)
    {
      setValue(KEYNAME, "");
    }
    else
    {
      setValue(KEYNAME, value);
    }
  }
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public void validateLastUpdateDate()
  {
    this.validateAttribute(LASTUPDATEDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF getLastUpdateDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeDateTimeDAOIF)mdClassIF.definesAttribute(LASTUPDATEDATE);
  }
  
  public com.runwaysdk.system.SingleActor getLastUpdatedBy()
  {
    if (getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedByOid()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public void validateLastUpdatedBy()
  {
    this.validateAttribute(LASTUPDATEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLastUpdatedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LASTUPDATEDBY);
  }
  
  public com.runwaysdk.system.SingleActor getLockedBy()
  {
    if (getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActor.get(getValue(LOCKEDBY));
    }
  }
  
  public String getLockedByOid()
  {
    return getValue(LOCKEDBY);
  }
  
  public void validateLockedBy()
  {
    this.validateAttribute(LOCKEDBY);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getLockedByMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(LOCKEDBY);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public com.runwaysdk.system.Actor getOwner()
  {
    if (getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.Actor.get(getValue(OWNER));
    }
  }
  
  public String getOwnerOid()
  {
    return getValue(OWNER);
  }
  
  public void validateOwner()
  {
    this.validateAttribute(OWNER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF getOwnerMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF)mdClassIF.definesAttribute(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.Actor value)
  {
    if(value == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, value.getOid());
    }
  }
  
  public void setOwnerId(java.lang.String oid)
  {
    if(oid == null)
    {
      setValue(OWNER, "");
    }
    else
    {
      setValue(OWNER, oid);
    }
  }
  
  public String getPosition()
  {
    return getValue(POSITION);
  }
  
  public void validatePosition()
  {
    this.validateAttribute(POSITION);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getPositionMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(POSITION);
  }
  
  public void setPosition(String value)
  {
    if(value == null)
    {
      setValue(POSITION, "");
    }
    else
    {
      setValue(POSITION, value);
    }
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public void validateSeq()
  {
    this.validateAttribute(SEQ);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeLongDAOIF getSeqMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeLongDAOIF)mdClassIF.definesAttribute(SEQ);
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public void validateSiteMaster()
  {
    this.validateAttribute(SITEMASTER);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getSiteMasterMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(SITEMASTER);
  }
  
  public String getType()
  {
    return getValue(TYPE);
  }
  
  public void validateType()
  {
    this.validateAttribute(TYPE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF getTypeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.UserInfo.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF)mdClassIF.definesAttribute(TYPE);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public static UserInfoQuery getAllInstances(String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    UserInfoQuery query = new UserInfoQuery(new com.runwaysdk.query.QueryFactory());
    com.runwaysdk.business.Entity.getAllInstances(query, sortAttribute, ascending, pageSize, pageNumber);
    return query;
  }
  
  public static UserInfo get(String oid)
  {
    return (UserInfo) com.runwaysdk.business.Business.get(oid);
  }
  
  public static UserInfo getByKey(String key)
  {
    return (UserInfo) com.runwaysdk.business.Business.get(CLASS, key);
  }
  
  public static UserInfo lock(java.lang.String oid)
  {
    UserInfo _instance = UserInfo.get(oid);
    _instance.lock();
    
    return _instance;
  }
  
  public static UserInfo unlock(java.lang.String oid)
  {
    UserInfo _instance = UserInfo.get(oid);
    _instance.unlock();
    
    return _instance;
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
