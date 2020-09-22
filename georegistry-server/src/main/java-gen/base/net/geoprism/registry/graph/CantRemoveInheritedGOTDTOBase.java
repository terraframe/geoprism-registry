package net.geoprism.registry.graph;

@com.runwaysdk.business.ClassSignature(hash = -1006816161)
public abstract class CantRemoveInheritedGOTDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.graph.CantRemoveInheritedGOT";
  private static final long serialVersionUID = -1006816161;
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected CantRemoveInheritedGOTDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public CantRemoveInheritedGOTDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String GOTCODE = "gotCode";
  public static java.lang.String HIERCODE = "hierCode";
  public static java.lang.String INHERITEDHIERARCHYLIST = "inheritedHierarchyList";
  public static java.lang.String OID = "oid";
  public String getGotCode()
  {
    return getValue(GOTCODE);
  }
  
  public void setGotCode(String value)
  {
    if(value == null)
    {
      setValue(GOTCODE, "");
    }
    else
    {
      setValue(GOTCODE, value);
    }
  }
  
  public boolean isGotCodeWritable()
  {
    return isWritable(GOTCODE);
  }
  
  public boolean isGotCodeReadable()
  {
    return isReadable(GOTCODE);
  }
  
  public boolean isGotCodeModified()
  {
    return isModified(GOTCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getGotCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(GOTCODE).getAttributeMdDTO();
  }
  
  public String getHierCode()
  {
    return getValue(HIERCODE);
  }
  
  public void setHierCode(String value)
  {
    if(value == null)
    {
      setValue(HIERCODE, "");
    }
    else
    {
      setValue(HIERCODE, value);
    }
  }
  
  public boolean isHierCodeWritable()
  {
    return isWritable(HIERCODE);
  }
  
  public boolean isHierCodeReadable()
  {
    return isReadable(HIERCODE);
  }
  
  public boolean isHierCodeModified()
  {
    return isModified(HIERCODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getHierCodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(HIERCODE).getAttributeMdDTO();
  }
  
  public String getInheritedHierarchyList()
  {
    return getValue(INHERITEDHIERARCHYLIST);
  }
  
  public void setInheritedHierarchyList(String value)
  {
    if(value == null)
    {
      setValue(INHERITEDHIERARCHYLIST, "");
    }
    else
    {
      setValue(INHERITEDHIERARCHYLIST, value);
    }
  }
  
  public boolean isInheritedHierarchyListWritable()
  {
    return isWritable(INHERITEDHIERARCHYLIST);
  }
  
  public boolean isInheritedHierarchyListReadable()
  {
    return isReadable(INHERITEDHIERARCHYLIST);
  }
  
  public boolean isInheritedHierarchyListModified()
  {
    return isModified(INHERITEDHIERARCHYLIST);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getInheritedHierarchyListMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(INHERITEDHIERARCHYLIST).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{gotCode}", this.getGotCode().toString());
    template = template.replace("{hierCode}", this.getHierCode().toString());
    template = template.replace("{inheritedHierarchyList}", this.getInheritedHierarchyList().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
