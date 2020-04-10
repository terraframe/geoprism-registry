package net.geoprism.registry.roles;

@com.runwaysdk.business.ClassSignature(hash = 1491770377)
public abstract class HierarchyRelationshipPermissionExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.roles.HierarchyRelationshipPermissionException";
  private static final long serialVersionUID = 1491770377;
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public HierarchyRelationshipPermissionExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CHILDGEOOBJECTTYPE = "childGeoObjectType";
  public static java.lang.String OID = "oid";
  public static java.lang.String ORGANIZATION = "organization";
  public static java.lang.String PARENTGEOOBJECTTYPE = "parentGeoObjectType";
  public String getChildGeoObjectType()
  {
    return getValue(CHILDGEOOBJECTTYPE);
  }
  
  public void setChildGeoObjectType(String value)
  {
    if(value == null)
    {
      setValue(CHILDGEOOBJECTTYPE, "");
    }
    else
    {
      setValue(CHILDGEOOBJECTTYPE, value);
    }
  }
  
  public boolean isChildGeoObjectTypeWritable()
  {
    return isWritable(CHILDGEOOBJECTTYPE);
  }
  
  public boolean isChildGeoObjectTypeReadable()
  {
    return isReadable(CHILDGEOOBJECTTYPE);
  }
  
  public boolean isChildGeoObjectTypeModified()
  {
    return isModified(CHILDGEOOBJECTTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getChildGeoObjectTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CHILDGEOOBJECTTYPE).getAttributeMdDTO();
  }
  
  public String getOrganization()
  {
    return getValue(ORGANIZATION);
  }
  
  public void setOrganization(String value)
  {
    if(value == null)
    {
      setValue(ORGANIZATION, "");
    }
    else
    {
      setValue(ORGANIZATION, value);
    }
  }
  
  public boolean isOrganizationWritable()
  {
    return isWritable(ORGANIZATION);
  }
  
  public boolean isOrganizationReadable()
  {
    return isReadable(ORGANIZATION);
  }
  
  public boolean isOrganizationModified()
  {
    return isModified(ORGANIZATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOrganizationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ORGANIZATION).getAttributeMdDTO();
  }
  
  public String getParentGeoObjectType()
  {
    return getValue(PARENTGEOOBJECTTYPE);
  }
  
  public void setParentGeoObjectType(String value)
  {
    if(value == null)
    {
      setValue(PARENTGEOOBJECTTYPE, "");
    }
    else
    {
      setValue(PARENTGEOOBJECTTYPE, value);
    }
  }
  
  public boolean isParentGeoObjectTypeWritable()
  {
    return isWritable(PARENTGEOOBJECTTYPE);
  }
  
  public boolean isParentGeoObjectTypeReadable()
  {
    return isReadable(PARENTGEOOBJECTTYPE);
  }
  
  public boolean isParentGeoObjectTypeModified()
  {
    return isModified(PARENTGEOOBJECTTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentGeoObjectTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTGEOOBJECTTYPE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{childGeoObjectType}", this.getChildGeoObjectType().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{organization}", this.getOrganization().toString());
    template = template.replace("{parentGeoObjectType}", this.getParentGeoObjectType().toString());
    
    return template;
  }
  
}
