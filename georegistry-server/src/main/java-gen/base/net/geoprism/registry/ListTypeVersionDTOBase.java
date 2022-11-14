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

@com.runwaysdk.business.ClassSignature(hash = 318332005)
public abstract class ListTypeVersionDTOBase extends com.runwaysdk.business.BusinessDTO
{
  public final static String CLASS = "net.geoprism.registry.ListTypeVersion";
  private static final long serialVersionUID = 318332005;
  
  protected ListTypeVersionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected ListTypeVersionDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static final java.lang.String CREATEDATE = "createDate";
  public static final java.lang.String CREATEDBY = "createdBy";
  public static final java.lang.String ENTITYDOMAIN = "entityDomain";
  public static final java.lang.String ENTRY = "entry";
  public static final java.lang.String FORDATE = "forDate";
  public static final java.lang.String GEOSPATIALACCESSCONSTRAINTS = "geospatialAccessConstraints";
  public static final java.lang.String GEOSPATIALACKNOWLEDGEMENTS = "geospatialAcknowledgements";
  public static final java.lang.String GEOSPATIALCOLLECTIONDATE = "geospatialCollectionDate";
  public static final java.lang.String GEOSPATIALCONTACTNAME = "geospatialContactName";
  public static final java.lang.String GEOSPATIALDESCRIPTION = "geospatialDescription";
  public static final java.lang.String GEOSPATIALDISCLAIMER = "geospatialDisclaimer";
  public static final java.lang.String GEOSPATIALDISTRIBUTIONFORMAT = "geospatialDistributionFormat";
  public static final java.lang.String GEOSPATIALEMAIL = "geospatialEmail";
  public static final java.lang.String GEOSPATIALLABEL = "geospatialLabel";
  public static final java.lang.String GEOSPATIALLANGUAGES = "geospatialLanguages";
  public static final java.lang.String GEOSPATIALLINEAGE = "geospatialLineage";
  public static final java.lang.String GEOSPATIALMASTER = "geospatialMaster";
  public static final java.lang.String GEOSPATIALORGANIZATION = "geospatialOrganization";
  public static final java.lang.String GEOSPATIALORIGINATOR = "geospatialOriginator";
  public static final java.lang.String GEOSPATIALPLACEKEYWORDS = "geospatialPlaceKeywords";
  public static final java.lang.String GEOSPATIALPROCESS = "geospatialProcess";
  public static final java.lang.String GEOSPATIALPROGRESS = "geospatialProgress";
  public static final java.lang.String GEOSPATIALREFERENCESYSTEM = "geospatialReferenceSystem";
  public static final java.lang.String GEOSPATIALREPORTSPECIFICATION = "geospatialReportSpecification";
  public static final java.lang.String GEOSPATIALSCALERESOLUTION = "geospatialScaleResolution";
  public static final java.lang.String GEOSPATIALSPATIALREPRESENTATION = "geospatialSpatialRepresentation";
  public static final java.lang.String GEOSPATIALTELEPHONENUMBER = "geospatialTelephoneNumber";
  public static final java.lang.String GEOSPATIALTOPICCATEGORIES = "geospatialTopicCategories";
  public static final java.lang.String GEOSPATIALUPDATEFREQUENCY = "geospatialUpdateFrequency";
  public static final java.lang.String GEOSPATIALUSECONSTRAINTS = "geospatialUseConstraints";
  public static final java.lang.String GEOSPATIALVISIBILITY = "geospatialVisibility";
  public static final java.lang.String KEYNAME = "keyName";
  public static final java.lang.String LASTUPDATEDATE = "lastUpdateDate";
  public static final java.lang.String LASTUPDATEDBY = "lastUpdatedBy";
  public static final java.lang.String LISTACCESSCONSTRAINTS = "listAccessConstraints";
  public static final java.lang.String LISTACKNOWLEDGEMENTS = "listAcknowledgements";
  public static final java.lang.String LISTCOLLECTIONDATE = "listCollectionDate";
  public static final java.lang.String LISTCONTACTNAME = "listContactName";
  public static final java.lang.String LISTDESCRIPTION = "listDescription";
  public static final java.lang.String LISTDISCLAIMER = "listDisclaimer";
  public static final java.lang.String LISTEMAIL = "listEmail";
  public static final java.lang.String LISTLABEL = "listLabel";
  public static final java.lang.String LISTMASTER = "listMaster";
  public static final java.lang.String LISTORGANIZATION = "listOrganization";
  public static final java.lang.String LISTORIGINATOR = "listOriginator";
  public static final java.lang.String LISTPROCESS = "listProcess";
  public static final java.lang.String LISTPROGRESS = "listProgress";
  public static final java.lang.String LISTTELEPHONENUMBER = "listTelephoneNumber";
  public static final java.lang.String LISTTYPE = "listType";
  public static final java.lang.String LISTUSECONSTRAINTS = "listUseConstraints";
  public static final java.lang.String LISTVISIBILITY = "listVisibility";
  public static final java.lang.String LOCKEDBY = "lockedBy";
  public static final java.lang.String MDBUSINESS = "mdBusiness";
  public static final java.lang.String OID = "oid";
  public static final java.lang.String OWNER = "owner";
  public static final java.lang.String PUBLISHDATE = "publishDate";
  public static final java.lang.String SEQ = "seq";
  public static final java.lang.String SITEMASTER = "siteMaster";
  public static final java.lang.String TYPE = "type";
  public static final java.lang.String VERSIONNUMBER = "versionNumber";
  public static final java.lang.String WORKING = "working";
  public java.util.Date getCreateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(CREATEDATE));
  }
  
  public boolean isCreateDateWritable()
  {
    return isWritable(CREATEDATE);
  }
  
  public boolean isCreateDateReadable()
  {
    return isReadable(CREATEDATE);
  }
  
  public boolean isCreateDateModified()
  {
    return isModified(CREATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getCreateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(CREATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getCreatedBy()
  {
    if(getValue(CREATEDBY) == null || getValue(CREATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(CREATEDBY));
    }
  }
  
  public String getCreatedByOid()
  {
    return getValue(CREATEDBY);
  }
  
  public boolean isCreatedByWritable()
  {
    return isWritable(CREATEDBY);
  }
  
  public boolean isCreatedByReadable()
  {
    return isReadable(CREATEDBY);
  }
  
  public boolean isCreatedByModified()
  {
    return isModified(CREATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getCreatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(CREATEDBY).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdDomainDTO getEntityDomain()
  {
    if(getValue(ENTITYDOMAIN) == null || getValue(ENTITYDOMAIN).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdDomainDTO.get(getRequest(), getValue(ENTITYDOMAIN));
    }
  }
  
  public String getEntityDomainOid()
  {
    return getValue(ENTITYDOMAIN);
  }
  
  public void setEntityDomain(com.runwaysdk.system.metadata.MdDomainDTO value)
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
  
  public boolean isEntityDomainWritable()
  {
    return isWritable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainReadable()
  {
    return isReadable(ENTITYDOMAIN);
  }
  
  public boolean isEntityDomainModified()
  {
    return isModified(ENTITYDOMAIN);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getEntityDomainMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(ENTITYDOMAIN).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeEntryDTO getEntry()
  {
    if(getValue(ENTRY) == null || getValue(ENTRY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.registry.ListTypeEntryDTO.get(getRequest(), getValue(ENTRY));
    }
  }
  
  public String getEntryOid()
  {
    return getValue(ENTRY);
  }
  
  public void setEntry(net.geoprism.registry.ListTypeEntryDTO value)
  {
    if(value == null)
    {
      setValue(ENTRY, "");
    }
    else
    {
      setValue(ENTRY, value.getOid());
    }
  }
  
  public boolean isEntryWritable()
  {
    return isWritable(ENTRY);
  }
  
  public boolean isEntryReadable()
  {
    return isReadable(ENTRY);
  }
  
  public boolean isEntryModified()
  {
    return isModified(ENTRY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getEntryMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(ENTRY).getAttributeMdDTO();
  }
  
  public java.util.Date getForDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(FORDATE));
  }
  
  public void setForDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(FORDATE, "");
    }
    else
    {
      setValue(FORDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public boolean isForDateWritable()
  {
    return isWritable(FORDATE);
  }
  
  public boolean isForDateReadable()
  {
    return isReadable(FORDATE);
  }
  
  public boolean isForDateModified()
  {
    return isModified(FORDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getForDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(FORDATE).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialAccessConstraintsDTO getGeospatialAccessConstraints()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialAccessConstraintsDTO) this.getAttributeStructDTO(GEOSPATIALACCESSCONSTRAINTS).getStructDTO();
  }
  
  public boolean isGeospatialAccessConstraintsWritable()
  {
    return isWritable(GEOSPATIALACCESSCONSTRAINTS);
  }
  
  public boolean isGeospatialAccessConstraintsReadable()
  {
    return isReadable(GEOSPATIALACCESSCONSTRAINTS);
  }
  
  public boolean isGeospatialAccessConstraintsModified()
  {
    return isModified(GEOSPATIALACCESSCONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialAccessConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALACCESSCONSTRAINTS).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialAcknowledgementsDTO getGeospatialAcknowledgements()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialAcknowledgementsDTO) this.getAttributeStructDTO(GEOSPATIALACKNOWLEDGEMENTS).getStructDTO();
  }
  
  public boolean isGeospatialAcknowledgementsWritable()
  {
    return isWritable(GEOSPATIALACKNOWLEDGEMENTS);
  }
  
  public boolean isGeospatialAcknowledgementsReadable()
  {
    return isReadable(GEOSPATIALACKNOWLEDGEMENTS);
  }
  
  public boolean isGeospatialAcknowledgementsModified()
  {
    return isModified(GEOSPATIALACKNOWLEDGEMENTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialAcknowledgementsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALACKNOWLEDGEMENTS).getAttributeMdDTO();
  }
  
  public java.util.Date getGeospatialCollectionDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(GEOSPATIALCOLLECTIONDATE));
  }
  
  public void setGeospatialCollectionDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALCOLLECTIONDATE, "");
    }
    else
    {
      setValue(GEOSPATIALCOLLECTIONDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public boolean isGeospatialCollectionDateWritable()
  {
    return isWritable(GEOSPATIALCOLLECTIONDATE);
  }
  
  public boolean isGeospatialCollectionDateReadable()
  {
    return isReadable(GEOSPATIALCOLLECTIONDATE);
  }
  
  public boolean isGeospatialCollectionDateModified()
  {
    return isModified(GEOSPATIALCOLLECTIONDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getGeospatialCollectionDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(GEOSPATIALCOLLECTIONDATE).getAttributeMdDTO();
  }
  
  public String getGeospatialContactName()
  {
    return getValue(GEOSPATIALCONTACTNAME);
  }
  
  public void setGeospatialContactName(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALCONTACTNAME, "");
    }
    else
    {
      setValue(GEOSPATIALCONTACTNAME, value);
    }
  }
  
  public boolean isGeospatialContactNameWritable()
  {
    return isWritable(GEOSPATIALCONTACTNAME);
  }
  
  public boolean isGeospatialContactNameReadable()
  {
    return isReadable(GEOSPATIALCONTACTNAME);
  }
  
  public boolean isGeospatialContactNameModified()
  {
    return isModified(GEOSPATIALCONTACTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialContactNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALCONTACTNAME).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialDescriptionDTO getGeospatialDescription()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialDescriptionDTO) this.getAttributeStructDTO(GEOSPATIALDESCRIPTION).getStructDTO();
  }
  
  public boolean isGeospatialDescriptionWritable()
  {
    return isWritable(GEOSPATIALDESCRIPTION);
  }
  
  public boolean isGeospatialDescriptionReadable()
  {
    return isReadable(GEOSPATIALDESCRIPTION);
  }
  
  public boolean isGeospatialDescriptionModified()
  {
    return isModified(GEOSPATIALDESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALDESCRIPTION).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialDisclaimerDTO getGeospatialDisclaimer()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialDisclaimerDTO) this.getAttributeStructDTO(GEOSPATIALDISCLAIMER).getStructDTO();
  }
  
  public boolean isGeospatialDisclaimerWritable()
  {
    return isWritable(GEOSPATIALDISCLAIMER);
  }
  
  public boolean isGeospatialDisclaimerReadable()
  {
    return isReadable(GEOSPATIALDISCLAIMER);
  }
  
  public boolean isGeospatialDisclaimerModified()
  {
    return isModified(GEOSPATIALDISCLAIMER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialDisclaimerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALDISCLAIMER).getAttributeMdDTO();
  }
  
  public String getGeospatialDistributionFormat()
  {
    return getValue(GEOSPATIALDISTRIBUTIONFORMAT);
  }
  
  public void setGeospatialDistributionFormat(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALDISTRIBUTIONFORMAT, "");
    }
    else
    {
      setValue(GEOSPATIALDISTRIBUTIONFORMAT, value);
    }
  }
  
  public boolean isGeospatialDistributionFormatWritable()
  {
    return isWritable(GEOSPATIALDISTRIBUTIONFORMAT);
  }
  
  public boolean isGeospatialDistributionFormatReadable()
  {
    return isReadable(GEOSPATIALDISTRIBUTIONFORMAT);
  }
  
  public boolean isGeospatialDistributionFormatModified()
  {
    return isModified(GEOSPATIALDISTRIBUTIONFORMAT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialDistributionFormatMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALDISTRIBUTIONFORMAT).getAttributeMdDTO();
  }
  
  public String getGeospatialEmail()
  {
    return getValue(GEOSPATIALEMAIL);
  }
  
  public void setGeospatialEmail(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALEMAIL, "");
    }
    else
    {
      setValue(GEOSPATIALEMAIL, value);
    }
  }
  
  public boolean isGeospatialEmailWritable()
  {
    return isWritable(GEOSPATIALEMAIL);
  }
  
  public boolean isGeospatialEmailReadable()
  {
    return isReadable(GEOSPATIALEMAIL);
  }
  
  public boolean isGeospatialEmailModified()
  {
    return isModified(GEOSPATIALEMAIL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialEmailMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALEMAIL).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialLabelDTO getGeospatialLabel()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialLabelDTO) this.getAttributeStructDTO(GEOSPATIALLABEL).getStructDTO();
  }
  
  public boolean isGeospatialLabelWritable()
  {
    return isWritable(GEOSPATIALLABEL);
  }
  
  public boolean isGeospatialLabelReadable()
  {
    return isReadable(GEOSPATIALLABEL);
  }
  
  public boolean isGeospatialLabelModified()
  {
    return isModified(GEOSPATIALLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALLABEL).getAttributeMdDTO();
  }
  
  public String getGeospatialLanguages()
  {
    return getValue(GEOSPATIALLANGUAGES);
  }
  
  public void setGeospatialLanguages(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALLANGUAGES, "");
    }
    else
    {
      setValue(GEOSPATIALLANGUAGES, value);
    }
  }
  
  public boolean isGeospatialLanguagesWritable()
  {
    return isWritable(GEOSPATIALLANGUAGES);
  }
  
  public boolean isGeospatialLanguagesReadable()
  {
    return isReadable(GEOSPATIALLANGUAGES);
  }
  
  public boolean isGeospatialLanguagesModified()
  {
    return isModified(GEOSPATIALLANGUAGES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialLanguagesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALLANGUAGES).getAttributeMdDTO();
  }
  
  public String getGeospatialLineage()
  {
    return getValue(GEOSPATIALLINEAGE);
  }
  
  public void setGeospatialLineage(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALLINEAGE, "");
    }
    else
    {
      setValue(GEOSPATIALLINEAGE, value);
    }
  }
  
  public boolean isGeospatialLineageWritable()
  {
    return isWritable(GEOSPATIALLINEAGE);
  }
  
  public boolean isGeospatialLineageReadable()
  {
    return isReadable(GEOSPATIALLINEAGE);
  }
  
  public boolean isGeospatialLineageModified()
  {
    return isModified(GEOSPATIALLINEAGE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialLineageMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALLINEAGE).getAttributeMdDTO();
  }
  
  public Boolean getGeospatialMaster()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(GEOSPATIALMASTER));
  }
  
  public void setGeospatialMaster(Boolean value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALMASTER, "");
    }
    else
    {
      setValue(GEOSPATIALMASTER, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isGeospatialMasterWritable()
  {
    return isWritable(GEOSPATIALMASTER);
  }
  
  public boolean isGeospatialMasterReadable()
  {
    return isReadable(GEOSPATIALMASTER);
  }
  
  public boolean isGeospatialMasterModified()
  {
    return isModified(GEOSPATIALMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getGeospatialMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(GEOSPATIALMASTER).getAttributeMdDTO();
  }
  
  public String getGeospatialOrganization()
  {
    return getValue(GEOSPATIALORGANIZATION);
  }
  
  public void setGeospatialOrganization(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALORGANIZATION, "");
    }
    else
    {
      setValue(GEOSPATIALORGANIZATION, value);
    }
  }
  
  public boolean isGeospatialOrganizationWritable()
  {
    return isWritable(GEOSPATIALORGANIZATION);
  }
  
  public boolean isGeospatialOrganizationReadable()
  {
    return isReadable(GEOSPATIALORGANIZATION);
  }
  
  public boolean isGeospatialOrganizationModified()
  {
    return isModified(GEOSPATIALORGANIZATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialOrganizationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALORGANIZATION).getAttributeMdDTO();
  }
  
  public String getGeospatialOriginator()
  {
    return getValue(GEOSPATIALORIGINATOR);
  }
  
  public void setGeospatialOriginator(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALORIGINATOR, "");
    }
    else
    {
      setValue(GEOSPATIALORIGINATOR, value);
    }
  }
  
  public boolean isGeospatialOriginatorWritable()
  {
    return isWritable(GEOSPATIALORIGINATOR);
  }
  
  public boolean isGeospatialOriginatorReadable()
  {
    return isReadable(GEOSPATIALORIGINATOR);
  }
  
  public boolean isGeospatialOriginatorModified()
  {
    return isModified(GEOSPATIALORIGINATOR);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialOriginatorMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALORIGINATOR).getAttributeMdDTO();
  }
  
  public String getGeospatialPlaceKeywords()
  {
    return getValue(GEOSPATIALPLACEKEYWORDS);
  }
  
  public void setGeospatialPlaceKeywords(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALPLACEKEYWORDS, "");
    }
    else
    {
      setValue(GEOSPATIALPLACEKEYWORDS, value);
    }
  }
  
  public boolean isGeospatialPlaceKeywordsWritable()
  {
    return isWritable(GEOSPATIALPLACEKEYWORDS);
  }
  
  public boolean isGeospatialPlaceKeywordsReadable()
  {
    return isReadable(GEOSPATIALPLACEKEYWORDS);
  }
  
  public boolean isGeospatialPlaceKeywordsModified()
  {
    return isModified(GEOSPATIALPLACEKEYWORDS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialPlaceKeywordsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALPLACEKEYWORDS).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialProcessDTO getGeospatialProcess()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialProcessDTO) this.getAttributeStructDTO(GEOSPATIALPROCESS).getStructDTO();
  }
  
  public boolean isGeospatialProcessWritable()
  {
    return isWritable(GEOSPATIALPROCESS);
  }
  
  public boolean isGeospatialProcessReadable()
  {
    return isReadable(GEOSPATIALPROCESS);
  }
  
  public boolean isGeospatialProcessModified()
  {
    return isModified(GEOSPATIALPROCESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialProcessMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALPROCESS).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialProgressDTO getGeospatialProgress()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialProgressDTO) this.getAttributeStructDTO(GEOSPATIALPROGRESS).getStructDTO();
  }
  
  public boolean isGeospatialProgressWritable()
  {
    return isWritable(GEOSPATIALPROGRESS);
  }
  
  public boolean isGeospatialProgressReadable()
  {
    return isReadable(GEOSPATIALPROGRESS);
  }
  
  public boolean isGeospatialProgressModified()
  {
    return isModified(GEOSPATIALPROGRESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialProgressMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALPROGRESS).getAttributeMdDTO();
  }
  
  public String getGeospatialReferenceSystem()
  {
    return getValue(GEOSPATIALREFERENCESYSTEM);
  }
  
  public void setGeospatialReferenceSystem(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALREFERENCESYSTEM, "");
    }
    else
    {
      setValue(GEOSPATIALREFERENCESYSTEM, value);
    }
  }
  
  public boolean isGeospatialReferenceSystemWritable()
  {
    return isWritable(GEOSPATIALREFERENCESYSTEM);
  }
  
  public boolean isGeospatialReferenceSystemReadable()
  {
    return isReadable(GEOSPATIALREFERENCESYSTEM);
  }
  
  public boolean isGeospatialReferenceSystemModified()
  {
    return isModified(GEOSPATIALREFERENCESYSTEM);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialReferenceSystemMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALREFERENCESYSTEM).getAttributeMdDTO();
  }
  
  public String getGeospatialReportSpecification()
  {
    return getValue(GEOSPATIALREPORTSPECIFICATION);
  }
  
  public void setGeospatialReportSpecification(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALREPORTSPECIFICATION, "");
    }
    else
    {
      setValue(GEOSPATIALREPORTSPECIFICATION, value);
    }
  }
  
  public boolean isGeospatialReportSpecificationWritable()
  {
    return isWritable(GEOSPATIALREPORTSPECIFICATION);
  }
  
  public boolean isGeospatialReportSpecificationReadable()
  {
    return isReadable(GEOSPATIALREPORTSPECIFICATION);
  }
  
  public boolean isGeospatialReportSpecificationModified()
  {
    return isModified(GEOSPATIALREPORTSPECIFICATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialReportSpecificationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALREPORTSPECIFICATION).getAttributeMdDTO();
  }
  
  public String getGeospatialScaleResolution()
  {
    return getValue(GEOSPATIALSCALERESOLUTION);
  }
  
  public void setGeospatialScaleResolution(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALSCALERESOLUTION, "");
    }
    else
    {
      setValue(GEOSPATIALSCALERESOLUTION, value);
    }
  }
  
  public boolean isGeospatialScaleResolutionWritable()
  {
    return isWritable(GEOSPATIALSCALERESOLUTION);
  }
  
  public boolean isGeospatialScaleResolutionReadable()
  {
    return isReadable(GEOSPATIALSCALERESOLUTION);
  }
  
  public boolean isGeospatialScaleResolutionModified()
  {
    return isModified(GEOSPATIALSCALERESOLUTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialScaleResolutionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALSCALERESOLUTION).getAttributeMdDTO();
  }
  
  public String getGeospatialSpatialRepresentation()
  {
    return getValue(GEOSPATIALSPATIALREPRESENTATION);
  }
  
  public void setGeospatialSpatialRepresentation(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALSPATIALREPRESENTATION, "");
    }
    else
    {
      setValue(GEOSPATIALSPATIALREPRESENTATION, value);
    }
  }
  
  public boolean isGeospatialSpatialRepresentationWritable()
  {
    return isWritable(GEOSPATIALSPATIALREPRESENTATION);
  }
  
  public boolean isGeospatialSpatialRepresentationReadable()
  {
    return isReadable(GEOSPATIALSPATIALREPRESENTATION);
  }
  
  public boolean isGeospatialSpatialRepresentationModified()
  {
    return isModified(GEOSPATIALSPATIALREPRESENTATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialSpatialRepresentationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALSPATIALREPRESENTATION).getAttributeMdDTO();
  }
  
  public String getGeospatialTelephoneNumber()
  {
    return getValue(GEOSPATIALTELEPHONENUMBER);
  }
  
  public void setGeospatialTelephoneNumber(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALTELEPHONENUMBER, "");
    }
    else
    {
      setValue(GEOSPATIALTELEPHONENUMBER, value);
    }
  }
  
  public boolean isGeospatialTelephoneNumberWritable()
  {
    return isWritable(GEOSPATIALTELEPHONENUMBER);
  }
  
  public boolean isGeospatialTelephoneNumberReadable()
  {
    return isReadable(GEOSPATIALTELEPHONENUMBER);
  }
  
  public boolean isGeospatialTelephoneNumberModified()
  {
    return isModified(GEOSPATIALTELEPHONENUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialTelephoneNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALTELEPHONENUMBER).getAttributeMdDTO();
  }
  
  public String getGeospatialTopicCategories()
  {
    return getValue(GEOSPATIALTOPICCATEGORIES);
  }
  
  public void setGeospatialTopicCategories(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALTOPICCATEGORIES, "");
    }
    else
    {
      setValue(GEOSPATIALTOPICCATEGORIES, value);
    }
  }
  
  public boolean isGeospatialTopicCategoriesWritable()
  {
    return isWritable(GEOSPATIALTOPICCATEGORIES);
  }
  
  public boolean isGeospatialTopicCategoriesReadable()
  {
    return isReadable(GEOSPATIALTOPICCATEGORIES);
  }
  
  public boolean isGeospatialTopicCategoriesModified()
  {
    return isModified(GEOSPATIALTOPICCATEGORIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialTopicCategoriesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALTOPICCATEGORIES).getAttributeMdDTO();
  }
  
  public String getGeospatialUpdateFrequency()
  {
    return getValue(GEOSPATIALUPDATEFREQUENCY);
  }
  
  public void setGeospatialUpdateFrequency(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALUPDATEFREQUENCY, "");
    }
    else
    {
      setValue(GEOSPATIALUPDATEFREQUENCY, value);
    }
  }
  
  public boolean isGeospatialUpdateFrequencyWritable()
  {
    return isWritable(GEOSPATIALUPDATEFREQUENCY);
  }
  
  public boolean isGeospatialUpdateFrequencyReadable()
  {
    return isReadable(GEOSPATIALUPDATEFREQUENCY);
  }
  
  public boolean isGeospatialUpdateFrequencyModified()
  {
    return isModified(GEOSPATIALUPDATEFREQUENCY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGeospatialUpdateFrequencyMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GEOSPATIALUPDATEFREQUENCY).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionGeospatialUseConstraintsDTO getGeospatialUseConstraints()
  {
    return (net.geoprism.registry.ListTypeVersionGeospatialUseConstraintsDTO) this.getAttributeStructDTO(GEOSPATIALUSECONSTRAINTS).getStructDTO();
  }
  
  public boolean isGeospatialUseConstraintsWritable()
  {
    return isWritable(GEOSPATIALUSECONSTRAINTS);
  }
  
  public boolean isGeospatialUseConstraintsReadable()
  {
    return isReadable(GEOSPATIALUSECONSTRAINTS);
  }
  
  public boolean isGeospatialUseConstraintsModified()
  {
    return isModified(GEOSPATIALUSECONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getGeospatialUseConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(GEOSPATIALUSECONSTRAINTS).getAttributeMdDTO();
  }
  
  public String getGeospatialVisibility()
  {
    return getValue(GEOSPATIALVISIBILITY);
  }
  
  public void setGeospatialVisibility(String value)
  {
    if(value == null)
    {
      setValue(GEOSPATIALVISIBILITY, "");
    }
    else
    {
      setValue(GEOSPATIALVISIBILITY, value);
    }
  }
  
  public boolean isGeospatialVisibilityWritable()
  {
    return isWritable(GEOSPATIALVISIBILITY);
  }
  
  public boolean isGeospatialVisibilityReadable()
  {
    return isReadable(GEOSPATIALVISIBILITY);
  }
  
  public boolean isGeospatialVisibilityModified()
  {
    return isModified(GEOSPATIALVISIBILITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGeospatialVisibilityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GEOSPATIALVISIBILITY).getAttributeMdDTO();
  }
  
  public String getKeyName()
  {
    return getValue(KEYNAME);
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
  
  public boolean isKeyNameWritable()
  {
    return isWritable(KEYNAME);
  }
  
  public boolean isKeyNameReadable()
  {
    return isReadable(KEYNAME);
  }
  
  public boolean isKeyNameModified()
  {
    return isModified(KEYNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getKeyNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(KEYNAME).getAttributeMdDTO();
  }
  
  public java.util.Date getLastUpdateDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LASTUPDATEDATE));
  }
  
  public boolean isLastUpdateDateWritable()
  {
    return isWritable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateReadable()
  {
    return isReadable(LASTUPDATEDATE);
  }
  
  public boolean isLastUpdateDateModified()
  {
    return isModified(LASTUPDATEDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getLastUpdateDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(LASTUPDATEDATE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLastUpdatedBy()
  {
    if(getValue(LASTUPDATEDBY) == null || getValue(LASTUPDATEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LASTUPDATEDBY));
    }
  }
  
  public String getLastUpdatedByOid()
  {
    return getValue(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByWritable()
  {
    return isWritable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByReadable()
  {
    return isReadable(LASTUPDATEDBY);
  }
  
  public boolean isLastUpdatedByModified()
  {
    return isModified(LASTUPDATEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLastUpdatedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LASTUPDATEDBY).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListAccessConstraintsDTO getListAccessConstraints()
  {
    return (net.geoprism.registry.ListTypeVersionListAccessConstraintsDTO) this.getAttributeStructDTO(LISTACCESSCONSTRAINTS).getStructDTO();
  }
  
  public boolean isListAccessConstraintsWritable()
  {
    return isWritable(LISTACCESSCONSTRAINTS);
  }
  
  public boolean isListAccessConstraintsReadable()
  {
    return isReadable(LISTACCESSCONSTRAINTS);
  }
  
  public boolean isListAccessConstraintsModified()
  {
    return isModified(LISTACCESSCONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListAccessConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTACCESSCONSTRAINTS).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListAcknowledgementsDTO getListAcknowledgements()
  {
    return (net.geoprism.registry.ListTypeVersionListAcknowledgementsDTO) this.getAttributeStructDTO(LISTACKNOWLEDGEMENTS).getStructDTO();
  }
  
  public boolean isListAcknowledgementsWritable()
  {
    return isWritable(LISTACKNOWLEDGEMENTS);
  }
  
  public boolean isListAcknowledgementsReadable()
  {
    return isReadable(LISTACKNOWLEDGEMENTS);
  }
  
  public boolean isListAcknowledgementsModified()
  {
    return isModified(LISTACKNOWLEDGEMENTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListAcknowledgementsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTACKNOWLEDGEMENTS).getAttributeMdDTO();
  }
  
  public java.util.Date getListCollectionDate()
  {
    return com.runwaysdk.constants.MdAttributeDateTimeUtil.getTypeSafeValue(getValue(LISTCOLLECTIONDATE));
  }
  
  public void setListCollectionDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(LISTCOLLECTIONDATE, "");
    }
    else
    {
      setValue(LISTCOLLECTIONDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATETIME_FORMAT).format(value));
    }
  }
  
  public boolean isListCollectionDateWritable()
  {
    return isWritable(LISTCOLLECTIONDATE);
  }
  
  public boolean isListCollectionDateReadable()
  {
    return isReadable(LISTCOLLECTIONDATE);
  }
  
  public boolean isListCollectionDateModified()
  {
    return isModified(LISTCOLLECTIONDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO getListCollectionDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateTimeMdDTO) getAttributeDTO(LISTCOLLECTIONDATE).getAttributeMdDTO();
  }
  
  public String getListContactName()
  {
    return getValue(LISTCONTACTNAME);
  }
  
  public void setListContactName(String value)
  {
    if(value == null)
    {
      setValue(LISTCONTACTNAME, "");
    }
    else
    {
      setValue(LISTCONTACTNAME, value);
    }
  }
  
  public boolean isListContactNameWritable()
  {
    return isWritable(LISTCONTACTNAME);
  }
  
  public boolean isListContactNameReadable()
  {
    return isReadable(LISTCONTACTNAME);
  }
  
  public boolean isListContactNameModified()
  {
    return isModified(LISTCONTACTNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListContactNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTCONTACTNAME).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListDescriptionDTO getListDescription()
  {
    return (net.geoprism.registry.ListTypeVersionListDescriptionDTO) this.getAttributeStructDTO(LISTDESCRIPTION).getStructDTO();
  }
  
  public boolean isListDescriptionWritable()
  {
    return isWritable(LISTDESCRIPTION);
  }
  
  public boolean isListDescriptionReadable()
  {
    return isReadable(LISTDESCRIPTION);
  }
  
  public boolean isListDescriptionModified()
  {
    return isModified(LISTDESCRIPTION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListDescriptionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTDESCRIPTION).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListDisclaimerDTO getListDisclaimer()
  {
    return (net.geoprism.registry.ListTypeVersionListDisclaimerDTO) this.getAttributeStructDTO(LISTDISCLAIMER).getStructDTO();
  }
  
  public boolean isListDisclaimerWritable()
  {
    return isWritable(LISTDISCLAIMER);
  }
  
  public boolean isListDisclaimerReadable()
  {
    return isReadable(LISTDISCLAIMER);
  }
  
  public boolean isListDisclaimerModified()
  {
    return isModified(LISTDISCLAIMER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListDisclaimerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTDISCLAIMER).getAttributeMdDTO();
  }
  
  public String getListEmail()
  {
    return getValue(LISTEMAIL);
  }
  
  public void setListEmail(String value)
  {
    if(value == null)
    {
      setValue(LISTEMAIL, "");
    }
    else
    {
      setValue(LISTEMAIL, value);
    }
  }
  
  public boolean isListEmailWritable()
  {
    return isWritable(LISTEMAIL);
  }
  
  public boolean isListEmailReadable()
  {
    return isReadable(LISTEMAIL);
  }
  
  public boolean isListEmailModified()
  {
    return isModified(LISTEMAIL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListEmailMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTEMAIL).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListLabelDTO getListLabel()
  {
    return (net.geoprism.registry.ListTypeVersionListLabelDTO) this.getAttributeStructDTO(LISTLABEL).getStructDTO();
  }
  
  public boolean isListLabelWritable()
  {
    return isWritable(LISTLABEL);
  }
  
  public boolean isListLabelReadable()
  {
    return isReadable(LISTLABEL);
  }
  
  public boolean isListLabelModified()
  {
    return isModified(LISTLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTLABEL).getAttributeMdDTO();
  }
  
  public Boolean getListMaster()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(LISTMASTER));
  }
  
  public void setListMaster(Boolean value)
  {
    if(value == null)
    {
      setValue(LISTMASTER, "");
    }
    else
    {
      setValue(LISTMASTER, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isListMasterWritable()
  {
    return isWritable(LISTMASTER);
  }
  
  public boolean isListMasterReadable()
  {
    return isReadable(LISTMASTER);
  }
  
  public boolean isListMasterModified()
  {
    return isModified(LISTMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getListMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(LISTMASTER).getAttributeMdDTO();
  }
  
  public String getListOrganization()
  {
    return getValue(LISTORGANIZATION);
  }
  
  public void setListOrganization(String value)
  {
    if(value == null)
    {
      setValue(LISTORGANIZATION, "");
    }
    else
    {
      setValue(LISTORGANIZATION, value);
    }
  }
  
  public boolean isListOrganizationWritable()
  {
    return isWritable(LISTORGANIZATION);
  }
  
  public boolean isListOrganizationReadable()
  {
    return isReadable(LISTORGANIZATION);
  }
  
  public boolean isListOrganizationModified()
  {
    return isModified(LISTORGANIZATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListOrganizationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTORGANIZATION).getAttributeMdDTO();
  }
  
  public String getListOriginator()
  {
    return getValue(LISTORIGINATOR);
  }
  
  public void setListOriginator(String value)
  {
    if(value == null)
    {
      setValue(LISTORIGINATOR, "");
    }
    else
    {
      setValue(LISTORIGINATOR, value);
    }
  }
  
  public boolean isListOriginatorWritable()
  {
    return isWritable(LISTORIGINATOR);
  }
  
  public boolean isListOriginatorReadable()
  {
    return isReadable(LISTORIGINATOR);
  }
  
  public boolean isListOriginatorModified()
  {
    return isModified(LISTORIGINATOR);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListOriginatorMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTORIGINATOR).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListProcessDTO getListProcess()
  {
    return (net.geoprism.registry.ListTypeVersionListProcessDTO) this.getAttributeStructDTO(LISTPROCESS).getStructDTO();
  }
  
  public boolean isListProcessWritable()
  {
    return isWritable(LISTPROCESS);
  }
  
  public boolean isListProcessReadable()
  {
    return isReadable(LISTPROCESS);
  }
  
  public boolean isListProcessModified()
  {
    return isModified(LISTPROCESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListProcessMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTPROCESS).getAttributeMdDTO();
  }
  
  public net.geoprism.registry.ListTypeVersionListProgressDTO getListProgress()
  {
    return (net.geoprism.registry.ListTypeVersionListProgressDTO) this.getAttributeStructDTO(LISTPROGRESS).getStructDTO();
  }
  
  public boolean isListProgressWritable()
  {
    return isWritable(LISTPROGRESS);
  }
  
  public boolean isListProgressReadable()
  {
    return isReadable(LISTPROGRESS);
  }
  
  public boolean isListProgressModified()
  {
    return isModified(LISTPROGRESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListProgressMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTPROGRESS).getAttributeMdDTO();
  }
  
  public String getListTelephoneNumber()
  {
    return getValue(LISTTELEPHONENUMBER);
  }
  
  public void setListTelephoneNumber(String value)
  {
    if(value == null)
    {
      setValue(LISTTELEPHONENUMBER, "");
    }
    else
    {
      setValue(LISTTELEPHONENUMBER, value);
    }
  }
  
  public boolean isListTelephoneNumberWritable()
  {
    return isWritable(LISTTELEPHONENUMBER);
  }
  
  public boolean isListTelephoneNumberReadable()
  {
    return isReadable(LISTTELEPHONENUMBER);
  }
  
  public boolean isListTelephoneNumberModified()
  {
    return isModified(LISTTELEPHONENUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getListTelephoneNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LISTTELEPHONENUMBER).getAttributeMdDTO();
  }
  
  public boolean isListTypeWritable()
  {
    return isWritable(LISTTYPE);
  }
  
  public boolean isListTypeReadable()
  {
    return isReadable(LISTTYPE);
  }
  
  public boolean isListTypeModified()
  {
    return isModified(LISTTYPE);
  }
  
  public net.geoprism.registry.ListTypeVersionListUseConstraintsDTO getListUseConstraints()
  {
    return (net.geoprism.registry.ListTypeVersionListUseConstraintsDTO) this.getAttributeStructDTO(LISTUSECONSTRAINTS).getStructDTO();
  }
  
  public boolean isListUseConstraintsWritable()
  {
    return isWritable(LISTUSECONSTRAINTS);
  }
  
  public boolean isListUseConstraintsReadable()
  {
    return isReadable(LISTUSECONSTRAINTS);
  }
  
  public boolean isListUseConstraintsModified()
  {
    return isModified(LISTUSECONSTRAINTS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO getListUseConstraintsMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeLocalTextMdDTO) getAttributeDTO(LISTUSECONSTRAINTS).getAttributeMdDTO();
  }
  
  public String getListVisibility()
  {
    return getValue(LISTVISIBILITY);
  }
  
  public void setListVisibility(String value)
  {
    if(value == null)
    {
      setValue(LISTVISIBILITY, "");
    }
    else
    {
      setValue(LISTVISIBILITY, value);
    }
  }
  
  public boolean isListVisibilityWritable()
  {
    return isWritable(LISTVISIBILITY);
  }
  
  public boolean isListVisibilityReadable()
  {
    return isReadable(LISTVISIBILITY);
  }
  
  public boolean isListVisibilityModified()
  {
    return isModified(LISTVISIBILITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getListVisibilityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LISTVISIBILITY).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.SingleActorDTO getLockedBy()
  {
    if(getValue(LOCKEDBY) == null || getValue(LOCKEDBY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.SingleActorDTO.get(getRequest(), getValue(LOCKEDBY));
    }
  }
  
  public String getLockedByOid()
  {
    return getValue(LOCKEDBY);
  }
  
  public boolean isLockedByWritable()
  {
    return isWritable(LOCKEDBY);
  }
  
  public boolean isLockedByReadable()
  {
    return isReadable(LOCKEDBY);
  }
  
  public boolean isLockedByModified()
  {
    return isModified(LOCKEDBY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getLockedByMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(LOCKEDBY).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdBusinessDTO getMdBusiness()
  {
    if(getValue(MDBUSINESS) == null || getValue(MDBUSINESS).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdBusinessDTO.get(getRequest(), getValue(MDBUSINESS));
    }
  }
  
  public String getMdBusinessOid()
  {
    return getValue(MDBUSINESS);
  }
  
  public void setMdBusiness(com.runwaysdk.system.metadata.MdBusinessDTO value)
  {
    if(value == null)
    {
      setValue(MDBUSINESS, "");
    }
    else
    {
      setValue(MDBUSINESS, value.getOid());
    }
  }
  
  public boolean isMdBusinessWritable()
  {
    return isWritable(MDBUSINESS);
  }
  
  public boolean isMdBusinessReadable()
  {
    return isReadable(MDBUSINESS);
  }
  
  public boolean isMdBusinessModified()
  {
    return isModified(MDBUSINESS);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMdBusinessMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MDBUSINESS).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.ActorDTO getOwner()
  {
    if(getValue(OWNER) == null || getValue(OWNER).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.ActorDTO.get(getRequest(), getValue(OWNER));
    }
  }
  
  public String getOwnerOid()
  {
    return getValue(OWNER);
  }
  
  public void setOwner(com.runwaysdk.system.ActorDTO value)
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
  
  public boolean isOwnerWritable()
  {
    return isWritable(OWNER);
  }
  
  public boolean isOwnerReadable()
  {
    return isReadable(OWNER);
  }
  
  public boolean isOwnerModified()
  {
    return isModified(OWNER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getOwnerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(OWNER).getAttributeMdDTO();
  }
  
  public java.util.Date getPublishDate()
  {
    return com.runwaysdk.constants.MdAttributeDateUtil.getTypeSafeValue(getValue(PUBLISHDATE));
  }
  
  public void setPublishDate(java.util.Date value)
  {
    if(value == null)
    {
      setValue(PUBLISHDATE, "");
    }
    else
    {
      setValue(PUBLISHDATE, new java.text.SimpleDateFormat(com.runwaysdk.constants.Constants.DATE_FORMAT).format(value));
    }
  }
  
  public boolean isPublishDateWritable()
  {
    return isWritable(PUBLISHDATE);
  }
  
  public boolean isPublishDateReadable()
  {
    return isReadable(PUBLISHDATE);
  }
  
  public boolean isPublishDateModified()
  {
    return isModified(PUBLISHDATE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDateMdDTO getPublishDateMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDateMdDTO) getAttributeDTO(PUBLISHDATE).getAttributeMdDTO();
  }
  
  public Long getSeq()
  {
    return com.runwaysdk.constants.MdAttributeLongUtil.getTypeSafeValue(getValue(SEQ));
  }
  
  public boolean isSeqWritable()
  {
    return isWritable(SEQ);
  }
  
  public boolean isSeqReadable()
  {
    return isReadable(SEQ);
  }
  
  public boolean isSeqModified()
  {
    return isModified(SEQ);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getSeqMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(SEQ).getAttributeMdDTO();
  }
  
  public String getSiteMaster()
  {
    return getValue(SITEMASTER);
  }
  
  public boolean isSiteMasterWritable()
  {
    return isWritable(SITEMASTER);
  }
  
  public boolean isSiteMasterReadable()
  {
    return isReadable(SITEMASTER);
  }
  
  public boolean isSiteMasterModified()
  {
    return isModified(SITEMASTER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSiteMasterMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SITEMASTER).getAttributeMdDTO();
  }
  
  public Integer getVersionNumber()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(VERSIONNUMBER));
  }
  
  public void setVersionNumber(Integer value)
  {
    if(value == null)
    {
      setValue(VERSIONNUMBER, "");
    }
    else
    {
      setValue(VERSIONNUMBER, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isVersionNumberWritable()
  {
    return isWritable(VERSIONNUMBER);
  }
  
  public boolean isVersionNumberReadable()
  {
    return isReadable(VERSIONNUMBER);
  }
  
  public boolean isVersionNumberModified()
  {
    return isModified(VERSIONNUMBER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getVersionNumberMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(VERSIONNUMBER).getAttributeMdDTO();
  }
  
  public Boolean getWorking()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(WORKING));
  }
  
  public void setWorking(Boolean value)
  {
    if(value == null)
    {
      setValue(WORKING, "");
    }
    else
    {
      setValue(WORKING, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isWorkingWritable()
  {
    return isWritable(WORKING);
  }
  
  public boolean isWorkingReadable()
  {
    return isReadable(WORKING);
  }
  
  public boolean isWorkingModified()
  {
    return isModified(WORKING);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getWorkingMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(WORKING).getAttributeMdDTO();
  }
  
  public final java.lang.String publish()
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "publish", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String publish(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "publish", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.registry.ListTypeVersionDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
  public static net.geoprism.registry.ListTypeVersionQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.registry.ListTypeVersionQueryDTO) clientRequest.getAllInstances(net.geoprism.registry.ListTypeVersionDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.registry.ListTypeVersionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.registry.ListTypeVersionDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.ListTypeVersionDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.registry.ListTypeVersionDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
